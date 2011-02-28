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

package org.guicerecipes;

import junit.framework.*;

import com.google.inject.*;

/** @version $Revision: 1.1 $ */
public class InjectorsTest extends TestCase {
	public void testMatchers() throws Exception {
		Injector injector = Guice.createInjector(new MyModule());

		assertEquals(2, Injectors.getInstancesOf(injector, A.class).size());
		assertEquals(1, Injectors.getInstancesOf(injector, B.class).size());
		assertEquals(1, Injectors.getInstancesOf(injector, C.class).size());

		//assertMatches(Injectors.getInstancesOf(injector, Matchers.subclassesOf(C.class).and(Matchers.annotatedWith(Blue.class))), hasSize(1));
	}

	public static class MyModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(C.class);
			bind(B.class);
		}
	}

	public static class A {
		public String name = "A";
	}

	public static class B extends A {
		public B() {
			name = "B";
		}
	}

	@Blue
	public static class C extends A {
		public C() {
			name = "C";
		}
	}

}
