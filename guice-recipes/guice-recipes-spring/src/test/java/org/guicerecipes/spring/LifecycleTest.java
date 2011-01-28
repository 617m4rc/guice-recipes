/**
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guicerecipes.spring;

import junit.framework.*;

import org.guicerecipes.*;
import org.guicerecipes.support.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;

import com.google.inject.*;

/** @author james.strachan@gmail.com (James Strachan) */
public class LifecycleTest extends TestCase {

	public void testBeanInitialised() throws CreationException, CloseFailedException {
		Injector injector = Guice.createInjector(new SpringModule(), new AbstractModule() {
			@Override
			protected void configure() {
				bind(MyBean.class).in(Singleton.class);
			}
		});

		MyBean bean = injector.getInstance(MyBean.class);
		assertNotNull("Should have instantiated the bean", bean);
		assertTrue("The post construct lifecycle should have been invoked on bean", bean.postConstruct);

		AnotherBean another = bean.another;
		assertNotNull("Should have instantiated the another", another);
		assertTrue("The post construct lifecycle should have been invoked on another", another.postConstruct);

		assertFalse("The pre destroy lifecycle not should have been invoked on bean", bean.preDestroy);
		Injectors.close(injector);

		assertTrue("The pre destroy lifecycle should have been invoked on bean", bean.preDestroy);
	}

	static class MyBean implements InitializingBean, DisposableBean {
		@Autowired
		public AnotherBean another;

		public boolean postConstruct;
		public boolean preDestroy;

		public void afterPropertiesSet() throws Exception {
			postConstruct = true;
		}

		public void destroy() throws Exception {
			preDestroy = true;
		}
	}

	static class AnotherBean implements InitializingBean {
		public boolean postConstruct;

		public void afterPropertiesSet() throws Exception {
			postConstruct = true;
		}
	}
}
