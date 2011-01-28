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

package org.guicerecipes.testing.junit4;

import org.guicerecipes.testing.junit4.example.*;
import org.junit.*;
import org.junit.runner.*;

import com.google.inject.*;

/** @version $Revision: 1.1 $ */
@RunWith(GuiceyJUnit4.class)
public class NamingConventionTest {
	@Inject
	Cheese cheese;

	@Test
	public void testSomething() {
		System.out.println("Running!");

		Assert.assertEquals("cheese.sayHello", "Cheddar James", cheese.sayHello("James"));
	}

	public static class TestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Cheese.class).to(Cheddar.class);
		}
	}
}
