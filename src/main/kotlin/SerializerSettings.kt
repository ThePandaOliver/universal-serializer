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

package dev.pandasystems.universalserializer

import com.google.common.reflect.TypeToken
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import java.lang.reflect.Type

interface SerializerSettings {
	fun <T : Any> registerTypeAdapter(type: Class<T>, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(type), adapter)
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> registerTypeAdapter(type: Type, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(type) as TypeToken<T>, adapter)
	fun <T : Any> registerTypeAdapter(type: TypeToken<T>, adapter: TypeAdapter<T>)

	fun registerTypeAdapterFactory(factory: TypeAdapterFactory)

	fun setPrettyPrinting()

	fun setDefaultSettings()
}