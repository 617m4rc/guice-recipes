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

package org.guicerecipes.support;

import java.lang.reflect.*;
import java.util.*;

import org.guicerecipes.support.internal.*;

import com.google.common.collect.*;
import com.google.inject.*;

/**
 * Some reflection helper methods
 * 
 * @version $Revision: 1.1 $
 */
public class Reflectors {
	/** Returns all the methods on the given type ignoring overloaded methods */
	public static List<Method> getAllMethods(Class<?> type) {
		return getAllMethods(TypeLiteral.get(type));
	}

	/** Returns all the methods on the given type ignoring overloaded methods */
	public static List<Method> getAllMethods(TypeLiteral<?> startType) {
		List<Method> answer = Lists.newArrayList();
		Map<MethodKey, Method> boundMethods = Maps.newHashMap();
		while (true) {
			Class<?> type = startType.getRawType();
			if (type == Object.class) {
				break;
			}

			Method[] methods = type.getDeclaredMethods();
			for (final Method method : methods) {
				MethodKey key = new MethodKey(method);
				if (boundMethods.get(key) == null) {
					boundMethods.put(key, method);
					answer.add(method);
				}
			}

			// startType = startType.getSupertype(type);
			Class<?> supertype = type.getSuperclass();
			if (supertype == Object.class) {
				break;
			}
			startType = startType.getSupertype(supertype);
		}
		return answer;
	}
}
