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

package org.guicerecipes.experiments;

import java.lang.annotation.*;
import java.lang.reflect.*;

import junit.framework.*;

import com.google.common.base.*;
import com.google.inject.*;
import com.google.inject.spi.*;

import static java.lang.annotation.RetentionPolicy.*;

/** @version $Revision: 1.1 $ */
public class InjectionPointInjectTest extends TestCase {

	public void testInjectionPointInjection() throws Exception {
		// TODO
/*		Injector injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(String.class).annotatedWith(Property.class).toProvider(MyProvider.class);
			}
		});

		MyBean bean = injector.getInstance(MyBean.class);
		assertNotNull("Bean was null!", bean);

		assertEquals("bean.name", "hello foo", bean.name);
*/	}

	@Retention(RUNTIME)
	@BindingAnnotation
	public static @interface Property {
		String value();
	}

	public static class MyBean {
		@Inject
		@Property("foo")
		String name;
	}

	public static class MyProvider implements Provider<String> {
		@Inject
		InjectionPoint injectionPoint;

		public String get() {
			Preconditions.checkNotNull(injectionPoint, "injectionPoint not injected!");

			Member member = injectionPoint.getMember();
			if (member instanceof AnnotatedElement) {
				AnnotatedElement annotatedElement = (AnnotatedElement) member;
				Property property = annotatedElement.getAnnotation(Property.class);
				if (property != null) {
					String value = property.value();
					return "hello " + value;
				}
			}
			return null;
		}
	}
}
