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

import java.util.*;

import org.guicerecipes.support.internal.*;

import com.google.common.collect.*;

/**
 * A class which is stored in a scope which is then used to close any objects in the same scope which have a shut down hook associated with them (such as @PreDestroy from JSR 250)
 * 
 * @version $Revision: 1.1 $
 */
public class PreDestroyer {
	List<CloseTask> closeTasks = Lists.newArrayList();

	public void addCloseTask(CloseTask closer) {
		closeTasks.add(closer);
	}

	public void close() throws CloseFailedException {
		CloseErrors errors = new CloseErrorsImpl(this);
		for (CloseTask task : closeTasks) {
			try {
				task.perform();
			} catch (Exception e) {
				errors.closeError(this, task.getSource(), e);
			}
		}
		errors.throwIfNecessary();
	}
}
