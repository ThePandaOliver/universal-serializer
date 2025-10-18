/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.universalserializer.typeadapter.factories

import com.google.common.reflect.TypeToken
import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory

class NumberTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		val raw = type.rawType
		// Support boxed and primitive numeric types
		val boxed: Class<out Number>? = when (raw) {
			java.lang.Byte::class.java, java.lang.Byte.TYPE -> java.lang.Byte::class.java
			java.lang.Short::class.java, java.lang.Short.TYPE -> java.lang.Short::class.java
			java.lang.Integer::class.java, java.lang.Integer.TYPE -> java.lang.Integer::class.java
			java.lang.Long::class.java, java.lang.Long.TYPE -> java.lang.Long::class.java
			java.lang.Float::class.java, java.lang.Float.TYPE -> java.lang.Float::class.java
			java.lang.Double::class.java, java.lang.Double.TYPE -> java.lang.Double::class.java
			else -> if (Number::class.java.isAssignableFrom(raw)) raw.asSubclass(Number::class.java) else null
		}
		boxed ?: return null
		@Suppress("UNCHECKED_CAST")
		val numberClazz = boxed as Class<Number>
		return object : TypeAdapter<Number> {
			override fun encode(value: Number): TreeElement {
				return TreePrimitive(value)
			}

			override fun decode(element: TreeElement): Number {
				require(element is TreePrimitive) { "Expected TreePrimitive, got ${element::class.simpleName}" }
				val n = element.asNumber
				// Convert to the requested boxed type when necessary
				return when (numberClazz) {
					java.lang.Byte::class.java -> java.lang.Byte.valueOf(n.toByte())
					java.lang.Short::class.java -> java.lang.Short.valueOf(n.toShort())
					java.lang.Integer::class.java -> java.lang.Integer.valueOf(n.toInt())
					java.lang.Long::class.java -> java.lang.Long.valueOf(n.toLong())
					java.lang.Float::class.java -> java.lang.Float.valueOf(n.toFloat())
					java.lang.Double::class.java -> java.lang.Double.valueOf(n.toDouble())
					else -> numberClazz.cast(n)
				}
			}
		}
	}
}