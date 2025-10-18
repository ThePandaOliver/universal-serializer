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
import dev.pandasystems.universalserializer.typeadapter.factories.*

class SerializerSettingsImpl : SerializerSettings {
	val adapters = mutableMapOf<TypeToken<*>, TypeAdapter<*>>()
	val adapterFactories = mutableListOf<TypeAdapterFactory>()
	var prettyPrinting = false

	override fun <T : Any> registerTypeAdapter(
		type: TypeToken<T>,
		adapter: TypeAdapter<T>
	) {
		adapters[type] = adapter
	}

	override fun registerTypeAdapterFactory(factory: TypeAdapterFactory) {
		adapterFactories += factory
	}

	override fun setPrettyPrinting() {
		prettyPrinting = true
	}

	override fun setDefaultSettings() {
		adapters.clear()
		adapterFactories.clear()
		prettyPrinting = false

		// Register default factories for primitive types and common collections
		registerTypeAdapterFactory(StringTypeAdapterFactory())
		registerTypeAdapterFactory(BooleanTypeAdapterFactory())
		registerTypeAdapterFactory(NumberTypeAdapterFactory())
		registerTypeAdapterFactory(CollectionTypeAdapterFactory())
		registerTypeAdapterFactory(ArrayTypeAdapterFactory())
		registerTypeAdapterFactory(MapTypeAdapterFactory())
	}
}