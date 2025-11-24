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
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf

object MapTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val kClass = type.classifier as? KClass<*> ?: return null
		if (!kClass.isSubclassOf(Map::class)) return null

		val args = type.arguments
		if (args.size != 2) return null
		val keyType = args[0].type ?: return null
		val valueType: KType = args[1].type ?: Any::class.createType()

		// Only support String keys to encode to TreeObject fields
		val keyClassifier = keyType.classifier as? KClass<*> ?: return null
		if (keyClassifier != String::class) return null

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is Map<*, *>) { "Expected Map, got ${value::class.simpleName}" }
				val obj = TreeObject()
				for ((k, v) in value) {
					obj[k as String] = serializer.toTree(v, valueType)
				}
				return obj
			}

			override fun decode(element: TreeElement, oldValue: Any?): Any {
				if (element !is TreeObject) throw IllegalArgumentException("Expected TreeObject, got ${element::class.simpleName}")
				val map = LinkedHashMap<String, Any>()
				for ((k, v) in element) {
					val decoded = serializer.fromTree(v, valueType)
					if (decoded != null) map[k] = decoded
				}
				return map
			}
		}
	}
}
