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

import static org.apache.juneau.rest.exception.ExpectationFailed.*;

import java.text.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.rest.*;

/**
 * Exception representing an HTTP 417 (Expectation Failed).
 *
 * <p>
 * The server cannot meet the requirements of the Expect request-header field.
 *
 * @deprecated Use {@link org.apache.juneau.http.exception.ExpectationFailed}
 */
@Response(code=CODE, description=MESSAGE)
@Deprecated
public class ExpectationFailed extends RestException {
	private static final long serialVersionUID = 1L;

	/** HTTP status code */
	public static final int CODE = 417;

	/** Default message */
	public static final String MESSAGE = "Expectation Failed";

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public ExpectationFailed(Throwable cause, String msg, Object...args) {
		super(cause, CODE, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 */
	public ExpectationFailed(String msg) {
		super(msg);
		setStatus(CODE);
	}

	/**
	 * Constructor.
	 */
	public ExpectationFailed() {
		this((Throwable)null, MESSAGE);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public ExpectationFailed(String msg, Object...args) {
		this(null, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 */
	public ExpectationFailed(Throwable cause) {
		this(cause, null);
	}
}