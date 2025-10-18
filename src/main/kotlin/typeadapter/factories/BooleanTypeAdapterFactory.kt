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
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory

class BooleanTypeAdapterFactory : TypeAdapterFactory {
	override fun createAdapter(
		serializer: Serializer,
		type: TypeToken<*>,
		annotations: List<Annotation>
	): TypeAdapter<*>? {
		if (type.rawType != java.lang.Boolean::class.java && type.rawType != Boolean::class.javaPrimitiveType) return null
		return object : TypeAdapter<Boolean> {
			override fun encode(value: Boolean): TreeElement = TreePrimitive(value)
			override fun decode(element: TreeElement): Boolean {
				require(element is TreePrimitive) { "Expected TreePrimitive, got ${element::class.simpleName}" }
				return element.asBoolean
			}
		}
	}
}
