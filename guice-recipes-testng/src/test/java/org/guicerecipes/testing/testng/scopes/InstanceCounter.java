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

package org.guicerecipes.testing.testng.scopes;

import java.util.concurrent.atomic.*;

import javax.annotation.*;

/** @version $Revision: 1.1 $ */
public class InstanceCounter {
	public static final AtomicInteger startCounter = new AtomicInteger(0);
	public static final AtomicInteger stopCounter = new AtomicInteger(0);

	@PostConstruct
	public void start() {
		startCounter.incrementAndGet();
	}

	@PreDestroy
	public void stop() {
		stopCounter.incrementAndGet();
	}
}
