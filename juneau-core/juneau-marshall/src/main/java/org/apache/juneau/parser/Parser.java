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
package org.apache.juneau.parser;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.html.*;
import org.apache.juneau.http.*;
import org.apache.juneau.json.*;
import org.apache.juneau.msgpack.*;
import org.apache.juneau.transform.*;
import org.apache.juneau.transforms.*;
import org.apache.juneau.uon.*;
import org.apache.juneau.utils.*;
import org.apache.juneau.xml.*;

/**
 * Parent class for all Juneau parsers.
 *
 * <h5 class='topic'>Valid data conversions</h5>
 *
 * Parsers can parse any parsable POJO types, as specified in the {@doc PojoCategories}.
 *
 * <p>
 * Some examples of conversions are shown below...
 * </p>
 * <table class='styled'>
 * 	<tr>
 * 		<th>Data type</th>
 * 		<th>Class type</th>
 * 		<th>JSON example</th>
 * 		<th>XML example</th>
 * 		<th>Class examples</th>
 * 	</tr>
 * 	<tr>
 * 		<td>object</td>
 * 		<td>Maps, Java beans</td>
 * 		<td class='code'>{name:<js>'John Smith'</js>,age:21}</td>
 * 		<td class='code'><xt>&lt;object&gt;
 * 	&lt;name</xt> <xa>type</xa>=<xs>'string'</xs><xt>&gt;</xt>John Smith<xt>&lt;/name&gt;
 * 	&lt;age</xt> <xa>type</xa>=<xs>'number'</xs><xt>&gt;</xt>21<xt>&lt;/age&gt;
 * &lt;/object&gt;</xt></td>
 * 		<td class='code'>HashMap, TreeMap&lt;String,Integer&gt;</td>
 * 	</tr>
 * 	<tr>
 * 		<td>array</td>
 * 		<td>Collections, Java arrays</td>
 * 		<td class='code'>[1,2,3]</td>
 * 		<td class='code'><xt>&lt;array&gt;
 * 	&lt;number&gt;</xt>1<xt>&lt;/number&gt;
 * 	&lt;number&gt;</xt>2<xt>&lt;/number&gt;
 * 	&lt;number&gt;</xt>3<xt>&lt;/number&gt;
 * &lt;/array&gt;</xt></td>
 * 		<td class='code'>List&lt;Integer&gt;, <jk>int</jk>[], Float[], Set&lt;Person&gt;</td>
 * 	</tr>
 * 	<tr>
 * 		<td>number</td>
 * 		<td>Numbers</td>
 * 		<td class='code'>123</td>
 * 		<td class='code'><xt>&lt;number&gt;</xt>123<xt>&lt;/number&gt;</xt></td>
 * 		<td class='code'>Integer, Long, Float, <jk>int</jk></td>
 * 	</tr>
 * 	<tr>
 * 		<td>boolean</td>
 * 		<td>Booleans</td>
 * 		<td class='code'><jk>true</jk></td>
 * 		<td class='code'><xt>&lt;boolean&gt;</xt>true<xt>&lt;/boolean&gt;</xt></td>
 * 		<td class='code'>Boolean</td>
 * 	</tr>
 * 	<tr>
 * 		<td>string</td>
 * 		<td>CharSequences</td>
 * 		<td class='code'><js>'foobar'</js></td>
 * 		<td class='code'><xt>&lt;string&gt;</xt>foobar<xt>&lt;/string&gt;</xt></td>
 * 		<td class='code'>String, StringBuilder</td>
 * 	</tr>
 * </table>
 *
 * <p>
 * In addition, any class types with {@link PojoSwap PojoSwaps} associated with them on the registered
 * bean context can also be passed in.
 *
 * <p>
 * For example, if the {@link TemporalCalendarSwap} transform is used to generalize {@code Calendar} objects to {@code String}
 * objects.
 * When registered with this parser, you can construct {@code Calendar} objects from {@code Strings} using the
 * following syntax...
 * <p class='bcode w800'>
 * 	Calendar c = parser.parse(<js>"'Sun Mar 03 04:05:06 EST 2001'"</js>, GregorianCalendar.<jk>class</jk>);
 * </p>
 *
 * <p>
 * If <code>Object.<jk>class</jk></code> is specified as the target type, then the parser automatically determines the
 * data types and generates the following object types...
 * <table class='styled'>
 * 	<tr><th>JSON type</th><th>Class type</th></tr>
 * 	<tr><td>object</td><td>{@link ObjectMap}</td></tr>
 * 	<tr><td>array</td><td>{@link ObjectList}</td></tr>
 * 	<tr><td>number</td><td>{@link Number}<br>(depending on length and format, could be {@link Integer},
 * 		{@link Double}, {@link Float}, etc...)</td></tr>
 * 	<tr><td>boolean</td><td>{@link Boolean}</td></tr>
 * 	<tr><td>string</td><td>{@link String}</td></tr>
 * </table>
 */
@ConfigurableContext
public abstract class Parser extends BeanContext {

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "Parser";

	/**
	 * Configuration property:  Auto-close streams.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.autoCloseStreams.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#autoCloseStreams(boolean)}
	 * 			<li class='jm'>{@link ParserBuilder#autoCloseStreams()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, <l>InputStreams</l> and <l>Readers</l> passed into parsers will be closed
	 * after parsing is complete.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a parser using strict mode.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.autoCloseStreams()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>PARSER_autoCloseStreams</jsf>, <jk>true</jk>)
	 * 		.build();
	 *
	 * 	Reader r = <jk>new</jk> FileReader(<js>"/tmp/myfile.json"</js>);
	 * 	MyBean myBean = p.parse(r, MyBean.<jk>class</jk>);
	 *
	 * 	<jsm>assertTrue</jsm>(r.isClosed());
	 * </p>
	 */
	public static final String PARSER_autoCloseStreams = PREFIX + ".autoCloseStreams.b";

	/**
	 * Configuration property:  Debug output lines.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.debugOutputLines.i"</js>
	 * 	<li><b>Data type:</b>  <c>Integer</c>
	 * 	<li><b>Default:</b>  <c>5</c>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#debugOutputLines(int)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * When parse errors occur, this specifies the number of lines of input before and after the
	 * error location to be printed as part of the exception message.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a parser whose exceptions print out 100 lines before and after the parse error location.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.debug()  <jc>// Enable debug mode to capture Reader contents as strings.</jc>
	 * 		.debugOuputLines(100)
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>BEAN_debug</jsf>, <jk>true</jk>)
	 * 		.set(<jsf>PARSER_debugOutputLines</jsf>, 100)
	 * 		.build();
	 *
	 * 	Reader r = <jk>new</jk> FileReader(<js>"/tmp/mybadfile.json"</js>);
	 * 	<jk>try</jk> {
	 * 		p.parse(r, Object.<jk>class</jk>);
	 * 	} <jk>catch</jk> (ParseException e) {
	 * 		System.<jsf>err</jsf>.println(e.getMessage());  <jc>// Will display 200 lines of the output.</jc>
	 * 	}
	 * </p>
	 */
	public static final String PARSER_debugOutputLines = PREFIX + ".debugOutputLines.i";

	/**
	 * Configuration property:  Parser listener.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.listener.c"</js>
	 * 	<li><b>Data type:</b>  <c>Class&lt;? extends ParserListener&gt;</c>
	 * 	<li><b>Default:</b>  <jk>null</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#listener(Class)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Class used to listen for errors and warnings that occur during parsing.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Define our parser listener.</jc>
	 * 	<jc>// Simply captures all unknown bean property events.</jc>
	 * 	<jk>public class</jk> MyParserListener <jk>extends</jk> ParserListener {
	 *
	 * 		<jc>// A simple property to store our events.</jc>
	 * 		<jk>public</jk> List&lt;String&gt; <jf>events</jf> = <jk>new</jk> LinkedList&lt;&gt;();
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public</jk> &lt;T&gt; <jk>void</jk> onUnknownBeanProperty(ParserSession session, ParserPipe pipe, String propertyName, Class&lt;T&gt; beanClass, T bean, <jk>int</jk> line, <jk>int</jk> col) {
	 * 			<jf>events</jf>.add(propertyName + <js>","</js> + line + <js>","</js> + col);
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Create a parser using our listener.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.listener(MyParserListener.<jk>class</jk>)
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>PARSER_listener</jsf>, MyParserListener.<jk>class</jk>)
	 * 		.build();
	 *
	 * 	<jc>// Create a session object.</jc>
	 * 	<jc>// Needed because listeners are created per-session.</jc>
	 * 	<jk>try</jk> (ReaderParserSession s = p.createSession()) {
	 *
	 * 		<jc>// Parse some JSON object.</jc>
	 * 		MyBean myBean = s.parse(<js>"{...}"</js>, MyBean.<jk>class</jk>);
	 *
	 * 		<jc>// Get the listener.</jc>
	 * 		MyParserListener l = s.getListener(MyParserListener.<jk>class</jk>);
	 *
	 * 		<jc>// Dump the results to the console.</jc>
	 * 		SimpleJsonSerializer.<jsf>DEFAULT</jsf>.println(l.<jf>events</jf>);
	 * 	}
	 * </p>
	 */
	public static final String PARSER_listener = PREFIX + ".listener.c";

	/**
	 * Configuration property:  Strict mode.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.strict.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#strict(boolean)}
	 * 			<li class='jm'>{@link ParserBuilder#strict()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, strict mode for the parser is enabled.
	 *
	 * <p>
	 * Strict mode can mean different things for different parsers.
	 *
	 * <table class='styled'>
	 * 	<tr><th>Parser class</th><th>Strict behavior</th></tr>
	 * 	<tr>
	 * 		<td>All reader-based parsers</td>
	 * 		<td>
	 * 			When enabled, throws {@link ParseException ParseExceptions} on malformed charset input.
	 * 			Otherwise, malformed input is ignored.
	 * 		</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@link JsonParser}</td>
	 * 		<td>
	 * 			When enabled, throws exceptions on the following invalid JSON syntax:
	 * 			<ul>
	 * 				<li>Unquoted attributes.
	 * 				<li>Missing attribute values.
	 * 				<li>Concatenated strings.
	 * 				<li>Javascript comments.
	 * 				<li>Numbers and booleans when Strings are expected.
	 * 				<li>Numbers valid in Java but not JSON (e.g. octal notation, etc...)
	 * 			</ul>
	 * 		</td>
	 * 	</tr>
	 * </table>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a parser using strict mode.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.strict()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>PARSER_strict</jsf>, <jk>true</jk>)
	 * 		.build();
	 *
	 * 	<jc>// Use it.</jc>
	 *  	<jk>try</jk> {
	 *  		String json = <js>"{unquotedAttr:'value'}"</js>;
	 * 		MyBean myBean = p.parse(json, MyBean.<jk>class</jk>);
	 *  	} <jk>catch</jk> (ParseException e) {
	 *  		<jsm>assertTrue</jsm>(e.getMessage().contains(<js>"Unquoted attribute detected."</js>);
	 *  	}
	 * </p>
	 */
	public static final String PARSER_strict = PREFIX + ".strict.b";

	/**
	 * Configuration property:  Trim parsed strings.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.trimStrings.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#trimStrings(boolean)}
	 * 			<li class='jm'>{@link ParserBuilder#trimStrings()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, string values will be trimmed of whitespace using {@link String#trim()} before being added to
	 * the POJO.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a parser with trim-strings enabled.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.trimStrings()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>PARSER_trimStrings</jsf>, <jk>true</jk>)
	 * 		.build();
	 *
	 * 	<jc>// Use it.</jc>
	 *  	String json = <js>"{foo:' bar '}"</js>;
	 * 	Map&lt;String,String&gt; m = p.parse(json, HashMap.<jk>class</jk>, String.<jk>class</jk>, String.<jk>class</jk>);
	 * 	<jsm>assertEquals</jsm>(<js>"bar"</js>, m.get(<js>"foo"</js>));
	 * </p>
	 */
	public static final String PARSER_trimStrings = PREFIX + ".trimStrings.b";

	/**
	 * Configuration property:  Unbuffered.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"Parser.unbuffered.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link ParserBuilder#unbuffered(boolean)}
	 * 			<li class='jm'>{@link ParserBuilder#unbuffered()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, don't use internal buffering during parsing.
	 *
	 * <p>
	 * This is useful in cases when you want to parse the same input stream or reader multiple times
	 * because it may contain multiple independent POJOs to parse.
	 * <br>Buffering would cause the parser to read past the current POJO in the stream.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a parser using strict mode.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.unbuffered()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	ReaderParser p = JsonParser.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>PARSER_unbuffered</jsf>, <jk>true</jk>)
	 * 		.build();
	 *
	 * 	<jc>// If you're calling parse on the same input multiple times, use a session instead of the parser directly.</jc>
	 * 	<jc>// It's more efficient because we don't need to recalc the session settings again. </jc>
	 * 	ReaderParserSession s = p.createSession();
	 *
	 * 	<jc>// Read input with multiple POJOs</jc>
	 * 	Reader json = <jk>new</jk> StringReader(<js>"{foo:'bar'}{foo:'baz'}"</js>);
	 * 	MyBean myBean1 = s.parse(json, MyBean.<jk>class</jk>);
	 * 	MyBean myBean2 = s.parse(json, MyBean.<jk>class</jk>);
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		This only allows for multi-input streams for the following parsers:
	 * 		<ul>
	 * 			<li class='jc'>{@link JsonParser}
	 * 			<li class='jc'>{@link UonParser}
	 * 		</ul>
	 * 		It has no effect on the following parsers:
	 * 		<ul>
	 * 			<li class='jc'>{@link MsgPackParser} - It already doesn't use buffering.
	 * 			<li class='jc'>{@link XmlParser}, {@link HtmlParser} - These use StAX which doesn't allow for more than one root element anyway.
	 * 			<li>RDF parsers - These read everything into an internal model before any parsing begins.
	 * 		</ul>
	 * </ul>
	 *
	 * If <jk>true</jk>, don't use internal buffering during parsing.
	 */
	public static final String PARSER_unbuffered = PREFIX + ".unbuffered.b";

	static Parser DEFAULT = new Parser(PropertyStore.create().build()) {
		@Override
		public ParserSession createSession(ParserSessionArgs args) {
			throw new NoSuchMethodError();
		}
	};

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final boolean trimStrings, strict, autoCloseStreams, unbuffered;
	private final int debugOutputLines;
	private final Class<? extends ParserListener> listener;

	/** General parser properties currently set on this parser. */
	private final MediaType[] consumes;

	/**
	 * Constructor.
	 *
	 * @param ps The property store containing all the settings for this object.
	 * @param consumes The list of media types that this parser consumes (e.g. <js>"application/json"</js>).
	 */
	protected Parser(PropertyStore ps, String...consumes) {
		super(ps);

		trimStrings = getBooleanProperty(PARSER_trimStrings, false);
		strict = getBooleanProperty(PARSER_strict, false);
		autoCloseStreams = getBooleanProperty(PARSER_autoCloseStreams, false);
		debugOutputLines = getIntegerProperty(PARSER_debugOutputLines, 5);
		unbuffered = getBooleanProperty(PARSER_unbuffered, false);
		listener = getClassProperty(PARSER_listener, ParserListener.class, null);
		this.consumes = new MediaType[consumes.length];
		for (int i = 0; i < consumes.length; i++) {
			this.consumes[i] = MediaType.forString(consumes[i]);
		}
	}

	@Override /* Context */
	public ParserBuilder builder() {
		return new ParserBuilder(getPropertyStore());
	}

	/**
	 * Instantiates a new clean-slate {@link ParserBuilder} object.
	 *
	 * <p>
	 * This is equivalent to simply calling <code><jk>new</jk> ParserBuilder()</code>.
	 *
	 * <p>
	 * Note that this method creates a builder initialized to all default settings, whereas {@link #builder()} copies
	 * the settings of the object called on.
	 *
	 * @return A new {@link ParserBuilder} object.
	 */
	public static ParserBuilder create() {
		return new ParserBuilder(PropertyStore.DEFAULT);
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Abstract methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns <jk>true</jk> if this parser subclasses from {@link ReaderParser}.
	 *
	 * @return <jk>true</jk> if this parser subclasses from {@link ReaderParser}.
	 */
	public boolean isReaderParser() {
		return true;
	}

	/**
	 * Create the session object that will be passed in to the parse method.
	 *
	 * <p>
	 * It's up to implementers to decide what the session object looks like, although typically it's going to be a
	 * subclass of {@link ParserSession}.
	 *
	 * @param args
	 * 	Runtime arguments.
	 * @return The new session.
	 */
	public abstract ParserSession createSession(ParserSessionArgs args);


	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Parses input into the specified object type.
	 *
	 * <p>
	 * The type can be a simple type (e.g. beans, strings, numbers) or parameterized type (collections/maps).
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	ReaderParser p = JsonParser.<jsf>DEFAULT</jsf>;
	 *
	 * 	<jc>// Parse into a linked-list of strings.</jc>
	 * 	List l = p.parse(json, LinkedList.<jk>class</jk>, String.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a linked-list of beans.</jc>
	 * 	List l = p.parse(json, LinkedList.<jk>class</jk>, MyBean.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a linked-list of linked-lists of strings.</jc>
	 * 	List l = p.parse(json, LinkedList.<jk>class</jk>, LinkedList.<jk>class</jk>, String.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a map of string keys/values.</jc>
	 * 	Map m = p.parse(json, TreeMap.<jk>class</jk>, String.<jk>class</jk>, String.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a map containing string keys and values of lists containing beans.</jc>
	 * 	Map m = p.parse(json, TreeMap.<jk>class</jk>, String.<jk>class</jk>, List.<jk>class</jk>, MyBean.<jk>class</jk>);
	 * </p>
	 *
	 * <p>
	 * <c>Collection</c> classes are assumed to be followed by zero or one objects indicating the element type.
	 *
	 * <p>
	 * <c>Map</c> classes are assumed to be followed by zero or two meta objects indicating the key and value types.
	 *
	 * <p>
	 * The array can be arbitrarily long to indicate arbitrarily complex data structures.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Use the {@link #parse(Object, Class)} method instead if you don't need a parameterized map/collection.
	 * </ul>
	 *
	 * @param <T> The class type of the object to create.
	 * @param input
	 * 	The input.
	 * 	<br>Character-based parsers can handle the following input class types:
	 * 	<ul>
	 * 		<li><jk>null</jk>
	 * 		<li>{@link Reader}
	 * 		<li>{@link CharSequence}
	 * 		<li>{@link InputStream} containing UTF-8 encoded text (or charset defined by
	 * 			{@link ReaderParser#RPARSER_streamCharset} property value).
	 * 		<li><code><jk>byte</jk>[]</code> containing UTF-8 encoded text (or charset defined by
	 * 			{@link ReaderParser#RPARSER_streamCharset} property value).
	 * 		<li>{@link File} containing system encoded text (or charset defined by
	 * 			{@link ReaderParser#RPARSER_fileCharset} property value).
	 * 	</ul>
	 * 	<br>Stream-based parsers can handle the following input class types:
	 * 	<ul>
	 * 		<li><jk>null</jk>
	 * 		<li>{@link InputStream}
	 * 		<li><code><jk>byte</jk>[]</code>
	 * 		<li>{@link File}
	 * 		<li>{@link CharSequence} containing encoded bytes according to the {@link InputStreamParser#ISPARSER_binaryFormat} setting.
	 * 	</ul>
	 * @param type
	 * 	The object type to create.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * @param args
	 * 	The type arguments of the class if it's a collection or map.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * 	<br>Ignored if the main type is not a map or collection.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 * @throws IOException Thrown by underlying stream.
	 * @see BeanSession#getClassMeta(Type,Type...) for argument syntax for maps and collections.
	 */
	public final <T> T parse(Object input, Type type, Type...args) throws ParseException, IOException {
		return createSession().parse(input, type, args);
	}

	/**
	 * Same as {@link #parse(Object, Type, Type...)} but since it's a {@link String} input doesn't throw an {@link IOException}.
	 *
	 * @param <T> The class type of the object being created.
	 * @param input
	 * 	The input.
	 * 	See {@link #parse(Object, Type, Type...)} for details.
	 * @param type
	 * 	The object type to create.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * @param args
	 * 	The type arguments of the class if it's a collection or map.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * 	<br>Ignored if the main type is not a map or collection.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 */
	public final <T> T parse(String input, Type type, Type...args) throws ParseException {
		return createSession().parse(input, type, args);
	}

	/**
	 * Same as {@link #parse(Object, Type, Type...)} except optimized for a non-parameterized class.
	 *
	 * <p>
	 * This is the preferred parse method for simple types since you don't need to cast the results.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	ReaderParser p = JsonParser.<jsf>DEFAULT</jsf>;
	 *
	 * 	<jc>// Parse into a string.</jc>
	 * 	String s = p.parse(json, String.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a bean.</jc>
	 * 	MyBean b = p.parse(json, MyBean.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a bean array.</jc>
	 * 	MyBean[] ba = p.parse(json, MyBean[].<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a linked-list of objects.</jc>
	 * 	List l = p.parse(json, LinkedList.<jk>class</jk>);
	 *
	 * 	<jc>// Parse into a map of object keys/values.</jc>
	 * 	Map m = p.parse(json, TreeMap.<jk>class</jk>);
	 * </p>
	 *
	 * @param <T> The class type of the object being created.
	 * @param input
	 * 	The input.
	 * 	See {@link #parse(Object, Type, Type...)} for details.
	 * @param type The object type to create.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 * @throws IOException Thrown by the underlying stream.
	 */
	public final <T> T parse(Object input, Class<T> type) throws ParseException, IOException {
		return createSession().parse(input, type);
	}

	/**
	 * Same as {@link #parse(Object, Class)} but since it's a {@link String} input doesn't throw an {@link IOException}.
	 *
	 * @param <T> The class type of the object being created.
	 * @param input
	 * 	The input.
	 * 	See {@link #parse(Object, Type, Type...)} for details.
	 * @param type The object type to create.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 */
	public final <T> T parse(String input, Class<T> type) throws ParseException {
		return createSession().parse(input, type);
	}

	/**
	 * Same as {@link #parse(Object, Type, Type...)} except the type has already been converted into a {@link ClassMeta}
	 * object.
	 *
	 * <p>
	 * This is mostly an internal method used by the framework.
	 *
	 * @param <T> The class type of the object being created.
	 * @param input
	 * 	The input.
	 * 	See {@link #parse(Object, Type, Type...)} for details.
	 * @param type The object type to create.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 * @throws IOException Thrown by the underlying stream.
	 */
	public final <T> T parse(Object input, ClassMeta<T> type) throws ParseException, IOException {
		return createSession().parse(input, type);
	}

	/**
	 * Same as {@link #parse(Object, ClassMeta)} but since it's a {@link String} input doesn't throw an {@link IOException}.
	 *
	 * @param <T> The class type of the object being created.
	 * @param input
	 * 	The input.
	 * 	See {@link #parse(Object, Type, Type...)} for details.
	 * @param type The object type to create.
	 * @return The parsed object.
	 * @throws ParseException Malformed input encountered.
	 */
	public final <T> T parse(String input, ClassMeta<T> type) throws ParseException {
		return createSession().parse(input, type);
	}

	@Override /* Context */
	public ParserSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Context */
	public final ParserSessionArgs createDefaultSessionArgs() {
		return new ParserSessionArgs().mediaType(getPrimaryMediaType());
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Optional methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Parses the contents of the specified reader and loads the results into the specified map.
	 *
	 * <p>
	 * Reader must contain something that serializes to a map (such as text containing a JSON object).
	 *
	 * <p>
	 * Used in the following locations:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The various character-based constructors in {@link ObjectMap} (e.g.
	 * 		{@link ObjectMap#ObjectMap(CharSequence,Parser)}).
	 * </ul>
	 *
	 * @param <K> The key class type.
	 * @param <V> The value class type.
	 * @param input The input.  See {@link #parse(Object, ClassMeta)} for supported input types.
	 * @param m The map being loaded.
	 * @param keyType The class type of the keys, or <jk>null</jk> to default to <code>String.<jk>class</jk></code>.
	 * @param valueType The class type of the values, or <jk>null</jk> to default to whatever is being parsed.
	 * @return The same map that was passed in to allow this method to be chained.
	 * @throws ParseException Malformed input encountered.
	 * @throws UnsupportedOperationException If not implemented.
	 */
	public final <K,V> Map<K,V> parseIntoMap(Object input, Map<K,V> m, Type keyType, Type valueType) throws ParseException {
		return createSession().parseIntoMap(input, m, keyType, valueType);
	}

	/**
	 * Parses the contents of the specified reader and loads the results into the specified collection.
	 *
	 * <p>
	 * Used in the following locations:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The various character-based constructors in {@link ObjectList} (e.g.
	 * 		{@link ObjectList#ObjectList(CharSequence,Parser)}.
	 * </ul>
	 *
	 * @param <E> The element class type.
	 * @param input The input.  See {@link #parse(Object, ClassMeta)} for supported input types.
	 * @param c The collection being loaded.
	 * @param elementType The class type of the elements, or <jk>null</jk> to default to whatever is being parsed.
	 * @return The same collection that was passed in to allow this method to be chained.
	 * @throws ParseException Malformed input encountered.
	 * @throws UnsupportedOperationException If not implemented.
	 */
	public final <E> Collection<E> parseIntoCollection(Object input, Collection<E> c, Type elementType) throws ParseException {
		return createSession().parseIntoCollection(input, c, elementType);
	}

	/**
	 * Parses the specified array input with each entry in the object defined by the {@code argTypes}
	 * argument.
	 *
	 * <p>
	 * Used for converting arrays (e.g. <js>"[arg1,arg2,...]"</js>) into an {@code Object[]} that can be passed
	 * to the {@code Method.invoke(target, args)} method.
	 *
	 * <p>
	 * Used in the following locations:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		Used to parse argument strings in the {@link PojoIntrospector#invokeMethod(Method, Reader)} method.
	 * </ul>
	 *
	 * @param input The input.  Subclasses can support different input types.
	 * @param argTypes Specifies the type of objects to create for each entry in the array.
	 * @return An array of parsed objects.
	 * @throws ParseException Malformed input encountered.
	 */
	public final Object[] parseArgs(Object input, Type[] argTypes) throws ParseException {
		if (argTypes == null || argTypes.length == 0)
			return new Object[0];
		return createSession().parseArgs(input, argTypes);
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the media types handled based on the values passed to the <c>consumes</c> constructor parameter.
	 *
	 * @return The list of media types.  Never <jk>null</jk>.
	 */
	public final MediaType[] getMediaTypes() {
		return consumes;
	}

	/**
	 * Returns the first media type handled based on the values passed to the <c>consumes</c> constructor parameter.
	 *
	 * @return The media type.
	 */
	public final MediaType getPrimaryMediaType() {
		return consumes == null || consumes.length == 0 ? null : consumes[0];
	}

	/**
	 * Returns <jk>true</jk> if this parser can handle the specified content type.
	 *
	 * @param contentType The content type to test.
	 * @return <jk>true</jk> if this parser can handle the specified content type.
	 */
	public boolean canHandle(String contentType) {
		if (contentType != null)
			for (MediaType mt : getMediaTypes())
				if (contentType.equals(mt.toString()))
					return true;
		return false;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Configuration property:  Auto-close streams.
	 *
	 * @see #PARSER_autoCloseStreams
	 * @return
	 * 	<jk>true</jk> if <l>InputStreams</l> and <l>Readers</l> passed into parsers will be closed
	 * 	after parsing is complete.
	 */
	protected final boolean isAutoCloseStreams() {
		return autoCloseStreams;
	}

	/**
	 * Configuration property:  Debug output lines.
	 *
	 * @see #PARSER_debugOutputLines
	 * @return
	 * 	The number of lines of input before and after the error location to be printed as part of the exception message.
	 */
	protected final int getDebugOutputLines() {
		return debugOutputLines;
	}

	/**
	 * Configuration property:  Parser listener.
	 *
	 * @see #PARSER_listener
	 * @return
	 * 	Class used to listen for errors and warnings that occur during parsing.
	 */
	protected final Class<? extends ParserListener> getListener() {
		return listener;
	}

	/**
	 * Configuration property:  Strict mode.
	 *
	 * @see #PARSER_strict
	 * @return
	 * 	<jk>true</jk> if strict mode for the parser is enabled.
	 */
	protected final boolean isStrict() {
		return strict;
	}

	/**
	 * Configuration property:  Trim parsed strings.
	 *
	 * @see #PARSER_trimStrings
	 * @return
	 * 	<jk>true</jk> if string values will be trimmed of whitespace using {@link String#trim()} before being added to
	 * 	the POJO.
	 */
	protected final boolean isTrimStrings() {
		return trimStrings;
	}

	/**
	 * Configuration property:  Unbuffered.
	 *
	 * @see #PARSER_unbuffered
	 * @return
	 * 	<jk>true</jk> if parsers don't use internal buffering during parsing.
	 */
	protected final boolean isUnbuffered() {
		return unbuffered;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public ObjectMap toMap() {
		return super.toMap()
			.append("Parser", new DefaultFilteringObjectMap()
				.append("autoCloseStreams", autoCloseStreams)
				.append("debugOutputLines", debugOutputLines)
				.append("listener", listener)
				.append("strict", strict)
				.append("trimStrings", trimStrings)
				.append("unbuffered", unbuffered)
			);
	}
}
