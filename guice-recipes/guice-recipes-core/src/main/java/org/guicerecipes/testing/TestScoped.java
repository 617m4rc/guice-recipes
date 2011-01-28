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

package org.guicerecipes.testing;

import java.lang.annotation.*;

import com.google.inject.*;

/**
 * This defines a {@link Scope} that lasts for a single test run.
 * 
 * <p>
 * A test conceptually comes in scope when it starts and goes out of scope when it finishes its execution (e.g., on JUnit lingo, roughly at the moment of {@link junit.framework.TestCase#setUp()} and
 * {@link junit.framework.TestCase#tearDown()}).
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 * @see org.guicerecipes.util.CloseableScope for an implementation of this scope
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TestScoped {
}
