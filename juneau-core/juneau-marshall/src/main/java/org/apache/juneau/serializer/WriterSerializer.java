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
package org.apache.juneau.serializer;

import java.nio.charset.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.utils.*;

/**
 * Subclass of {@link Serializer} for character-based serializers.
 */
@ConfigurableContext
public abstract class WriterSerializer extends Serializer {

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "WriterSerializer";

	/**
	 * Configuration property:  File charset.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"WriterSerializer.fileCharset.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>Default:</b>  <js>"DEFAULT"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link WriterSerializerBuilder#fileCharset(Charset)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The character set to use for writing <c>Files</c> to the file system.
	 *
	 * <p>
	 * Used when passing in files to {@link Serializer#serialize(Object, Object)}.
	 *
	 * <p>
	 * <js>"DEFAULT"</js> can be used to indicate the JVM default file system charset.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a serializer that writes UTF-8 files.</jc>
	 * 	WriterSerializer s = JsonSerializer.
	 * 		.<jsm>create</jsm>()
	 * 		.fileCharset(Charset.<jsm>forName</jsm>(<js>"UTF-8"</js>))
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	WriterSerializer s = JsonSerializer.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>WSERIALIZER_fileCharset</jsf>, <js>"UTF-8"</js>)
	 * 		.build();
	 *
	 * 	<jc>// Use it to read a UTF-8 encoded file.</jc>
	 * 	s.serialize(<jk>new</jk> File(<js>"MyBean.txt"</js>), myBean);
	 * </p>
	 */
	public static final String WSERIALIZER_fileCharset = PREFIX + ".fileCharset.s";

	/**
	 * Configuration property:  Maximum indentation.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"WriterSerializer.maxIndent.i"</js>
	 * 	<li><b>Data type:</b>  <c>Integer</c>
	 * 	<li><b>Default:</b>  <c>100</c>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link WriterSerializerBuilder#maxIndent(int)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies the maximum indentation level in the serialized document.
	 *
	 * <p>
	 * This setting does not apply to the RDF serializers.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a serializer that indents a maximum of 20 tabs.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.maxIndent(20)
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>SERIALIZER_maxIndent</jsf>, 20)
	 * 		.build();
	 * </p>
	 */
	public static final String WSERIALIZER_maxIndent = PREFIX + ".maxIndent.i";

	/**
	 * Configuration property:  Quote character.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"WriterSerializer.quoteChar.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>Default:</b>  <js>"\""</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link WriterSerializerBuilder#quoteChar(char)}
	 * 			<li class='jm'>{@link WriterSerializerBuilder#sq()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * This is the character used for quoting attributes and values.
	 *
	 * <p>
	 * This setting does not apply to the RDF serializers.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a serializer that uses single quotes.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.sq()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>WSERIALIZER_quoteChar</jsf>, <js>'\''</js>)
	 * 		.build();
	 * </p>
	 */
	public static final String WSERIALIZER_quoteChar = PREFIX + ".quoteChar.s";

	/**
	 * Configuration property:  Output stream charset.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"WriterSerializer.streamCharset.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>Default:</b>  <js>"UTF-8"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link WriterSerializerBuilder#streamCharset(Charset)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The character set to use when writing to <c>OutputStreams</c>.
	 *
	 * <p>
	 * Used when passing in output streams and byte arrays to {@link WriterSerializer#serialize(Object, Object)}.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a serializer that writes UTF-8 files.</jc>
	 * 	WriterSerializer s = JsonSerializer.
	 * 		.<jsm>create</jsm>()
	 * 		.streamCharset(Charset.<jsm>forName</jsm>(<js>"UTF-8"</js>))
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	WriterSerializer s = JsonSerializer.
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>WSERIALIZER_streamCharset</jsf>, <js>"UTF-8"</js>)
	 * 		.build();
	 *
	 * 	<jc>// Use it to write to a UTF-8 encoded output stream.</jc>
	 * 	s.serializer(<jk>new</jk> FileOutputStreamStream(<js>"MyBean.txt"</js>), myBean);
	 * </p>
	 */
	public static final String WSERIALIZER_streamCharset = PREFIX + ".streamCharset.s";

	/**
	 * Configuration property:  Use whitespace.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"WriterSerializer.useWhitespace.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>true</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link WriterSerializerBuilder#useWhitespace(boolean)}
	 * 			<li class='jm'>{@link WriterSerializerBuilder#useWhitespace()}
	 * 			<li class='jm'>{@link WriterSerializerBuilder#ws()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, whitespace is added to the output to improve readability.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a serializer with whitespace enabled.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.ws()
	 * 		.build();
	 *
	 * 	<jc>// Same, but use property.</jc>
	 * 	WriterSerializer s = JsonSerializer
	 * 		.<jsm>create</jsm>()
	 * 		.set(<jsf>WSERIALIZER_useWhitespace</jsf>, <jk>true</jk>)
	 * 		.build();
	 *
	 * 	<jc>// Produces "\{\n\t'foo': 'bar'\n\}\n"</jc>
	 * 	String json = s.serialize(<jk>new</jk> MyBean());
	 * </p>
	 */
	public static final String WSERIALIZER_useWhitespace = PREFIX + ".useWhitespace.b";

	static final WriterSerializer DEFAULT = new WriterSerializer(PropertyStore.create().build(), "", "") {
		@Override
		public WriterSerializerSession createSession(SerializerSessionArgs args) {
			throw new NoSuchMethodError();
		}
	};

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final Charset fileCharset;
	private final int maxIndent;
	private final char quoteChar;
	private final Charset streamCharset;
	private final boolean useWhitespace;

	/**
	 * Constructor.
	 *
	 * @param ps
	 * 	The property store containing all the settings for this object.
	 * @param produces
	 * 	The media type that this serializer produces.
	 * @param accept
	 * 	The accept media types that the serializer can handle.
	 * 	<p>
	 * 	Can contain meta-characters per the <c>media-type</c> specification of {@doc RFC2616.section14.1}
	 * 	<p>
	 * 	If empty, then assumes the only media type supported is <c>produces</c>.
	 * 	<p>
	 * 	For example, if this serializer produces <js>"application/json"</js> but should handle media types of
	 * 	<js>"application/json"</js> and <js>"text/json"</js>, then the arguments should be:
	 * 	<p class='bcode w800'>
	 * 	<jk>super</jk>(ps, <js>"application/json"</js>, <js>"application/json,text/json"</js>);
	 * 	</p>
	 * 	<br>...or...
	 * 	<p class='bcode w800'>
	 * 	<jk>super</jk>(ps, <js>"application/json"</js>, <js>"*&#8203;/json"</js>);
	 * 	</p>
	 * <p>
	 * The accept value can also contain q-values.
	 */
	protected WriterSerializer(PropertyStore ps, String produces, String accept) {
		super(ps, produces, accept);

		maxIndent = getIntegerProperty(WSERIALIZER_maxIndent, 100);
		quoteChar = getStringProperty(WSERIALIZER_quoteChar, "\"").charAt(0);
		streamCharset = getProperty(WSERIALIZER_streamCharset, Charset.class, IOUtils.UTF8);
		fileCharset = getProperty(WSERIALIZER_fileCharset, Charset.class, Charset.defaultCharset());
		useWhitespace = getBooleanProperty(WSERIALIZER_useWhitespace, false);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Abstract methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* SerializerSession */
	public abstract WriterSerializerSession createSession(SerializerSessionArgs args);

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public WriterSerializerSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Serializer */
	public final boolean isWriterSerializer() {
		return true;
	}

	/**
	 * Convenience method for serializing an object to a <c>String</c>.
	 *
	 * @param o The object to serialize.
	 * @return The output serialized to a string.
	 * @throws SerializeException If a problem occurred trying to convert the output.
	 */
	@Override /* Serializer */
	public final String serialize(Object o) throws SerializeException {
		return createSession(createDefaultSessionArgs()).serialize(o);
	}

	/**
	 * Identical to {@link #serialize(Object)} except throws a {@link RuntimeException} instead of a {@link SerializeException}.
	 *
	 * <p>
	 * This is typically good enough for debugging purposes.
	 *
	 * @param o The object to serialize.
	 * @return The serialized object.
	 */
	public final String toString(Object o) {
		try {
			return serialize(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Wraps the specified object inside a {@link StringObject}.
	 *
	 * @param o The object to wrap.
	 * @return The wrapped object.
	 */
	public final StringObject toStringObject(Object o) {
		return new StringObject(this, o);
	}

	/**
	 * Convenience method for serializing an object and sending it to STDOUT.
	 *
	 * @param o The object to serialize.
	 * @return This object (for method chaining).
	 */
	public final WriterSerializer println(Object o) {
		System.out.println(toString(o));  // NOT DEBUG
		return this;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Configuration property:  File charset.
	 *
	 * @see #WSERIALIZER_fileCharset
	 * @return
	 * 	The character set to use when writing to <c>Files</c> on the file system.
	 */
	protected final Charset getFileCharset() {
		return fileCharset;
	}

	/**
	 * Configuration property:  Maximum indentation.
	 *
	 * @see #WSERIALIZER_maxIndent
	 * @return
	 * 	The maximum indentation level in the serialized document.
	 */
	protected final int getMaxIndent() {
		return maxIndent;
	}

	/**
	 * Configuration property:  Quote character.
	 *
	 * @see #WSERIALIZER_quoteChar
	 * @return
	 * 	The character used for quoting attributes and values.
	 */
	protected final char getQuoteChar() {
		return quoteChar;
	}

	/**
	 * Configuration property:  Output stream charset.
	 *
	 * @see #WSERIALIZER_streamCharset
	 * @return
	 * 	The character set to use when writing to <c>OutputStreams</c> and byte arrays.
	 */
	protected final Charset getStreamCharset() {
		return streamCharset;
	}

	/**
	 * Configuration property:  Trim strings.
	 *
	 * @see #WSERIALIZER_useWhitespace
	 * @return
	 * 	If <jk>true</jk>, whitespace is added to the output to improve readability.
	 */
	protected final boolean isUseWhitespace() {
		return useWhitespace;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public ObjectMap toMap() {
		return super.toMap()
			.append("WriterSerializer", new DefaultFilteringObjectMap()
				.append("fileCharset", fileCharset)
				.append("maxIndent", maxIndent)
				.append("quoteChar", quoteChar)
				.append("streamCharset", streamCharset)
				.append("useWhitespace", useWhitespace)
			);
	}
}
