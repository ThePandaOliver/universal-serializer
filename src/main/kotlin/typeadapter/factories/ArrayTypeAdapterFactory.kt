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
import java.lang.reflect.Array
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

object ArrayTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: KType,
		annotations: List<Annotation>
	): TypeAdapter<Any>? {
		val classifier = type.classifier as? KClass<*> ?: return null
		val raw = classifier.java
		if (!raw.isArray) return null

		val componentClass = raw.componentType
		val componentKType = componentClass.kotlin.createType()

		return object : TypeAdapter<Any> {
			override fun encode(value: Any): TreeElement {
				require(value::class.java.isArray) { "Expected array instance" }
				val arr = TreeArray()
				val len = Array.getLength(value)
				for (i in 0 until len) {
					val item = Array.get(value, i)
					arr.add(serializer.toTree(item, componentKType))
				}
				return arr
			}

			override fun decode(element: TreeElement, oldValue: Any?): Any {
				require(element is TreeArray) { "Expected TreeArray, got ${element::class.simpleName}" }
				val result = Array.newInstance(componentClass, element.size)
				var i = 0
				for (child in element) {
					val decoded = serializer.fromTree(child, componentKType)
					Array.set(result, i++, decoded)
				}
				return result
			}
		}
	}
}
