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
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf

class CollectionTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val kClass = type.classifier as? KClass<*> ?: return null
		if (!kClass.isSubclassOf(Collection::class)) return null

		val elementType: KType = type.arguments.firstOrNull()?.type ?: Any::class.createType()
		val isSet = kClass.isSubclassOf(Set::class)

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value is Collection<*>) { "Expected Collection, got ${value::class.simpleName}" }
				val arr = TreeArray()
				for (item in value) {
					arr.add(serializer.toTree(item, elementType))
				}
				return arr
			}

			override fun decode(element: TreeElement): Any {
				require(element is TreeArray) { "Expected TreeArray, got ${element::class.simpleName}" }
				return if (isSet) {
					val set = LinkedHashSet<Any>()
					for (child in element) {
						val decoded = serializer.fromTree(child, elementType)
						if (decoded != null) set += decoded
					}
					set
				} else {
					val list = ArrayList<Any>()
					for (child in element) {
						val decoded = serializer.fromTree(child, elementType)
						if (decoded != null) list += decoded
					}
					list
				}
			}
		}
	}
}
