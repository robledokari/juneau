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

import static org.apache.juneau.rest.exception.NotFound.*;

import java.text.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.rest.*;

/**
 * Exception representing an HTTP 404 (Not Found).
 *
 * <p>
 * The requested resource could not be found but may be available in the future.
 * <br>Subsequent requests by the client are permissible.
 *
 * @deprecated Use {@link org.apache.juneau.http.exception.NotFound}
 */
@Response(code=CODE, description=MESSAGE)
@Deprecated
public class NotFound extends RestException {
	private static final long serialVersionUID = 1L;

	/** HTTP status code */
	public static final int CODE = 404;

	/** Default message */
	public static final String MESSAGE = "Not Found";

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public NotFound(Throwable cause, String msg, Object...args) {
		super(cause, CODE, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 */
	public NotFound(String msg) {
		super(msg);
		setStatus(CODE);
	}

	/**
	 * Constructor.
	 */
	public NotFound() {
		this((Throwable)null, MESSAGE);
	}

	/**
	 * Constructor.
	 *
	 * @param msg The message.  Can be <jk>null</jk>.
	 * @param args Optional {@link MessageFormat}-style arguments in the message.
	 */
	public NotFound(String msg, Object...args) {
		this(null, msg, args);
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause.  Can be <jk>null</jk>.
	 */
	public NotFound(Throwable cause) {
		this(cause, null);
	}
}