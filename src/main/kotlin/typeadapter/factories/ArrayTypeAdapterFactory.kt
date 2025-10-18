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
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import java.lang.reflect.Array

class ArrayTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		val raw = type.rawType
		if (!raw.isArray) return null

		val componentType = raw.componentType
		@Suppress("UNCHECKED_CAST")
		val componentToken = TypeToken.of(componentType) as TypeToken<Any>

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value::class.java.isArray) { "Expected array instance" }
				val arr = TreeArray()
				val len = Array.getLength(value)
				for (i in 0 until len) {
					val item = Array.get(value, i)
					arr.add(serializer.toTree(item, componentToken, emptyList()))
				}
				return arr
			}

			override fun decode(element: TreeElement): Any {
				require(element is TreeArray) { "Expected TreeArray, got ${element::class.simpleName}" }
				val result = Array.newInstance(componentType, element.size)
				var i = 0
				for (child in element) {
					val decoded = serializer.fromTree(child, componentToken, emptyList())
					Array.set(result, i++, decoded)
				}
				return result
			}
		}
	}
}
