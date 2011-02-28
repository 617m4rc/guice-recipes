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

package org.guicerecipes.jsr250;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.guicerecipes.util.*;

/**
 * A cache which maintains which method is annotated by a given annotation for each class
 * 
 * @version $Revision: 1.1 $
 */
class AnnotatedMethodCache {
	private final Class<? extends Annotation> annotationType;
	private Map<Class<?>, Method> methodCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, Method>());

	public AnnotatedMethodCache(Class<? extends Annotation> annotationType) {
		this.annotationType = annotationType;
	}

	/**
	 * Looks up the method which is annotated for the given type
	 */
	public Method getMethod(Class<?> type) {
		// if we are invoked concurrently it doesn't matter if we look up the method
		// concurrently - its the same instance that will be overwritten in the map
		Method method = methodCache.get(type);
		if (method == null) {
			method = Reflection.findMethodWithAnnotation(type, annotationType, true);
			if (method != null) {
				if (method.getParameterTypes().length != 0) {
					throw new IllegalArgumentException("Method should have no arguments for @PreDestroy: " + method);
				}
				methodCache.put(type, method);
			}
		}
		return method;
	}

}
