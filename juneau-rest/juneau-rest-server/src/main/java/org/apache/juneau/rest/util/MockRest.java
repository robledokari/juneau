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
package org.apache.juneau.rest.util;

import java.util.*;

import org.apache.juneau.rest.*;

/**
 * Creates a mocked interface against a REST resource class.
 */
public class MockRest {
	private static Map<Class<?>,RestContext> CONTEXTS = new HashMap<>();

	private final RestContext rc;
	
	private MockRest(Class<?> c) throws Exception {
		if (! CONTEXTS.containsKey(c))
			CONTEXTS.put(c, RestContext.create(c.newInstance()).build());
		rc = CONTEXTS.get(c);
	}
	
	/**
	 * Create a new mock REST interface 
	 * 
	 * @param c The REST class.
	 * @return A new mock interface.
	 * @throws Exception
	 */
	public static MockRest create(Class<?> c) throws Exception {
		return new MockRest(c);
	}
	
	/**
	 * Performs a REST request against the REST interface.
	 * 
	 * @param method The HTTP method
	 * @param path The URI path.
	 * @param pathArgs Optional path arguments.
	 * @return A new servlet request.
	 * @throws Exception
	 */
	public MockServletRequest request(String method, String path, Object...pathArgs) throws Exception {
		return MockServletRequest.create(method, path, pathArgs).restContext(rc);
	}
}
