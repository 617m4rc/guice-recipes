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

import javax.annotation.*;

import org.guicerecipes.support.*;

/**
 * A module which installs JSR 250 lifecycle and injection using the {@link Resource} annotation.
 * 
 * @version $Revision: 1.1 $
 */
public class Jsr250Module extends GuiceyFruitModule {

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		super.configure();

		bindAnnotationInjector(Resource.class, ResourceMemberProvider.class);

		bindMethodHandler(PostConstruct.class, new MethodHandler() {
			public void afterInjection(Object injectee, Annotation annotation, Method method) throws InvocationTargetException, IllegalAccessException {

				method.invoke(injectee);
			}
		});

		bind(PreDestroyCloser.class);
	}

}
