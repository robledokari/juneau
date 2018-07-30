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
package org.apache.juneau.rest;

import static org.apache.juneau.internal.ReflectionUtils.*;

import org.apache.juneau.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.httppart.*;

/**
 * Contains metadata about a throwable on a REST Java method.
 */
public class RestMethodThrown {

	final Class<?> type;
	final int code;
	final ObjectMap api;
	private final ResponseMeta responseMeta;

	RestMethodThrown(Class<?> type, HttpPartSerializer partSerializer, PropertyStore ps) {
		this.responseMeta = ResponseMeta.create(type, ps);

		HttpPartSchema s = HttpPartSchema.DEFAULT;
		if (hasAnnotation(Response.class, type))
			s = HttpPartSchema.create(Response.class, type);

		this.type = type;
		this.api = HttpPartSchema.getApiCodeMap(s, 500).unmodifiable();
		this.code = s.getCode(500);
	}

	/**
	 * Returns the return type of the Java method.
	 *
	 * @return The return type of the Java method.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Returns the HTTP status code of the response.
	 *
	 * @return The HTTP status code of the response.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the Swagger metadata associated with this return.
	 *
	 * @return A map of return metadata, never <jk>null</jk>.
	 */
	public ObjectMap getApi() {
		return api;
	}

	/**
	 * Returns metadata about the thrown object.
	 *
	 * @return
	 * 	Metadata about the thrown object.
	 * 	<br>Can be <jk>null</jk> if no {@link Response} annotation is associated with the thrown object.
	 */
	public ResponseMeta getResponseMeta() {
		return responseMeta;
	}
}
