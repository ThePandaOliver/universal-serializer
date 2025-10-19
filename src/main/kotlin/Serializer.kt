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
import dev.pandasystems.universalserializer.formats.SerializerFormat
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import dev.pandasystems.universalserializer.typeadapter.factories.*
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import kotlin.reflect.jvm.kotlinProperty

class Serializer(
	val format: SerializerFormat? = null,

	private val adapters: MutableMap<TypeToken<*>, TypeAdapter<*>> = mutableMapOf(),
	private val adapterFactories: MutableList<TypeAdapterFactory> = mutableListOf(
		TreeElementTypeAdapterFactory(),
		StringTypeAdapterFactory(),
		BooleanTypeAdapterFactory(),
		NumberTypeAdapterFactory(),
		CollectionTypeAdapterFactory(),
		ArrayTypeAdapterFactory(),
		MapTypeAdapterFactory()
	)
) {
	private val cachedAdapters = mutableMapOf<TypeToken<*>, TypeAdapter<*>>()

	fun <T : Any> toTree(obj: T?, annotations: List<Annotation> = emptyList()): TreeElement {
		return toTree(obj, TypeToken.of(obj?.javaClass ?: Any::class.java), annotations)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> toTree(obj: T?, type: TypeToken<T>, annotations: List<Annotation> = emptyList()): TreeElement {
		if (obj == null) return TreeNull
		val t = type as TypeToken<Any>
		val adapter = getAdapter(t, annotations)
		if (adapter != null) return adapter.encode(obj)

		val treeObject = TreeObject()
		t.rawType.declaredFields.forEach { field ->
			val mods = field.modifiers
			if (Modifier.isStatic(mods) || field.isSynthetic) return@forEach
			val name = field.kotlinProperty?.name ?: field.name
			field.isAccessible = true

			val anns = field.annotations.toList()
			val value = field[obj]
			val fieldType = TypeToken.of(field.genericType) as TypeToken<Any>
			treeObject[name] = toTree(value, fieldType, anns)
		}
		return treeObject
	}

	fun <T : Any> toValue(obj: T?, annotations: List<Annotation> = emptyList()): String {
		requireNotNull(format) { "format must not be null" }
		return format.write(toTree(obj, annotations))
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

	fun <T : Any> fromValue(value: String, type: TypeToken<T>, annotations: List<Annotation> = emptyList()): T? {
		requireNotNull(format) { "format must not be null" }
		return fromTree(format.read(value), type, annotations)
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

	fun <T : Any> registerTypeAdapter(type: Class<T>, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(type), adapter)

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> registerTypeAdapter(type: Type, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(type) as TypeToken<T>, adapter)
	fun <T : Any> registerTypeAdapter(type: TypeToken<T>, adapter: TypeAdapter<T>) = adapters.put(type, adapter)

	fun registerTypeAdapterFactory(factory: TypeAdapterFactory) = adapterFactories.add(factory)
}