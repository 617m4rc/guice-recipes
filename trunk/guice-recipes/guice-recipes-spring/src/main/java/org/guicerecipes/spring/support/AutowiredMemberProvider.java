/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guicerecipes.spring.support;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.guicerecipes.*;
import org.guicerecipes.spring.*;
import org.guicerecipes.support.*;
import org.guicerecipes.support.Predicate;
import org.guicerecipes.support.Strings;
import org.springframework.beans.factory.annotation.*;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.inject.*;
import com.google.inject.name.*;

/**
 * Creates a value for an {@link Autowired} member with an optional {@link Qualifier} annotation
 * 
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class AutowiredMemberProvider extends AnnotationMemberProviderSupport<Autowired> {

	private final Injector injector;

	@Inject
	public AutowiredMemberProvider(Injector injector) {
		Preconditions.checkNotNull(injector, "injector");
		this.injector = injector;
	}

	public boolean isNullParameterAllowed(Autowired annotation, Method method, Class<?> parameterType, int parameterIndex) {
		return !annotation.required();
	}

	@Override
	protected Object provide(Autowired annotation, Member member, TypeLiteral<?> typeLiteral, Class<?> memberType, Annotation[] annotations) {
		Predicate<Binding> filter = createQualifierFilter(member, annotations);

		Class<?> type = typeLiteral.getRawType();
		if (type.isArray()) {
			return provideArrayValue(member, typeLiteral, memberType, filter);
		} else if (Collection.class.isAssignableFrom(type)) {
			Collection collection = createCollection(type);
			return provideCollectionValues(collection, member, typeLiteral, filter);
		} else if (Map.class.isAssignableFrom(type)) {
			Map map = createMap(type);
			return provideMapValues(map, member, typeLiteral, filter);
		} else {
			return provideSingleValue(member, type, annotation, filter);
		}
	}

	/**
	 * Returns a new filter on the given member to respect the use of {@link Qualifier} annotations or annotations annotated with {@link Qualifier}
	 */
	protected Predicate<Binding> createQualifierFilter(Member member, Annotation[] parameterAnnotations) {

		if (member instanceof AnnotatedElement) {
			AnnotatedElement annotatedElement = (AnnotatedElement) member;
			final Qualifier qualifier = annotatedElement.getAnnotation(Qualifier.class);
			if (qualifier != null) {
				final String expectedValue = qualifier.value();
				final boolean notEmptyValue = Strings.isNotEmpty(expectedValue);
				return new Predicate<Binding>() {
					public boolean matches(Binding binding) {
						String value = annotationName(binding);

						// we cannot use @Qualified as a binding annotation
						// so we can't test for just a @Qualified binding with no text
						// so lets just test for a non-empty string
						if (notEmptyValue) {
							return Comparators.equal(expectedValue, value);
						} else {
							return Strings.isNotEmpty(value);
						}
					}

					@Override
					public String toString() {
						return "@Autowired @Qualifier(" + expectedValue + ")";
					}
				};
			}

			// lets iterate through all of the annotations looking for a qualifier
			Set<Annotation> qualifiedAnnotations = Sets.newHashSet();
			Annotation[] annotations = annotatedElement.getAnnotations();
			for (Annotation annotation : annotations) {
				if (isQualified(annotation)) {
					qualifiedAnnotations.add(annotation);
				}
			}
			if (parameterAnnotations != null) {
				for (Annotation annotation : parameterAnnotations) {
					if (isQualified(annotation)) {
						qualifiedAnnotations.add(annotation);
					}
				}
			}
			int size = qualifiedAnnotations.size();
			if (size == 1) {
				final Annotation annotation = Iterables.getOnlyElement(qualifiedAnnotations);
				return new Predicate<Binding>() {
					public boolean matches(Binding binding) {
						Annotation actualAnnotation = binding.getKey().getAnnotation();
						return (actualAnnotation != null) && actualAnnotation.equals(annotation);
					}

					@Override
					public String toString() {
						return "@Autowired " + annotation;
					}
				};
			} else if (size > 0) {
				throw new ProvisionException("Too many qualified annotations " + qualifiedAnnotations + " when trying to inject " + member);
			}
		}
		return new Predicate<Binding>() {
			public boolean matches(Binding binding) {
				return true;
			}

			@Override
			public String toString() {
				return "@Autowired";
			}
		};
	}

	/** Returns true if the annotation is a qualified annotation and a valid Guice binding annotation */
	protected boolean isQualified(Annotation annotation) {
		Class<? extends Annotation> annotationType = annotation.annotationType();
		Qualifier qualified = annotationType.getAnnotation(Qualifier.class);
		if (qualified != null) {
			// we can only support qualified annotations which are also annotated with Guice's
			// @BindingAnnotation
			if (annotationType.getAnnotation(BindingAnnotation.class) != null) {
				return true;
			}
		}
		return false;
	}

	protected Object provideSingleValue(Member member, Class<?> type, Autowired annotation, Predicate<Binding> filter) {
		Set<Binding<?>> set = getSortedBindings(type, filter);
		int size = set.size();
		if (size == 1) {
			Binding<?> binding = Iterables.getOnlyElement(set);
			return binding.getProvider().get();
		} else if (size == 0) {
			// should we at least try and create one
			try {
				Binding<?> binding = injector.getBinding(type);
				if (filter.matches(binding)) {
					return binding.getProvider().get();
				} else {
					if (annotation.required()) {
						throw new ProvisionException("Could not find required binding for " + filter + " when injecting " + member);
					}
					return null;
				}
			} catch (Exception e) {
				// TODO should we log the warning that we can't resolve this?
				if (annotation.required()) {
					if (e instanceof ProvisionException) {
						throw (ProvisionException) e;
					}
					throw new ProvisionException("Could not resolve type " + type.getCanonicalName() + " with filter " + filter + " when injecting " + member + ": " + e, e);
				}
				return null;
			}
			// throw new ProvisionException("No binding could be found for " + type.getCanonicalName());
		} else {
			throw new ProvisionException("Too many bindings " + size + " found for " + type.getCanonicalName() + " with keys " + keys(set) + " when injecting " + member);
		}
	}

	/** Returns the keys used in the given bindings */
	public static List<Key<?>> keys(Iterable<Binding<?>> bindings) {
		List<Key<?>> answer = Lists.newArrayList();
		for (Binding<?> binding : bindings) {
			answer.add(binding.getKey());
		}
		return answer;
	}

	protected Object provideArrayValue(Member member, TypeLiteral<?> type, Class<?> memberType, Predicate<Binding> filter) {
		Class<?> componentType = memberType.getComponentType();
		Set<Binding<?>> set = getSortedBindings(componentType, filter);
		// TODO should we return an empty array when no matches?
		// FWIW Spring seems to return null
		if (set.isEmpty()) {
			return null;
		}
		Object array = Array.newInstance(componentType, set.size());
		int index = 0;
		for (Binding<?> binding : set) {
			Object value = binding.getProvider().get();
			Array.set(array, index++, value);
		}
		return array;
	}

	private Collection provideCollectionValues(Collection collection, Member member, TypeLiteral<?> type, Predicate<Binding> filter) {
		Type typeInstance = type.getType();
		if (typeInstance instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) typeInstance;
			Type[] arguments = parameterizedType.getActualTypeArguments();
			if (arguments.length == 1) {
				Type argument = arguments[0];
				if (argument instanceof Class) {
					Class<?> componentType = (Class<?>) argument;
					if (componentType != Object.class) {
						Set<Binding<?>> set = getSortedBindings(componentType, filter);
						if (set.isEmpty()) {
							// TODO return null or empty collection if nothing to inject?
							return null;
						}
						for (Binding<?> binding : set) {
							Object value = binding.getProvider().get();
							collection.add(value);
						}
						return collection;
					}
				}
			}
		}
		// TODO return null or empty collection if nothing to inject?
		return null;
	}

	protected Map provideMapValues(Map map, Member member, TypeLiteral<?> type, Predicate<Binding> filter) {
		Type typeInstance = type.getType();
		if (typeInstance instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) typeInstance;
			Type[] arguments = parameterizedType.getActualTypeArguments();
			if (arguments.length == 2) {
				Type key = arguments[0];
				if (key instanceof Class) {
					Class<?> keyType = (Class<?>) key;
					if ((keyType != Object.class) && (keyType != String.class)) {
						throw new ProvisionException("Cannot inject Map instances with a key type of " + keyType.getName() + " for " + member);
					}
					Type valueType = arguments[1];
					if (valueType instanceof Class) {
						Class<?> componentType = (Class<?>) valueType;
						if (componentType != Object.class) {
							Set<Binding<?>> set = getSortedBindings(componentType, filter);
							if (set.isEmpty()) {
								// TODO return null or empty collection if nothing to inject?
								return null;
							}
							for (Binding<?> binding : set) {
								Object keyValue = binding.getKey().toString();
								Object value = binding.getProvider().get();
								map.put(keyValue, value);
							}
							return map;
						}
					}
				}
			}
		}
		// TODO return null or empty collection if nothing to inject?
		return null;
	}

	protected Map createMap(Class<?> type) {
		Object answer = tryCreateInstance(type);
		if (answer instanceof Map) {
			return (Map) answer;
		} else if (SortedMap.class.isAssignableFrom(type)) {
			return new TreeMap();
		}
		return new HashMap();
	}

	protected Collection createCollection(Class<?> type) {
		Object answer = tryCreateInstance(type);
		if (answer instanceof Collection) {
			return (Collection) answer;
		} else if (SortedSet.class.isAssignableFrom(type)) {
			return new TreeSet();
		} else if (Set.class.isAssignableFrom(type)) {
			return new HashSet();
		}
		return new ArrayList();
	}

	/**
	 * Returns a new instance of the given class if its a public non abstract class which has a public zero argument constructor otherwise returns null
	 */
	protected Object tryCreateInstance(Class<?> type) {
		Object answer = null;
		int modifiers = type.getModifiers();
		if (!Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers) && !type.isInterface()) {
			// if its a concrete class with no args make one
			Constructor<?> constructor = null;
			try {
				constructor = type.getConstructor();
			} catch (NoSuchMethodException e) {
				// ignore
			}
			if (constructor != null) {
				if (Modifier.isPublic(constructor.getModifiers())) {
					try {
						answer = constructor.newInstance();
					} catch (InstantiationException e) {
						throw new ProvisionException("Failed to instantiate " + constructor, e);
					} catch (IllegalAccessException e) {
						throw new ProvisionException("Failed to instantiate " + constructor, e);
					} catch (InvocationTargetException ie) {
						Throwable e = ie.getTargetException();
						throw new ProvisionException("Failed to instantiate " + constructor, e);
					}
				}
			}
		}
		return answer;
	}

	protected Set<Binding<?>> getSortedBindings(Class<?> type, Predicate<Binding> filter) {
		SortedSet<Binding<?>> answer = new TreeSet<Binding<?>>(new Comparator<Binding<?>>() {
			public int compare(Binding<?> b1, Binding<?> b2) {

				int answer = typeName(b1).compareTo(typeName(b2));
				if (answer == 0) {
					// TODO would be nice to use google colletions here but its excluded from guice
					String n1 = annotationName(b1);
					String n2 = annotationName(b2);
					if ((n1 != null) || (n2 != null)) {
						if (n1 == null) {
							return -1;
						}
						if (n2 == null) {
							return 1;
						}
						return n1.compareTo(n2);
					}
				}
				return answer;
			}
		});
		Set<Binding<?>> bindings = Injectors.getBindingsOf(injector, type);
		for (Binding<?> binding : bindings) {
			if (isValidAutowireBinding(binding) && filter.matches(binding)) {
				answer.add(binding);
			}
		}
		/*
		 * if (answer.isEmpty() && bindings.size() == 1) { // if we have no matches on the filter, but we have a single static value, lets return it return bindings; }
		 */
		return answer;
	}

	protected boolean isValidAutowireBinding(Binding<?> binding) {
		Key<?> key = binding.getKey();
		Annotation annotation = key.getAnnotation();
		if (annotation instanceof NoAutowire) {
			return false;
		}
		Class<? extends Annotation> annotationType = key.getAnnotationType();
		if ((annotationType != null) && NoAutowire.class.isAssignableFrom(annotationType)) {
			return false;
		}
		return true;
	}

	private String annotationName(Binding<?> binding) {
		Annotation annotation = binding.getKey().getAnnotation();
		if (annotation instanceof Named) {
			Named named = (Named) annotation;
			return named.value();
		}
		if (annotation instanceof Qualifier) {
			Qualifier qualifier = (Qualifier) annotation;
			return qualifier.value();
		}
		return null;
	}

	protected static String typeName(Binding<?> bindings) {
		return bindings.getKey().getTypeLiteral().getRawType().getName();
	}

}
