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

package dev.pandasystems.universalserializer.typeadapter

import dev.pandasystems.universalserializer.Serializer
import kotlin.reflect.KType

fun interface TypeAdapterFactory {
	fun createAdapter(serializer: Serializer, type: KType, annotations: List<Annotation>): TypeAdapter<Any>?
}