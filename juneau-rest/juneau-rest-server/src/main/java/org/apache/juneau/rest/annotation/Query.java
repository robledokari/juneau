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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import org.apache.juneau.httppart.*;
import org.apache.juneau.rest.*;

/**
 * Identical to {@link FormData @FormData}, but only retrieves the parameter from the URL string, not URL-encoded form
 * posts.
 * 
 * <p>
 * Unlike {@link FormData @FormData}, using this annotation does not result in the servlet reading the contents of
 * URL-encoded form posts.
 * Therefore, this annotation can be used in conjunction with the {@link Body @Body} annotation or
 * {@link RestRequest#getBody()} method for <code>application/x-www-form-urlencoded POST</code> calls.
 * 
 * <h5 class='section'>Example:</h5>
 * <p class='bcode'>
 * 	<ja>@RestMethod</ja>(name=<jsf>GET</jsf>)
 * 	<jk>public void</jk> doGet(RestRequest req, RestResponse res,
 * 				<ja>@Query</ja>(<js>"p1"</js>) <jk>int</jk> p1, <ja>@Query</ja>(<js>"p2"</js>) String p2, <ja>@Query</ja>(<js>"p3"</js>) UUID p3) {
 * 		...
 * 	}
 * </p>
 * 
 * <p>
 * This is functionally equivalent to the following code...
 * <p class='bcode'>
 * 	<ja>@RestMethod</ja>(name=<jsf>GET</jsf>)
 * 	<jk>public void</jk> doGet(RestRequest req, RestResponse res) {
 * 		<jk>int</jk> p1 = req.getQueryParameter(<jk>int</jk>.<jk>class</jk>, <js>"p1"</js>, 0);
 * 		String p2 = req.getQueryParameter(String.<jk>class</jk>, <js>"p2"</js>);
 * 		UUID p3 = req.getQueryParameter(UUID.<jk>class</jk>, <js>"p3"</js>);
 * 		...
 * 	}
 * </p>
 * 
 * <h5 class='section'>See Also:</h5>
 * <ul>
 * 	<li class='link'><a class="doclink" href="../../../../../overview-summary.html#juneau-rest-server.Query">Overview &gt; juneau-rest-server &gt; @Query</a>
 * 	<li class='link'><a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Swagger Specification &gt; Parameter Object</a>
 * </ul>
 */
@Documented
@Target({PARAMETER,TYPE})
@Retention(RUNTIME)
@Inherited
public @interface Query {

	/**
	 * Specify <jk>true</jk> if using multi-part parameters to represent collections and arrays.
	 * 
	 * <p>
	 * Normally, we expect single parameters to be specified in UON notation for representing collections of values
	 * (e.g. <js>"&amp;key=(1,2,3)"</js>.
	 * This annotation allows the use of multi-part parameters to represent collections
	 * (e.g. <js>"&amp;key=1&amp;key=2&amp;key=3"</js>.
	 * 
	 * <p>
	 * This setting should only be applied to Java parameters of type array or Collection.
	 */
	boolean multipart() default false;

	/**
	 * URL query parameter name.
	 * 
	 * Required. The name of the parameter. Parameter names are case sensitive.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain-text.
	 * </ul>
	 */
	String name() default "";

	/**
	 * A synonym for {@link #name()}.
	 * 
	 * <p>
	 * Allows you to use shortened notation if you're only specifying the name.
	 * 
	 * <p>
	 * The following are completely equivalent ways of defining the existence of a query entry:
	 * <p class='bcode w800'>
	 * 	<jk>public</jk> Order placeOrder(<jk>@Query</jk>(name=<js>"petId"</js>) <jk>long</jk> petId) {...}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jk>public</jk> Order placeOrder(<jk>@Query</jk>(<js>"petId"</js>) <jk>long</jk> petId) {...}
	 * </p>
	 */
	String value() default "";

	/**
	 * Specifies the {@link HttpPartParser} class used for parsing values from strings.
	 * 
	 * <p>
	 * The default value for this parser is inherited from the servlet/method which defaults to {@link UonPartParser}.
	 * <br>You can use {@link SimplePartParser} to parse POJOs that are directly convertible from <code>Strings</code>.
	 */
	Class<? extends HttpPartParser> parser() default HttpPartParser.Null.class;

	/**
	 * <mk>description</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] description() default {};
	
	/**
	 * <mk>required</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is boolean.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String required() default "";
	
	/**
	 * <mk>type</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 	<li>
	 * 		The possible values are:
	 * 		<ul>
	 * 			<li><js>"string"</js>
	 * 			<li><js>"number"</js>
	 * 			<li><js>"integer"</js>
	 * 			<li><js>"boolean"</js>
	 * 			<li><js>"array"</js>
	 * 			<li><js>"file"</js>
	 * 		</ul>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 * 
	 * <h5 class='section'>See Also:</h5>
	 * <ul class='doctree'>
	 * 	<li class='link'><a class='doclink' href='https://swagger.io/specification/#dataTypes'>Swagger specification &gt; Data Types</a>
	 * </ul>
	 */
	String type() default "";
	
	/**
	 * <mk>format</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 * 
	 * <h5 class='section'>See Also:</h5>
	 * <ul class='doctree'>
	 * 	<li class='link'><a class='doclink' href='https://swagger.io/specification/#dataTypes'>Swagger specification &gt; Data Types</a>
	 * </ul>
	 */
	String format() default "";
	
	/**
	 * <mk>pattern</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 	<li>
	 * 		This string SHOULD be a valid regular expression.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String pattern() default "";

	/**
	 * <mk>collectionFormat</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 	<li>
	 * 		The possible value are:
	 * 		<ul>
	 * 			<li><js>"csv"</js>
	 * 			<li><js>"ssv"</js>
	 * 			<li><js>"tsv"</js>
	 * 			<li><js>"pipes"</js>
	 * 			<li><js>"multi"</js>
	 * 		</ul>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String collectionFormat() default "";

	/**
	 * <mk>maximum</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String maximum() default "";
	
	/**
	 * <mk>minimum</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String minimum() default "";

	/**
	 * <mk>multipleOf</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String multipleOf() default "";
	
	/**
	 * <mk>maxLength</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String maxLength() default "";
	
	/**
	 * <mk>minLength</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String minLength() default "";
	
	/**
	 * <mk>maxItems</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String maxItems() default "";

	/**
	 * <mk>minItems</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String minItems() default "";

	/**
	 * <mk>allowEmptyValue</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is boolean.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String allowEmptyValue() default "";

	/**
	 * <mk>exclusiveMaximum</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String exclusiveMaximum() default "";

	/**
	 * <mk>exclusiveMinimum</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is numeric.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String exclusiveMinimum() default "";
	
	/**
	 * <mk>uniqueItems</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is boolean.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String uniqueItems() default "";
	
	/**
	 * <mk>schema</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON object.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		The leading/trailing <code>{ }</code> characters are optional.
	 * 		<br>The following two example are considered equivalent:
	 * 		<ul>
	 * 			<li><code>schema=<js>"{type:'string',format:'binary'}"</js></code>
	 * 			<li><code>schema=<js>"type:'string',format:'binary'"</js></code>
	 * 		<ul>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	Schema schema() default @Schema;
	
	/**
	 * <mk>default</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <p>
	 * Additionally, this method can be used to define a default value for a missing query entry.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is any JSON.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] _default() default {};
	
	/**
	 * <mk>enum</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON array or comma-delimited list.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] _enum() default {};
	
	/**
	 * <mk>items</mk> field of the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON object.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	Items items() default @Items;	
	
	/**
	 * TODO
	 * 
	 * <p>
	 * This attribute defines a JSON representation of the value that is used by {@link BasicRestInfoProvider} to construct
	 * an example of the query entry.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON object or plain text string.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] example() default {};
	
	/**
	 * Free-form value for the Swagger <a class="doclink" href="https://swagger.io/specification/v2/#parameterObject">Parameter</a> object.
	 * 
	 * <p>
	 * This is a JSON object that makes up the swagger information for this field.
	 * 
	 * <p>
	 * The following are completely equivalent ways of defining the swagger description of the Query object:
	 * <p class='bcode w800'>
	 * 	<jc>// Normal</jc>
	 * 	<ja>@Query</ja>(
	 * 		name=<js>"status"</js>, 
	 * 		description=<js>"Status values that need to be considered for filter."</js>, 
	 * 		required=<js>"true"</js>, 
	 * 		type=<js>"array"</js>,
	 * 		items=<ja>@Items</ja>(
	 * 			type=<js>"string"</js>,
	 * 			_enum=<js>"AVAILABLE,PENDING,SOLD"</js>,
	 * 			_default=<js>"AVAILABLE"</js>
	 * 		),
	 * 		example=<js>"['AVAILABLE','PENDING']"</js>
	 * 	) 
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Free-form</jc>
	 * 	<ja>@Query</ja>(
	 * 		name=<js>"status"</js>, 
	 * 		api={
	 * 			<js>"description: 'Status values that need to be considered for filter.',"</js>, 
	 * 			<js>"required: true,"</js>, 
	 * 			<js>"type: 'array',"</js>,
	 * 			<js>"items: {"</js>,
	 * 				<js>"type: 'string',"</js>,
	 * 				<js>"enum: 'AVAILABLE,PENDING,SOLD',"</js>,
	 * 				<js>"default: 'AVAILABLE'"</js>,
	 * 			<js>"}"</js>,
	 * 			<js>"example: "['AVAILABLE','PENDING']"</js>
	 * 		}
	 * 	) 
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Free-form using variables</jc>
	 * 	<ja>@Query</ja>(
	 * 		name=<js>"status"</js>, 
	 * 		api=<js>"$L{statusSwagger}"</js>
	 * 	) 
	 * </p>
	 * <p class='bcode w800'>
	 * 	<mc>// Contents of MyResource.properties</mc>
	 * 	<mk>statusSwagger</mk> = <mv>{ description: "Status values that need to be considered for filter.", required: true, type: "array", items: {type: "string", enum: "AVAILABLE,PENDING,SOLD", default: "AVAILABLE"}, example: "['AVAILABLE','PENDING']" } }</mv>
	 * </p>
	 * 
	 * <p>
	 * 	The reasons why you may want to use this field include:
	 * <ul>
	 * 	<li>You want to pull in the entire Swagger JSON definition for this field from an external source such as a properties file.
	 * 	<li>You want to add extra fields to the Swagger documentation that are not officially part of the Swagger specification.
	 * </ul>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		Note that the only swagger field you can't specify using this value is <js>"name"</js> whose value needs to be known during servlet initialization.
	 * 	<li>
	 * 		The format is a Simplified JSON object.
	 * 	<li>
	 * 		The leading/trailing <code>{ }</code> characters are optional.
	 * 		<br>The following two example are considered equivalent:
	 * 		<p class='bcode w800'>
	 * 	<ja>@Query</ja>(api=<js>"{description: 'ID of order to fetch'}"</js>)
	 * 		</p>
	 * 		<p class='bcode w800'>
	 * 	<ja>@Query</ja>(api=<js>"description: 'ID of order to fetch''"</js>)
	 * 		</p>
	 * 	<li>
	 * 		Multiple lines are concatenated with newlines so that you can format the value to be readable.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined in this field supersede values pulled from the Swagger JSON file and are superseded by individual values defined on this annotation.
	 * </ul>
	 */
	String[] api() default {};
}
