/**
 * Copyright (C) 2008 Google Inc.
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

package org.guicerecipes.support;

import java.io.*;
import java.util.*;

import com.google.inject.internal.*;
import com.google.inject.spi.*;

/**
 * Indicates that an attempt to close an injector or scope failed closing one or more bindings.
 * 
 * @author james.strachan@gmail.com (James Strachan)
 */
public class CloseFailedException extends IOException {
	private static final long serialVersionUID = -8331122352619160766L;
	private final List<Message> messages;

	public CloseFailedException(List<Message> messages) {
		super(Errors.format("Close errors", messages));
		this.messages = messages;
	}

	public List<Message> getMessages() {
		return messages;
	}
}
