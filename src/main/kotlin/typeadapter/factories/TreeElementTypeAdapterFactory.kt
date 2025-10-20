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
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory

class TreeElementTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		val raw = type.rawType
		if (!TreeElement::class.java.isAssignableFrom(raw)) return null

		@Suppress("UNCHECKED_CAST")
		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is TreeElement) { "Expected TreeElement, got ${value::class.java.name}" }
				return value
			}

			override fun decode(element: TreeElement): Any {
				// Ensure we return exactly the requested subtype when possible
				if (!raw.isInstance(element)) {
					throw IllegalArgumentException("Expected ${raw.name}, got ${element::class.java.name}")
				}
				return element
			}
		}
	}
}
