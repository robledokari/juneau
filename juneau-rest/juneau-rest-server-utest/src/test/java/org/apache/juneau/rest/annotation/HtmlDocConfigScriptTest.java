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

import org.apache.juneau.html.annotation.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.mock2.*;
import org.junit.*;
import org.junit.runners.*;

/**
 * Tests related to @HtmlDoc(script) annotation.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({"serial"})
public class HtmlDocConfigScriptTest {

	//=================================================================================================================
	// Basic tests
	//=================================================================================================================

	@RestResource()
	@HtmlDocConfig(script={"a01a","a01b"})
	public static class A extends BasicRestServlet {
		@RestMethod(path="/a01")
		public Object a01() {
			return "OK";
		}
		@RestMethod(path="/a02")
		@HtmlDocConfig(script={"a02a","a02b"})
		public Object a02() {
			return "OK";
		}
		@RestMethod(path="/a03")
		@HtmlDocConfig(script={"INHERIT","a03a","a03b"})
		public Object a03() {
			return "OK";
		}
		@RestMethod(path="/a04")
		@HtmlDocConfig(script={"a04a","INHERIT","a04b"})
		public Object a04() {
			return "OK";
		}
		@RestMethod(path="/a05")
		@HtmlDocConfig(script={"a05a","a05b","INHERIT"})
		public Object a05() {
			return "OK";
		}
	}
	static MockRest a = MockRest.build(A.class);

	@Test
	public void a01() throws Exception {
		a.get("/a01").accept("text/html").execute().assertBodyContains("<script>a01a\n a01b\n</script>");
	}
	@Test
	public void a02() throws Exception {
		a.get("/a02").accept("text/html").execute().assertBodyContains("<script>a02a\n a02b\n</script>");
	}
	@Test
	public void a03() throws Exception {
		a.get("/a03").accept("text/html").execute().assertBodyContains("<script>a01a\n a01b\n a03a\n a03b\n</script>");
	}
	@Test
	public void a04() throws Exception {
		a.get("/a04").accept("text/html").execute().assertBodyContains("<script>a04a\n a01a\n a01b\n a04b\n</script>");
	}
	@Test
	public void a05() throws Exception {
		a.get("/a05").accept("text/html").execute().assertBodyContains("<script>a05a\n a05b\n a01a\n a01b\n</script>");
	}

	//=================================================================================================================
	// Inheritance
	//=================================================================================================================

	@RestResource()
	@HtmlDocConfig(script={"b01a","b01b"})
	public static class B extends A {
		@RestMethod(path="/b01")
		public Object b01() {
			return "OK";
		}
		@RestMethod(path="/b02")
		@HtmlDocConfig(script={"b02a","b02b"})
		public Object b02() {
			return "OK";
		}
		@RestMethod(path="/b03")
		@HtmlDocConfig(script={"INHERIT","b03a","b03b"})
		public Object b03() {
			return "OK";
		}
		@RestMethod(path="/b04")
		@HtmlDocConfig(script={"b04a","INHERIT","b04b"})
		public Object b04() {
			return "OK";
		}
		@RestMethod(path="/b05")
		@HtmlDocConfig(script={"b05a","b05b","INHERIT"})
		public Object b05() {
			return "OK";
		}
	}
	static MockRest b = MockRest.build(B.class);

	@Test
	public void b01() throws Exception {
		b.get("/b01").accept("text/html").execute().assertBodyContains("<script>b01a\n b01b\n</script>");
	}
	@Test
	public void b02() throws Exception {
		b.get("/b02").accept("text/html").execute().assertBodyContains("<script>b02a\n b02b\n</script>");
	}
	@Test
	public void b03() throws Exception {
		b.get("/b03").accept("text/html").execute().assertBodyContains("<script>b01a\n b01b\n b03a\n b03b\n</script>");
	}
	@Test
	public void b04() throws Exception {
		b.get("/b04").accept("text/html").execute().assertBodyContains("<script>b04a\n b01a\n b01b\n b04b\n</script>");
	}
	@Test
	public void b05() throws Exception {
		b.get("/b05").accept("text/html").execute().assertBodyContains("<script>b05a\n b05b\n b01a\n b01b\n</script>");
	}
}