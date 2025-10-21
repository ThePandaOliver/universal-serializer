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
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

class TreeElementTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val kClass = type.classifier as? KClass<*> ?: return null
		if (!kClass.isSubclassOf(TreeElement::class)) return null

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is TreeElement) { "Expected TreeElement, got ${value::class.java.name}" }
				return value
			}

			override fun decode(element: TreeElement): Any {
				if (!kClass.java.isInstance(element)) {
					throw IllegalArgumentException("Expected ${kClass.java.name}, got ${element::class.java.name}")
				}
				return element
			}
		}
	}
}
