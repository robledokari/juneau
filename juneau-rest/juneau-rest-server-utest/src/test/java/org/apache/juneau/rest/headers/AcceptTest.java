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
package org.apache.juneau.rest.headers;

import static org.apache.juneau.http.HttpMethodName.*;

import java.io.IOException;

import org.apache.juneau.*;
import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.mock2.*;
import org.apache.juneau.serializer.*;
import org.junit.*;
import org.junit.runners.*;

/**
 * Tests behavior related to the Accept header.
 */
@SuppressWarnings({})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AcceptTest {

	//=================================================================================================================
	// Setup classes
	//=================================================================================================================

	public static class DummySerializer extends WriterSerializer {
		String name;
		DummySerializer(PropertyStore ps, String name, String produces) {
			super(ps, produces, null);
			this.name = name;
		}
		@Override /* Serializer */
		public WriterSerializerSession createSession(SerializerSessionArgs args) {
			return new WriterSerializerSession(args) {
				@Override /* SerializerSession */
				protected void doSerialize(SerializerPipe out, Object o) throws IOException, SerializeException {
					out.getWriter().write(name);
				}
			};
		}
	}

	public static class S1 extends DummySerializer { public S1(PropertyStore ps) {super(ps, "s1", "text/s1");}}
	public static class S2 extends DummySerializer { public S2(PropertyStore ps) {super(ps, "s2", "text/s2");}}
	public static class S3 extends DummySerializer { public S3(PropertyStore ps) {super(ps, "s3", "text/s3");}}

	//=================================================================================================================
	// Test that default Accept headers on servlet annotation are picked up.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 "},
		serializers={S1.class,S2.class}
	)
	public static class A {
		@RestMethod
		public String putA01(@Body String in) {
			return in;
		}
	}
	private static MockRest a = MockRest.build(A.class, null);

	@Test
	public void a01_defaultHeadersOnServletAnnotation_valid() throws Exception {
		a.put("/a01", null).execute().assertBody("s2");
		a.put("/a01", null).accept("text/s1").execute().assertBody("s1");
		a.put("/a01", null).accept("text/s2").execute().assertBody("s2");
	}

	@Test
	public void a02_defaultHeadersOnServletAnnotation_invalid() throws Exception {
		a.put("/a01?noTrace=true", null).accept("text/s3").execute().assertStatus(406).assertBodyContains("Unsupported media-type in request header 'Accept': 'text/s3'");
	}

	//=================================================================================================================
	// Test that default Accept headers on servlet annotation are picked up
	// when @RestMethod.parsers/serializers annotations are used.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 "},
		serializers={S1.class,S2.class}
	)
	public static class B {
		@RestMethod(name=PUT, serializers=S3.class)
		public String b(@Body String in) {
			return in;
		}
	}
	private static MockRest b = MockRest.build(B.class, null);

	@Test
	public void b01_restMethodWithParsersSerializers_valid() throws Exception {
		b.put("/b", null).accept("text/s3").execute().assertBody("s3");
	}

	@Test
	public void b02_restMethodWithParsersSerializers_invalid() throws Exception {
		b.put("/b?noTrace=true", null).accept("text/s4").execute()
			.assertStatus(406)
			.assertBodyContains(
				"Unsupported media-type in request header 'Accept': 'text/s4'",
				"Supported media-types: ['text/s3']"
			);
	}

	//=================================================================================================================
	// Test that default Accept headers on servlet annotation are picked up
	// when @RestMethod.addParsers/addSerializers annotations are used.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 "},
		serializers={S1.class,S2.class}
	)
	public static class C {
		@RestMethod(name=PUT, serializers={S3.class,Inherit.class})
		public String c(@Body String in) {
			return in;
		}
	}
	private static MockRest c = MockRest.build(C.class, null);

	@Test
	public void c01_restMethodAddParsersSerializersInherit() throws Exception {
		c.put("/c", null).execute().assertBody("s2");
		c.put("/c", null).accept("text/s1").execute().assertBody("s1");
		c.put("/c", null).accept("text/s2").execute().assertBody("s2");
		c.put("/c", null).accept("text/s3").execute().assertBody("s3");
	}

	@Test
	public void c02_restMethodAddParsersSerializersInherit_invalid() throws Exception {
		c.put("/c?noTrace=true", null).accept("text/s4").execute()
			.assertStatus(406)
			.assertBodyContains(
				"Unsupported media-type in request header 'Accept': 'text/s4'",
				"Supported media-types: ['text/s3','text/s1','text/s2']"
			);
	}

	//=================================================================================================================
	// Various Accept incantations.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 "},
		serializers={S1.class,S2.class}
	)
	public static class D {
		@RestMethod(name=PUT)
		public String d(@Body String in) {
			return in;
		}
	}
	private static MockRest d = MockRest.build(D.class, null);

	@Test
	public void d01_accept_valid() throws Exception {
		// "*/*" should match the first serializer, not the default serializer.
		d.put("/d", null).accept("*/*").execute().assertBody("s1");
		// "text/*" should match the first serializer, not the default serializer.
		d.put("/d", null).accept("text/*").execute().assertBody("s1");
		d.put("/d", null).accept("bad/*,text/*").execute().assertBody("s1");
		d.put("/d", null).accept("text/*,bad/*").execute().assertBody("s1");
		d.put("/d", null).accept("text/s1;q=0.5,text/s2").execute().assertBody("s2");
		d.put("/d", null).accept("text/s1,text/s2;q=0.5").execute().assertBody("s1");
	}
	@Test
	public void d02_accept_invalid() throws Exception {
		d.put("/d?noTrace=true", null).accept("bad/*").execute()
			.assertStatus(406)
			.assertBodyContains(
				"Unsupported media-type in request header 'Accept': 'bad/*'",
				"Supported media-types: ['text/s1','text/s2']"
			);
	}

	//=================================================================================================================
	// Test that default Accept headers on method annotation are picked up
	// when @RestMethod.parsers/serializers annotations are used.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 "},
		serializers={S1.class,S2.class}
	)
	public static class E {
		@RestMethod(name=PUT, defaultRequestHeaders={"Accept: text/s3"}, serializers=S3.class)
		public String d(@Body String in) {
			return in;
		}
	}
	private static MockRest e = MockRest.build(E.class, null);

	@Test
	public void e01_restMethodParserSerializerAnnotations_valid() throws Exception {
		e.put("/d", null).execute().assertBody("s3");
		e.put("/d", null).accept("text/s3").execute().assertBody("s3");
	}
	@Test
	public void e02_restMethodParserSerializerAnnotations_invalid() throws Exception {
		e.put("/d?noTrace=true", null).accept("text/s1").execute()
			.assertStatus(406)
			.assertBodyContains(
				"Unsupported media-type in request header 'Accept': 'text/s1'",
				"Supported media-types: ['text/s3']"
			);
		e.put("/d?noTrace=true", null).accept("text/s2").execute()
			.assertStatus(406)
			.assertBodyContains(
				"Unsupported media-type in request header 'Accept': 'text/s2'",
				"Supported media-types: ['text/s3']"
			);
	}

	//=================================================================================================================
	// Test that default Accept headers on method annotation are picked up
	// 	when @RestMethod.addParsers/addSerializers annotations are used.
	//=================================================================================================================

	@RestResource(
		defaultRequestHeaders={" Accept : text/s2 ",},
		serializers={S1.class,S2.class}
	)
	public static class F {
		@RestMethod(name=PUT, defaultRequestHeaders={"Accept: text/s3"}, serializers={Inherit.class, S3.class})
		public String f(@Body String in) {
			return in;
		}
	}
	private static MockRest f = MockRest.build(F.class, null);

	@Test
	public void f01_restMethodAddParsersSerializersAnnotations_valid() throws Exception {
		f.put("/f", null).execute().assertBody("s3");
		f.put("/f", null).accept("text/s1").execute().assertBody("s1");
		f.put("/f", null).accept("text/s2").execute().assertBody("s2");
		f.put("/f", null).accept("text/s3").execute().assertBody("s3");
	}
}
