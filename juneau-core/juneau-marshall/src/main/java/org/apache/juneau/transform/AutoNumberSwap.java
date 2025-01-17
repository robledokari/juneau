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
package org.apache.juneau.transform;

import static org.apache.juneau.internal.CollectionUtils.*;
import static org.apache.juneau.internal.ClassUtils.*;

import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.serializer.*;

/**
 * A dynamic POJO swap based on reflection of a Java class that converts POJOs to Number serializable objects.
 *
 * <p>
 * Looks for methods on the class that can be called to swap-in surrogate Number objects before serialization and swap-out
 * surrogate Number objects after parsing.
 *
 * <h5 class='figure'>Valid surrogate objects</h5>
 * <ul>
 * 	<li class='jc'>Any subclass of {@link Number}
 * 	<li class='jc'>Any number primitive
 * </ul>
 *
 * <h5 class='figure'>Valid swap methods (S = Swapped type)</h5>
 * <ul>
 * 	<li class='jm'><c><jk>public</jk> S toNumber()</c>
 * 	<li class='jm'><c><jk>public</jk> S toNumber(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toInteger()</c>
 * 	<li class='jm'><c><jk>public</jk> S toInteger(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toInt()</c>
 * 	<li class='jm'><c><jk>public</jk> S toInt(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toLong()</c>
 * 	<li class='jm'><c><jk>public</jk> S toLong(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toFloat()</c>
 * 	<li class='jm'><c><jk>public</jk> S toFloat(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toDouble()</c>
 * 	<li class='jm'><c><jk>public</jk> S toDouble(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toShort()</c>
 * 	<li class='jm'><c><jk>public</jk> S toShort(BeanSession)</c>
 * 	<li class='jm'><c><jk>public</jk> S toByte()</c>
 * 	<li class='jm'><c><jk>public</jk> S toByte(BeanSession)</c>
 * </ul>
 *
 * <h5 class='figure'>Valid unswap methods (N = Normal type, S = Swapped type)</h5>
 * <ul>
 * 	<li class='jm'><c><jk>public static</jk> N fromInteger(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromInteger(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromInt(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromInt(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromLong(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromLong(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromFloat(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromFloat(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromDouble(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromDouble(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromShort(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromShort(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromByte(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N fromByte(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N create(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N create(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N valueOf(S)</c>
 * 	<li class='jm'><c><jk>public static</jk> N valueOf(BeanSession, S)</c>
 * 	<li class='jm'><c><jk>public</jk> N(S)</c>
 * </ul>
 * <p>
 * Classes are ignored if any of the following are true:
 * <ul>
 * 	<li>Classes annotated with {@link BeanIgnore @BeanIgnore}.
 * 	<li>Non-static member classes.
 * </ul>
 *
 * <p>
 * Members/constructors are ignored if any of the following are true:
 * <ul>
 * 	<li>Members/constructors annotated with {@link BeanIgnore @BeanIgnore}.
 * 	<li>Deprecated members/constructors.
 * </ul>
 *
 * @param <T> The normal class type.
 */
public class AutoNumberSwap<T> extends PojoSwap<T,Number> {

	private static final Set<String>
		SWAP_METHOD_NAMES = newUnmodifiableHashSet("toNumber", "toInteger", "toInt", "toLong", "toFloat", "toDouble", "toShort", "toByte"),
		UNSWAP_METHOD_NAMES = newUnmodifiableHashSet("fromInteger", "fromInt", "fromLong", "fromFloat", "fromDouble", "fromShort", "fromByte", "create", "valueOf");

	/**
	 * Look for constructors and methods on this class and construct a dynamic swap if it's possible to do so.
	 *
	 * @param ci The class to try to constructor a dynamic swap on.
	 * @return A POJO swap instance, or <jk>null</jk> if one could not be created.
	 */
	@SuppressWarnings({ "rawtypes" })
	public static PojoSwap<?,?> find(ClassInfo ci) {

		if (shouldIgnore(ci))
			return null;

		// Find swap() method if present.
		for (MethodInfo m : ci.getPublicMethods()) {
			if (isSwapMethod(m)) {

				ClassInfo rt = m.getReturnType();

				for (MethodInfo m2 : ci.getPublicMethods())
					if (isUnswapMethod(m2, ci, rt))
						return new AutoNumberSwap(ci, m, m2, null);

				for (ConstructorInfo cs : ci.getPublicConstructors())
					if (isUnswapConstructor(cs, rt))
						return new AutoNumberSwap(ci, m, null, cs);

				return new AutoNumberSwap(ci, m, null, null);
			}
		}

		return null;
	}

	private static boolean shouldIgnore(ClassInfo ci) {
		return
			ci.hasAnnotation(BeanIgnore.class)
			|| ci.isNonStaticMemberClass()
			|| ci.isPrimitive()
			|| ci.isChildOf(Number.class);
	}

	private static boolean isSwapMethod(MethodInfo mi) {
		ClassInfo rt = mi.getReturnType();
		return
			mi.isNotDeprecated()
			&& mi.isNotStatic()
			&& (rt.isChildOf(Number.class) || (rt.isPrimitive() && rt.isAny(int.class, short.class, long.class, float.class, double.class, byte.class)))
			&& mi.hasName(SWAP_METHOD_NAMES)
			&& mi.hasFuzzyParamTypes(BeanSession.class)
			&& ! mi.hasAnnotation(BeanIgnore.class);
	}

	private static boolean isUnswapMethod(MethodInfo mi, ClassInfo ci, ClassInfo rt) {
		return
			mi.isNotDeprecated()
			&& mi.isStatic()
			&& mi.hasName(UNSWAP_METHOD_NAMES)
			&& mi.hasFuzzyParamTypes(BeanSession.class, rt.inner())
			&& mi.hasReturnTypeParent(ci)
			&& ! mi.hasAnnotation(BeanIgnore.class);
	}

	private static boolean isUnswapConstructor(ConstructorInfo cs, ClassInfo rt) {
		return
			cs.isNotDeprecated()
			&& cs.hasParamTypeParents(rt)
			&& ! cs.hasAnnotation(BeanIgnore.class);
	}

	//------------------------------------------------------------------------------------------------------------------

	private final Method swapMethod, unswapMethod;
	private final Constructor<?> unswapConstructor;
	private final Class<?> unswapType;

	private AutoNumberSwap(ClassInfo ci, MethodInfo swapMethod, MethodInfo unswapMethod, ConstructorInfo unswapConstructor) {
		super(ci.inner(), swapMethod.inner().getReturnType());
		this.swapMethod = swapMethod.inner();
		this.unswapMethod = unswapMethod == null ? null : unswapMethod.inner();
		this.unswapConstructor = unswapConstructor == null ? null : unswapConstructor.inner();

		Class<?> unswapType = null;
		if (unswapMethod != null) {
			for (ParamInfo pi : unswapMethod.getParams())
				if (! pi.getParameterType().is(BeanSession.class))
					unswapType = pi.getParameterType().getWrapperIfPrimitive();
		} else if (unswapConstructor != null) {
			for (ParamInfo pi : unswapConstructor.getParams())
				if (! pi.getParameterType().is(BeanSession.class))
					unswapType = pi.getParameterType().getWrapperIfPrimitive();
		}
		this.unswapType = unswapType;
	}

	@Override /* PojoSwap */
	public Number swap(BeanSession session, Object o) throws SerializeException {
		try {
			return (Number)swapMethod.invoke(o, getMatchingArgs(swapMethod.getParameterTypes(), session));
		} catch (Exception e) {
			throw SerializeException.create(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override /* PojoSwap */
	public T unswap(BeanSession session, Number o, ClassMeta<?> hint) throws ParseException {
		try {
			Object o2 = ObjectUtils.toType(o, unswapType);
			if (unswapMethod != null)
				return (T)unswapMethod.invoke(null, getMatchingArgs(unswapMethod.getParameterTypes(), session, o2));
			if (unswapConstructor != null)
				return (T)unswapConstructor.newInstance(o2);
			return super.unswap(session, o, hint);
		} catch (Exception e) {
			throw ParseException.create(e);
		}
	}
}
