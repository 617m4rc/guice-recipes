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

package org.guicerecipes.jpa.support;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.persistence.*;

import org.guicerecipes.support.*;

import com.google.inject.*;

/**
 * Allows the JPA persistence context to be injected via {@link javax.persistence.PersistenceContext}
 * 
 * @version $Revision: 1.1 $
 */
public class PersistenceMemberProvider extends AnnotationMemberProviderSupport<PersistenceContext> {

	private final Provider<EntityManager> defaultEntityManager;

	private Map<String, Provider<EntityManager>> namedEntityManagers;

	@Inject
	public PersistenceMemberProvider(Provider<EntityManager> defaultEntityManager) {
		this.defaultEntityManager = defaultEntityManager;
	}

	@Override
	protected Object provide(PersistenceContext annotation, Member member, TypeLiteral<?> requiredType, Class<?> memberType, Annotation[] annotations) {

		Provider<EntityManager> provider = null;

		String name = annotation.name();
		if ((namedEntityManagers != null) && (name != null) && (name.length() > 0)) {
			provider = namedEntityManagers.get(name);
		}
		if (provider == null) {
			provider = defaultEntityManager;
		}
		return provider.get();
	}

	public boolean isNullParameterAllowed(PersistenceContext annotation, Method method, Class<?> parameterType, int parameterIndex) {
		return false;
	}

	public Map<String, Provider<EntityManager>> getNamedEntityManagers() {
		return namedEntityManagers;
	}

	@Inject(optional = true)
	public void setNamedEntityManagers(Map<String, Provider<EntityManager>> namedEntityManagers) {
		this.namedEntityManagers = namedEntityManagers;
	}
}
