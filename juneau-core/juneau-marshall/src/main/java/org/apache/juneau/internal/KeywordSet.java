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
package org.apache.juneau.internal;

import java.util.*;

/**
 * Stores a set of language keywords for quick lookup.
 */
public final class KeywordSet {
	final String[] store;

	/**
	 * Constructor.
	 * 
	 * @param keywords The list of keywords.
	 */
	public KeywordSet(String... keywords) {
		this.store = keywords;
		Arrays.sort(store);
	}

	/**
	 * Returns <jk>true</jk> if the specified string exists in this store.
	 * 
	 * @param s The string to check.
	 * @return <jk>true</jk> if the specified string exists in this store.
	 */
	public boolean contains(String s) {
		if (s == null || s.length() < 2)
			return false;
		return Arrays.binarySearch(store, s) >= 0;
	}
}
