/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.universalserializer.typeadapter.factories

import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType

object EnumTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val kClass = type.classifier as? KClass<*> ?: return null
		val raw = kClass.java
		if (!raw.isEnum) return null

		@Suppress("UNCHECKED_CAST")
		val enumClass = raw as Class<out Enum<*>>

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is Enum<*>) { "Expected Enum, got ${value::class.simpleName}" }
				return TreePrimitive(value.name)
			}

			override fun decode(element: TreeElement, oldValue: Any?): Any {
				require(element is TreePrimitive) { "Expected TreePrimitive, got ${element::class.simpleName}" }
				val name = element.asString
				return java.lang.Enum.valueOf(enumClass, name)
			}
		}
	}
}
