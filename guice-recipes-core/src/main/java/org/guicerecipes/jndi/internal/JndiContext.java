/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guicerecipes.jndi.internal;

import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.naming.Binding;
import javax.naming.spi.*;

import com.google.inject.*;

/**
 * A default JNDI context
 * 
 * @version $Revision:$
 */
public class JndiContext implements Context, Serializable {
	public static final String SEPARATOR = "/";
	protected static final NameParser NAME_PARSER = new NameParser() {
		public Name parse(String name) throws NamingException {
			return new CompositeName(name);
		}
	};
	private static final long serialVersionUID = -5754338187296859149L;

	private final Hashtable<String, Object> environment; // environment for this context
	private final Map<String, Object> bindings; // bindings at my level
	private final Map<String, Object> treeBindings; // all bindings under me
	private boolean frozen;
	private String nameInNamespace = "";

	public JndiContext() throws Exception {
		this(new Hashtable<String, Object>(), new HashMap<String, Object>());
	}

	public JndiContext(Hashtable<String, Object> environment) {
		this(environment, new HashMap<String, Object>());
	}

	public JndiContext(Hashtable<String, Object> environment, Map<String, Object> bindings) {
		if (environment == null) {
			this.environment = new Hashtable<String, Object>();
		} else {
			this.environment = new Hashtable<String, Object>(environment);
		}
		this.bindings = bindings;
		treeBindings = new HashMap<String, Object>();
	}

	public JndiContext(Hashtable<String, Object> environment, Map<String, Object> bindings, String nameInNamespace) {
		this(environment, bindings);
		this.nameInNamespace = nameInNamespace;
	}

	protected JndiContext(JndiContext clone, Hashtable<String, Object> env) {
		bindings = clone.bindings;
		treeBindings = clone.treeBindings;
		environment = new Hashtable<String, Object>(env);
	}

	protected JndiContext(JndiContext clone, Hashtable<String, Object> env, String nameInNamespace) {
		this(clone, env);
		this.nameInNamespace = nameInNamespace;
	}

	public void freeze() {
		frozen = true;
	}

	boolean isFrozen() {
		return frozen;
	}

	/**
	 * internalBind is intended for use only during setup or possibly by suitably synchronized superclasses. It binds every possible lookup into a map in each context. To do this, each context strips
	 * off one name segment and if necessary creates a new context for it. Then it asks that context to bind the remaining name. It returns a map containing all the bindings from the next context,
	 * plus the context it just created (if it in fact created it). (the names are suitably extended by the segment originally lopped off).
	 */
	protected Map<String, Object> internalBind(String name, Object value) throws NamingException {
		/*
		 * // lets wrap the value in a Provider final Object originalValue = value; value = new Provider() { public Object get() { return originalValue; } };
		 */
		assert (name != null) && (name.length() > 0);
		assert !frozen;

		Map<String, Object> newBindings = new HashMap<String, Object>();
		int pos = name.indexOf('/');
		if (pos == -1) {
			if (treeBindings.put(name, value) != null) {
				throw new NamingException("Something already bound at " + name);
			}
			bindings.put(name, value);
			newBindings.put(name, value);
		} else {
			String segment = name.substring(0, pos);
			assert segment != null;
			assert !segment.equals("");
			Object o = treeBindings.get(segment);
			if (o == null) {
				o = newContext();
				treeBindings.put(segment, o);
				bindings.put(segment, o);
				newBindings.put(segment, o);
			} else if (!(o instanceof JndiContext)) {
				throw new NamingException("Something already bound where a subcontext should go");
			}
			JndiContext defaultContext = (JndiContext) o;
			String remainder = name.substring(pos + 1);
			Map<String, Object> subBindings = defaultContext.internalBind(remainder, value);
			for (Iterator<Map.Entry<String, Object>> iterator = subBindings.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				String subName = segment + "/" + (String) entry.getKey();
				Object bound = entry.getValue();
				treeBindings.put(subName, bound);
				newBindings.put(subName, bound);
			}
		}
		return newBindings;
	}

	protected JndiContext newContext() {
		try {
			return new JndiContext();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		return environment.put(propName, propVal);
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, Object> getEnvironment() throws NamingException {
		return (Hashtable<String, Object>) environment.clone();
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		return environment.remove(propName);
	}

	@SuppressWarnings("unchecked")
	public Object lookup(String name) throws NamingException {
		if (name.length() == 0) {
			return this;
		}
		Object result = treeBindings.get(name);
		if (result == null) {
			result = bindings.get(name);
		}
		if (result == null) {
			int pos = name.indexOf(':');
			if (pos > 0) {
				String scheme = name.substring(0, pos);
				Context ctx = NamingManager.getURLContext(scheme, environment);
				if (ctx == null) {
					throw new NamingException("scheme " + scheme + " not recognized");
				}
				return ctx.lookup(name);
			} else {
				// Split out the first name of the path
				// and look for it in the bindings map.
				CompositeName path = new CompositeName(name);

				if (path.size() == 0) {
					return this;
				} else {
					String first = path.get(0);
					Object value = bindings.get(first);
					if (value == null) {
						throw new NameNotFoundException(name);
					} else if ((value instanceof Context) && (path.size() > 1)) {
						Context subContext = (Context) value;
						value = subContext.lookup(path.getSuffix(1));
					}
					return value;
				}
			}
		}
		if (result instanceof Provider<?>) {
			Provider<Object> provider = (Provider<Object>) result;
			result = provider.get();
		}
		if (result instanceof LinkRef) {
			LinkRef ref = (LinkRef) result;
			result = lookup(ref.getLinkName());
		}
		if (result instanceof Reference) {
			try {
				result = NamingManager.getObjectInstance(result, null, null, environment);
			} catch (NamingException e) {
				throw e;
			} catch (Exception e) {
				throw (NamingException) new NamingException("could not look up : " + name).initCause(e);
			}
		}
		if (result instanceof JndiContext) {
			String prefix = getNameInNamespace();
			if (prefix.length() > 0) {
				prefix = prefix + SEPARATOR;
			}
			result = new JndiContext((JndiContext) result, environment, prefix + name);
		}
		return result;
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	public Object lookupLink(String name) throws NamingException {
		return lookup(name);
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		Name result = (Name) prefix.clone();
		result.addAll(name);
		return result;
	}

	public String composeName(String name, String prefix) throws NamingException {
		CompositeName result = new CompositeName(prefix);
		result.addAll(new CompositeName(name));
		return result.toString();
	}

	@SuppressWarnings("unchecked")
	public NamingEnumeration list(String name) throws NamingException {
		Object o = lookup(name);
		if (o == this) {
			return new ListEnumeration();
		} else if (o instanceof Context) {
			return ((Context) o).list("");
		} else {
			throw new NotContextException();
		}
	}

	@SuppressWarnings("unchecked")
	public NamingEnumeration listBindings(String name) throws NamingException {
		Object o = lookup(name);
		if (o == this) {
			return new ListBindingEnumeration();
		} else if (o instanceof Context) {
			return ((Context) o).listBindings("");
		} else {
			throw new NotContextException();
		}
	}

	public Object lookupLink(Name name) throws NamingException {
		return lookupLink(name.toString());
	}

	@SuppressWarnings("unchecked")
	public NamingEnumeration list(Name name) throws NamingException {
		return list(name.toString());
	}

	@SuppressWarnings("unchecked")
	public NamingEnumeration listBindings(Name name) throws NamingException {
		return listBindings(name.toString());
	}

	public void bind(Name name, Object value) throws NamingException {
		bind(name.toString(), value);
	}

	public void bind(String name, Object value) throws NamingException {
		if (isFrozen()) {
			throw new OperationNotSupportedException();
		} else {
			internalBind(name, value);
		}
	}

	public void close() throws NamingException {
		// ignore
	}

	public Context createSubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public void destroySubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public String getNameInNamespace() throws NamingException {
		return nameInNamespace;
	}

	public NameParser getNameParser(Name name) throws NamingException {
		return NAME_PARSER;
	}

	public NameParser getNameParser(String name) throws NamingException {
		return NAME_PARSER;
	}

	public void rebind(Name name, Object value) throws NamingException {
		bind(name, value);
	}

	public void rebind(String name, Object value) throws NamingException {
		bind(name, value);
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public void rename(String oldName, String newName) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public void unbind(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	public void unbind(String name) throws NamingException {
		bindings.remove(name);
		treeBindings.remove(name);
	}

	@SuppressWarnings("unchecked")
	private abstract class LocalNamingEnumeration implements NamingEnumeration {
		private Iterator i = bindings.entrySet().iterator();

		public boolean hasMore() throws NamingException {
			return i.hasNext();
		}

		public boolean hasMoreElements() {
			return i.hasNext();
		}

		protected Map.Entry getNext() {
			return (Map.Entry) i.next();
		}

		public void close() throws NamingException {
		}
	}

	private class ListEnumeration extends LocalNamingEnumeration {
		ListEnumeration() {
		}

		public Object next() throws NamingException {
			return nextElement();
		}

		@SuppressWarnings("unchecked")
		public Object nextElement() {
			Map.Entry entry = getNext();
			return new NameClassPair((String) entry.getKey(), entry.getValue().getClass().getName());
		}
	}

	private class ListBindingEnumeration extends LocalNamingEnumeration {
		ListBindingEnumeration() {
		}

		public Object next() throws NamingException {
			return nextElement();
		}

		@SuppressWarnings("unchecked")
		public Object nextElement() {
			Map.Entry entry = getNext();
			return new Binding((String) entry.getKey(), entry.getValue());
		}
	}
}
