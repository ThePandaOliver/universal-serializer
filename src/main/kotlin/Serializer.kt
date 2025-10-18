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
import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreeNull
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import kotlin.reflect.jvm.kotlinProperty

class Serializer(
	private val adapters: Map<TypeToken<*>, TypeAdapter<*>>,
	private val adapterFactories: List<TypeAdapterFactory>,
	private val prettyPrinting: Boolean
) {
	private val cachedAdapters = mutableMapOf<TypeToken<*>, TypeAdapter<*>>()

	fun <T : Any> toTree(obj: T?, annotations: List<Annotation> = emptyList()): TreeElement {
		if (obj == null) return TreeNull
		@Suppress("UNCHECKED_CAST")
		val type = TypeToken.of(obj::class.java) as TypeToken<T>

		val adapter = getAdapter(type, annotations)
		if (adapter != null) return adapter.encode(obj)

 	val treeObject = TreeObject()
 	type.rawType.declaredFields.forEach { field ->
 		val mods = field.modifiers
 		if (java.lang.reflect.Modifier.isStatic(mods) || field.isSynthetic) return@forEach
 		val name = field.kotlinProperty?.name ?: field.name
 		field.isAccessible = true

 		val annotations = field.annotations.toList()
 		val value = field[obj]

 		@Suppress("UNCHECKED_CAST")
 		val fieldType = TypeToken.of(field.genericType) as TypeToken<Any>
 		treeObject[name] = toTree(value, fieldType, annotations)
 	}

 	return treeObject
	}

	@Suppress("UNCHECKED_CAST")
	fun toTree(obj: Any?, type: TypeToken<*>, annotations: List<Annotation> = emptyList()): TreeElement {
		if (obj == null) return TreeNull
		val t = type as TypeToken<Any>
		val adapter = getAdapter(t, annotations)
		if (adapter != null) return adapter.encode(obj)

		val treeObject = TreeObject()
		t.rawType.declaredFields.forEach { field ->
			val mods = field.modifiers
			if (java.lang.reflect.Modifier.isStatic(mods) || field.isSynthetic) return@forEach
			val name = field.kotlinProperty?.name ?: field.name
			field.isAccessible = true

			val anns = field.annotations.toList()
			val value = field[obj]
			val fieldType = TypeToken.of(field.genericType) as TypeToken<Any>
			treeObject[name] = toTree(value, fieldType, anns)
		}
		return treeObject
	}

	fun <T : Any> fromTree(element: TreeElement, type: TypeToken<T>, annotations: List<Annotation> = emptyList()): T? {
		val clazz = type.rawType
		if (element is TreeNull) return null

		val adapter = getAdapter(type, annotations)
		if (adapter != null) return adapter.decode(element)

		@Suppress("UNCHECKED_CAST")
		val instance = clazz.getConstructor().newInstance() as T

		if (element is TreeObject) {
			for (field in clazz.declaredFields) {
				val name = field.kotlinProperty?.name ?: field.name
				field.isAccessible = true

				val annotations = field.annotations.toList()
				val value = element[name] ?: continue

				field[instance] = fromTree(value, TypeToken.of(field.genericType), annotations)
			}
		}

		return instance
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> getAdapter(type: TypeToken<T>, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? {
		val cached = cachedAdapters[type] as? TypeAdapter<T>
		if (cached != null) return cached

		var found: TypeAdapter<*>? = null
		for (factory in adapterFactories) {
			val created = factory.createAdapter(this, type, annotations)
			if (created != null) {
				found = created
				break
			}
		}

		val result = (found as? TypeAdapter<T>) ?: (adapters[type] as? TypeAdapter<T>)
		if (result != null) {
			cachedAdapters[type] = result
		}
		return result
	}

	companion object {
		fun create(settingsBlock: SerializerSettings.() -> Unit): Serializer {
			val settingsImpl = SerializerSettingsImpl()
			settingsImpl.settingsBlock()
			return Serializer(
				adapters = settingsImpl.adapters,
				adapterFactories = settingsImpl.adapterFactories,
				prettyPrinting = settingsImpl.prettyPrinting
			)
		}

		fun create(): Serializer = create { setDefaultSettings() }
	}
}