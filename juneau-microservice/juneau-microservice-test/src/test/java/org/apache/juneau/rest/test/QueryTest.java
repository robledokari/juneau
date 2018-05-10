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
package org.apache.juneau.rest.test;

import static org.apache.juneau.rest.testutils.TestUtils.*;

import org.apache.juneau.*;
import org.apache.juneau.rest.client.*;
import org.junit.*;

/**
 * Tests client-side form posts.
 */
public class QueryTest extends RestTestcase {

	private static String URL = "/testQuery";
	RestClient client = TestMicroservice.DEFAULT_CLIENT;

	//====================================================================================================
	// Default values.
	//====================================================================================================

	@Test
	public void defaultQuery() throws Exception {
		assertObjectEquals("{f1:'1',f2:'2',f3:'3'}", client.doGet(URL + "/defaultQuery").getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/defaultQuery").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/defaultQuery").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
	}

	@Test
	public void annotatedQuery() throws Exception {
		assertObjectEquals("{f1:null,f2:null,f3:null}", client.doGet(URL + "/annotatedQuery").getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/annotatedQuery").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/annotatedQuery").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
	}

	@Test
	public void annotatedQueryDefault() throws Exception {
		assertObjectEquals("{f1:'1',f2:'2',f3:'3'}", client.doGet(URL + "/annotatedQueryDefault").getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/annotatedQueryDefault").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/annotatedQueryDefault").query("f1",4).query("f2",5).query("f3",6).getResponse(ObjectMap.class));
	}

	@Test
	public void annotatedAndDefaultQuery() throws Exception {
		assertObjectEquals("{f1:'4',f2:'5',f3:'6'}", client.doGet(URL + "/annotatedAndDefaultQuery").getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'7',f2:'8',f3:'9'}", client.doGet(URL + "/annotatedAndDefaultQuery").query("f1",7).query("f2",8).query("f3",9).getResponse(ObjectMap.class));
		assertObjectEquals("{f1:'7',f2:'8',f3:'9'}", client.doGet(URL + "/annotatedAndDefaultQuery").query("f1",7).query("f2",8).query("f3",9).getResponse(ObjectMap.class));
	}
}
