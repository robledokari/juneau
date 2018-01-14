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
package org.apache.juneau.svl.vars;

import org.apache.juneau.svl.*;

/**
 * A basic variable resolver that returns the first non-null value.
 * 
 * <p>
 * The format for this var is <js>"$CR{arg1[,arg2...]}"</js>.
 * 
 * <p>
 * The difference between {@link CoalesceVar} and {@link CoalesceAndRecurseVar} is that the first will not resolve
 * inner variables nor recursively resolve variables, and the second will.
 * Use {@link CoalesceVar} when resolving user-input.
 */
public class CoalesceAndRecurseVar extends MultipartResolvingVar {

	/** The name of this variable. */
	public static final String NAME = "CR";

	/**
	 * Constructor.
	 */
	public CoalesceAndRecurseVar() {
		super(NAME);
	}

	@Override
	public String resolve(VarResolverSession session, String arg) throws Exception {
		return arg;
	}
}
