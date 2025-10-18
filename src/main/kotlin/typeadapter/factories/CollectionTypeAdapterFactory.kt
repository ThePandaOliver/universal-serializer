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
import java.lang.reflect.ParameterizedType

class CollectionTypeAdapterFactory : TypeAdapterFactory {
	@Suppress("UNCHECKED_CAST")
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		val raw = type.rawType
		if (!java.util.Collection::class.java.isAssignableFrom(raw)) return null

		// Determine element type
		val elementType = when (val t = type.type) {
			is ParameterizedType -> TypeToken.of(t.actualTypeArguments[0])
			else -> TypeToken.of(Any::class.java)
		} as TypeToken<Any>

		val isSet = java.util.Set::class.java.isAssignableFrom(raw)

		return object : TypeAdapter<Collection<Any>> {
			override fun encode(value: Collection<Any>): TreeElement {
				val arr = TreeArray()
				for (item in value) {
					arr.add(serializer.toTree(item, elementType, emptyList()))
				}
				return arr
			}

			override fun decode(element: TreeElement): Collection<Any> {
				require(element is TreeArray) { "Expected TreeArray, got ${element::class.simpleName}" }
				return if (isSet) {
					val set = LinkedHashSet<Any>()
					for (child in element) {
						val decoded = serializer.fromTree(child, elementType, emptyList())
						if (decoded != null) set += decoded
					}
					set
				} else {
					val list = ArrayList<Any>()
					for (child in element) {
						val decoded = serializer.fromTree(child, elementType, emptyList())
						if (decoded != null) list += decoded
					}
					list
				}
			}
		}
	}
}
