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
package org.apache.juneau.msgpack;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.serializer.*;

/**
 * Serializes POJO models to MessagePack.
 *
 * <h5 class='section'>Media types:</h5>
 *
 * Handles <c>Accept</c> types:  <bc>octal/msgpack</bc>
 * <p>
 * Produces <c>Content-Type</c> types: <bc>octal/msgpack</bc>
 */
@ConfigurableContext
public class MsgPackSerializer extends OutputStreamSerializer {

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "MsgPackSerializer";

	/**
	 * Configuration property:  Add <js>"_type"</js> properties when needed.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul>
	 * 	<li><b>Name:</b>  <js>"MsgPackSerializer.addBeanTypes.b"</js>
	 * 	<li><b>Data type:</b>  <c>Boolean</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link MsgPackSerializerBuilder#addBeanTypes(boolean)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * If <jk>true</jk>, then <js>"_type"</js> properties will be added to beans if their type cannot be inferred
	 * through reflection.
	 *
	 * <p>
	 * When present, this value overrides the {@link #SERIALIZER_addBeanTypes} setting and is
	 * provided to customize the behavior of specific serializers in a {@link SerializerGroup}.
	 */
	public static final String MSGPACK_addBeanTypes = PREFIX + ".addBeanTypes.b";


	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	/** Default serializer, all default settings.*/
	public static final MsgPackSerializer DEFAULT = new MsgPackSerializer(PropertyStore.DEFAULT);

	/** Default serializer, all default settings, spaced-hex string output.*/
	public static final MsgPackSerializer DEFAULT_SPACED_HEX = new SpacedHex(PropertyStore.DEFAULT);

	/** Default serializer, all default settings, spaced-hex string output.*/
	public static final MsgPackSerializer DEFAULT_BASE64 = new Base64(PropertyStore.DEFAULT);

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined subclasses
	//-------------------------------------------------------------------------------------------------------------------

	/** Default serializer, spaced-hex string output. */
	public static class SpacedHex extends MsgPackSerializer {

		/**
		 * Constructor.
		 *
		 * @param ps The property store containing all the settings for this object.
		 */
		public SpacedHex(PropertyStore ps) {
			super(
				ps.builder().set(OSSERIALIZER_binaryFormat, BinaryFormat.SPACED_HEX).build()
			);
		}
	}

	/** Default serializer, BASE64 string output. */
	public static class Base64 extends MsgPackSerializer {

		/**
		 * Constructor.
		 *
		 * @param ps The property store containing all the settings for this object.
		 */
		public Base64(PropertyStore ps) {
			super(
				ps.builder().set(OSSERIALIZER_binaryFormat, BinaryFormat.BASE64).build()
			);
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final boolean
		addBeanTypes;

	/**
	 * Constructor.
	 *
	 * @param ps The property store containing all the settings for this object.
	 */
	public MsgPackSerializer(PropertyStore ps) {
		super(ps, "octal/msgpack", null);
		this.addBeanTypes = getBooleanProperty(MSGPACK_addBeanTypes, getBooleanProperty(SERIALIZER_addBeanTypes, false));
	}

	@Override /* Context */
	public MsgPackSerializerBuilder builder() {
		return new MsgPackSerializerBuilder(getPropertyStore());
	}

	/**
	 * Instantiates a new clean-slate {@link MsgPackSerializerBuilder} object.
	 *
	 * <p>
	 * This is equivalent to simply calling <code><jk>new</jk> MsgPackSerializerBuilder()</code>.
	 *
	 * <p>
	 * Note that this method creates a builder initialized to all default settings, whereas {@link #builder()} copies
	 * the settings of the object called on.
	 *
	 * @return A new {@link MsgPackSerializerBuilder} object.
	 */
	public static MsgPackSerializerBuilder create() {
		return new MsgPackSerializerBuilder();
	}

	@Override /* Context */
	public MsgPackSerializerSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Serializer */
	public MsgPackSerializerSession createSession(SerializerSessionArgs args) {
		return new MsgPackSerializerSession(this, args);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------------------------------------------------------

	@Override
	protected final boolean isAddBeanTypes() {
		return addBeanTypes;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public ObjectMap toMap() {
		return super.toMap()
			.append("MsgPackSerializer", new DefaultFilteringObjectMap()
				.append("addBeanTypes", addBeanTypes)
			);
	}
}
