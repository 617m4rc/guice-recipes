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

package org.guicerecipes.test.osgi;

import javax.annotation.*;

import org.apache.commons.logging.*;
import org.guicerecipes.jsr250.*;
import org.guicerecipes.support.*;
import org.junit.*;
import org.junit.runner.*;
import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.junit.*;
import org.ops4j.pax.exam.options.*;
import org.osgi.framework.*;

import com.google.inject.*;

import static org.junit.Assert.*;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.MavenUtils.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * Tests GuiceyFruit using OSGi
 * 
 * @version $Revision: 1.1 $
 */
@RunWith(JUnit4TestRunner.class)
@Ignore
// TODO Make it pass
public class GuiceyFruitOSGiTest {
	private static final transient Log LOG = LogFactory.getLog(GuiceyFruitOSGiTest.class);

	@Inject
	BundleContext bundleContext;

	@Test
	public void listBundles() {
		LOG.info("************ Hello from OSGi ************");

		for (Bundle b : bundleContext.getBundles()) {
			LOG.info("Bundle " + b.getBundleId() + " : " + b.getSymbolicName());
		}

		Guice.createInjector(new GuiceyFruitModule() {
		});

		Injector injector = Guice.createInjector(new Jsr250Module() {
			@Override
			protected void configure() {
				super.configure();

				bind(MyBean.class);

				bindInstance("foo", new AnotherBean("Foo"));
				bindInstance("xyz", new AnotherBean("XYZ"));
			}
		});

		MyBean bean = injector.getInstance(MyBean.class);
		assertNotNull("Should have instantiated the bean", bean);
		assertNotNull("Should have injected a foo", bean.foo);
		assertNotNull("Should have injected a bar", bean.bar);

		assertEquals("Should have injected correct foo", "Foo", bean.foo.name);
		assertEquals("Should have injected correct bar", "XYZ", bean.bar.name);

		LOG.info("Created bean from GuiceyFruit: " + bean);
	}

	@Configuration
	public static Option[] configure() {
		return options(
		// install log service using pax runners profile abstraction (there are more profiles, like DS)
			logProfile(),
			// this is how you set the default log level when using pax logging (logProfile)
			systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),

			mavenBundleAsInProject("org.guicerecipes", "guiceyfruit-core"), mavenBundleAsInProject("com.google.inject", "guice"),

			// Guice dependencies
			mavenBundleAsInProject("javax.annotation", "com.springsource.javax.annotation"), mavenBundleAsInProject("org.aopalliance", "com.springsource.org.aopalliance"),

			felix(), equinox());
	}

	/** TODO we can remove this method when 0.4.1 of Pax Exam comes out! */
	public static MavenUrlProvisionOption mavenBundleAsInProject(String groupId, String artifactId) {
		return mavenBundle().groupId(groupId).artifactId(artifactId).version(asInProject());
	}

	public static class MyBean {
		@Resource
		public AnotherBean foo;

		public AnotherBean bar;

		@Resource(name = "xyz")
		public void bar(AnotherBean bar) {
			this.bar = bar;
		}
	}

	static class AnotherBean {
		public String name = "undefined";

		AnotherBean(String name) {
			this.name = name;
		}
	}

}
