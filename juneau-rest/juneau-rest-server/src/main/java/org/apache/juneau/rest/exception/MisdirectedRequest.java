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
package org.apache.juneau.rest.exception;

import static org.apache.juneau.rest.exception.MisdirectedRequest.*;

import java.text.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.rest.*;

/**
 * Exception representing an HTTP 421 (Misdirected Request).
 *
 * <p>
 * The request was directed at a server that is not able to produce a response (for example because of connection reuse).
 *
 * @deprecated Use {@link org.apache.juneau.http.exception.MisdirectedRequest}
 */
@Response(code=CODE, description=MESSAGE)
@Deprecated
public class MisdirectedRequest extends RestException {
	private static final long serialVersionUID = 1L;

	/** HTTP status code */
	public static final int CODE = 421;

	/** Default message */
	public static final String MESSAGE = "Misdirected Request";

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public MisdirectedRequest(Throwable cause, String msg, Object...args) {
		super(cause, CODE, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 */
	public MisdirectedRequest(String msg) {
		super(msg);
		setStatus(CODE);
	}

	/**
	 * Constructor.
	 */
	public MisdirectedRequest() {
		this((Throwable)null, MESSAGE);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public MisdirectedRequest(String msg, Object...args) {
		this(null, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 */
	public MisdirectedRequest(Throwable cause) {
		this(cause, null);
	}
}