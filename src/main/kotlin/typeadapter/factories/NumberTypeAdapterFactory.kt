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

import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType

class NumberTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val kClass = type.classifier as? KClass<*> ?: return null
		val boxed = kClass.javaObjectType
		if (!Number::class.java.isAssignableFrom(boxed)) return null
		@Suppress("UNCHECKED_CAST")
		val numberClazz = boxed as Class<Number>
		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is Number) { "Expected Number, got ${value::class.simpleName}" }
				return TreePrimitive(value)
			}

			override fun decode(element: TreeElement): Any {
				require(element is TreePrimitive) { "Expected TreePrimitive, got ${element::class.simpleName}" }
				val n = element.asNumber
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