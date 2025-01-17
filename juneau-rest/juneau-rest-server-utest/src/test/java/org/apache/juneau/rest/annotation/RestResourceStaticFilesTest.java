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
package org.apache.juneau.rest.annotation;

import org.apache.juneau.rest.mock2.*;
import org.junit.*;
import org.junit.runners.*;

/**
 * Tests that validate the behavior of @RestResource(staticFiles).
 */
@SuppressWarnings({})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestResourceStaticFilesTest {

	//------------------------------------------------------------------------------------------------------------------
	// Basic tests
	//------------------------------------------------------------------------------------------------------------------

	@RestResource(staticFiles={"xdocs:xdocs","xdocs2:xdocs2:{Foo:'Bar'}"})
	public static class A1 {
		@RestMethod
		public String a01() {
			return null;
		}
	}
	static MockRest a1 = MockRest.build(A1.class);

	@Test
	public void a01a() throws Exception {
		a1.get("/xdocs/test.txt").execute().assertBodyContains("OK-1");
		a1.get("/xdocs/xsubdocs/test.txt").execute().assertBodyContains("OK-2");
	}
	@Test
	public void a01b_preventPathTraversals() throws Exception {
		a1.get("/xdocs/xsubdocs/../test.txt?noTrace=true").execute().assertStatus(404);
		a1.get("/xdocs/xsubdocs/%2E%2E/test.txt?noTrace=true").execute().assertStatus(404);
	}

	@RestResource(staticFiles={"xdocs2:xdocs2:{Foo:'Bar',Baz:'Qux'},xdocs:xdocs"})
	public static class A2 {
		@RestMethod
		public String a02() {
			return null;
		}
	}
	static MockRest a2 = MockRest.build(A1.class);

	@Test
	public void a02a() throws Exception {
		a1.get("/xdocs/test.txt").execute().assertBodyContains("OK-1");
		a1.get("/xdocs/xsubdocs/test.txt").execute().assertBodyContains("OK-2");
	}
	@Test
	public void a02b_preventPathTraversals() throws Exception {
		a1.get("/xdocs/xsubdocs/../test.txt?noTrace=true").execute().assertStatus(404);
		a1.get("/xdocs/xsubdocs/%2E%2E/test.txt?noTrace=true").execute().assertStatus(404);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Static files with response headers.
	//------------------------------------------------------------------------------------------------------------------

	@RestResource(staticFiles={"xdocs:xdocs:{Foo:'Bar',Baz:'Qux'}"})
	public static class B {
		@RestMethod
		public String b01() {
			return null;
		}
	}
	static MockRest b = MockRest.build(B.class);

	@Test
	public void b01() throws Exception {
		b.get("/xdocs/test.txt").execute().assertHeader("Foo","Bar").assertHeader("Baz","Qux").assertBodyContains("OK-1");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Class hierarchy
	//------------------------------------------------------------------------------------------------------------------

	@RestResource(staticFiles={"xdocs:xdocs"})
	public static class C1 {
		@RestMethod
		public String c01() {
			return null;
		}
	}

	@RestResource(staticFiles={"xdocs:/xdocs"})
	public static class C2 extends C1 {
		@RestMethod
		public String c02() {
			return null;
		}
	}

	static MockRest c1 = MockRest.build(C1.class);
	static MockRest c2 = MockRest.build(C2.class);

	@Test
	public void c01() throws Exception {
		// Should resolve to relative xdocs folder.
		c1.get("/xdocs/test.txt").execute().assertBodyContains("OK-1");
		c1.get("/xdocs/xsubdocs/test.txt").execute().assertBodyContains("OK-2");

		// Should be overridden to absolute xdocs folder.
		c2.get("/xdocs/test.txt").execute().assertBodyContains("OK-3");
		c2.get("/xdocs/xsubdocs/test.txt").execute().assertBodyContains("OK-4");

		// Should pick up from file system.
		c1.get("/xdocs/test2.txt").execute().assertBodyContains("OK-5");
		c2.get("/xdocs/test2.txt").execute().assertBodyContains("OK-5");
		c1.get("/xdocs/xsubdocs/test2.txt").execute().assertBodyContains("OK-6");
		c2.get("/xdocs/xsubdocs/test2.txt").execute().assertBodyContains("OK-6");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Overridden patterns
	//------------------------------------------------------------------------------------------------------------------

	@RestResource(staticFiles={"xdocs:/xdocs,xdocs:xdocs"})
	public static class D {
		@RestMethod
		public String d01() {
			return null;
		}
	}

	static MockRest d = MockRest.build(D.class);

	@Test
	public void d01() throws Exception {
		// Should be overridden to absolute xdocs folder.
		d.get("/xdocs/test.txt").execute().assertBodyContains("OK-3");
		d.get("/xdocs/xsubdocs/test.txt").execute().assertBodyContains("OK-4");

		// Should pick up from file system.
		d.get("/xdocs/test2.txt").execute().assertBodyContains("OK-5");
		d.get("/xdocs/xsubdocs/test2.txt").execute().assertBodyContains("OK-6");
	}

}
