// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.rest.response;

import org.apache.juneau.http.annotation.*;

/**
 * Superclass of all predefined responses in this package.
 *
 * <p>
 * Consists simply of a simple string message.
 *
 * @deprecated Use {@link org.apache.juneau.http.response.HttpResponse}
 */
@Response
@Deprecated
public abstract class HttpResponse {

	private final String message;

	/**
	 * Constructor.
	 *
	 * @param message Message to send as the response.
	 */
	protected HttpResponse(String message) {
		this.message = message;
	}

	@ResponseBody
	@Override /* Object */
	public String toString() {
		return message;
	}
}
