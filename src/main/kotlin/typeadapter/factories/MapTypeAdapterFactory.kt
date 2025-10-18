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
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import java.lang.reflect.ParameterizedType

class MapTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		val raw = type.rawType
		if (!java.util.Map::class.java.isAssignableFrom(raw)) return null

		val t = type.type
		if (t !is ParameterizedType) return null
		val keyType = t.actualTypeArguments[0]
		val valueType = t.actualTypeArguments[1]

		// Only support String keys to encode to TreeObject fields
		if (keyType != String::class.java) return null

		@Suppress("UNCHECKED_CAST")
		val valueToken = TypeToken.of(valueType) as TypeToken<Any>

		return object : TypeAdapter<Map<String, Any>> {
			override fun encode(value: Map<String, Any>): TreeElement {
				val obj = TreeObject()
				for ((k, v) in value) {
					obj[k] = serializer.toTree(v, valueToken, emptyList())
				}
				return obj
			}

			override fun decode(element: TreeElement): Map<String, Any> {
				if (element !is TreeObject) throw IllegalArgumentException("Expected TreeObject, got ${element::class.simpleName}")
				val map = LinkedHashMap<String, Any>()
				for ((k, v) in element) {
					val decoded = serializer.fromTree(v, valueToken, emptyList())
					if (decoded != null) map[k] = decoded
				}
				return map
			}
		}
	}
}
