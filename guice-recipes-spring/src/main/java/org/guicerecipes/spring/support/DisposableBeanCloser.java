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

package org.guicerecipes.spring.support;

import org.guicerecipes.support.*;
import org.springframework.beans.factory.*;

/**
 * A {@link Closer} which detects the Spring {@link DisposableBean} interface and invokes the {@link org.springframework.beans.factory.DisposableBean#destroy()} method when a scope is closed.
 * 
 * @version $Revision: 1.1 $
 */
public class DisposableBeanCloser implements Closer {
	public void close(Object object) throws Throwable {
		if (object instanceof DisposableBean) {
			DisposableBean disposableBean = (DisposableBean) object;
			disposableBean.destroy();
		}
	}
}
