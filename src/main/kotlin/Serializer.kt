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

class Serializer @JvmOverloads constructor(
	val format: SerializerFormat? = null,

	private val adapters: MutableMap<TypeToken<*>, TypeAdapter<*>> = mutableMapOf(),
	private val adapterFactories: MutableList<TypeAdapterFactory> = DEFAULT_TYPE_ADAPTER_FACTORIES.toMutableList()
) {
	private val cachedAdapters = mutableMapOf<TypeToken<*>, TypeAdapter<*>>()


	// Serialize

	inline fun <reified T : Any> toTree(obj: T?): TreeElement = toTree(obj, T::class.java)

	fun <T : Any> toTree(obj: T?, clazz: Class<T>): TreeElement = toTree(obj, TypeToken.of(clazz))

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> toTree(obj: T?, typeOf: Type): TreeElement = toTree(obj, TypeToken.of(typeOf) as TypeToken<T>)

	fun <T : Any> toTree(obj: T?, typeToken: TypeToken<T>): TreeElement {
		fun <R : Any> serializeObject(obj: R?, typeToken: TypeToken<R>, annotations: List<Annotation>): TreeElement {
			if (obj == null) return TreeNull
			val adapter = getAdapter(typeToken, annotations)
			if (adapter != null) return adapter.encode(obj)

			val treeObject = TreeObject()
			val raw = typeToken.rawType
			for (field in raw.declaredFields) {
				val mods = field.modifiers
				if (Modifier.isStatic(mods) || field.isSynthetic) break
				field.isAccessible = true
				val name = field.kotlinProperty?.name ?: field.name

				val annotations = field.annotations.toList()
				val value = field[obj]
				@Suppress("UNCHECKED_CAST")
				val fieldType = TypeToken.of(field.genericType) as TypeToken<Any>
				treeObject[name] = serializeObject(value, fieldType, annotations)
			}
			return treeObject
		}

		return serializeObject(obj, typeToken, emptyList())
	}

	inline fun <reified T : Any> toValue(obj: T): String = toValue(obj, T::class.java)

	fun <T : Any> toValue(obj: T, clazz: Class<T>): String = toValue(obj, TypeToken.of(clazz))

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> toValue(obj: T, typeOf: Type): String = toValue(obj, TypeToken.of(typeOf) as TypeToken<T>)

	fun <T : Any> toValue(obj: T?, typeToken: TypeToken<T>): String {
		requireNotNull(format) { "format was not specified" }
		return format.write(toTree(obj, typeToken))
	}


	// Deserialize

	inline fun <reified T : Any> fromTree(element: TreeElement): T? = fromTree(element, T::class.java)

	fun <T : Any> fromTree(element: TreeElement, clazz: Class<T>): T? = fromTree(element, TypeToken.of(clazz))

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromTree(element: TreeElement, typeOf: Type): T? = fromTree(element, TypeToken.of(typeOf) as TypeToken<T>)

	fun <T : Any> fromTree(element: TreeElement, typeToken: TypeToken<T>): T? {
		fun <R : Any> deserializeObject(element: TreeElement, typeToken: TypeToken<R>, annotations: List<Annotation>): R? {
			if (element is TreeNull) return null

			val adapter = getAdapter(typeToken, annotations)
			if (adapter != null) return adapter.decode(element)

			val raw = typeToken.rawType as Class<R>
			if (element is TreeObject) {
				val instance = raw.getConstructor().newInstance()

				for (field in raw.declaredFields) {
					val mods = field.modifiers
					if (Modifier.isStatic(mods) || field.isSynthetic) break
					field.isAccessible = true
					val name = field.kotlinProperty?.name ?: field.name

					val annotations = field.annotations.toList()
					val valueElement = element[name] ?: continue
					val fieldType = TypeToken.of(field.genericType)

					field[instance] = deserializeObject(valueElement, fieldType, annotations)
				}

				return instance
			}

			throw IllegalArgumentException("Cannot deserialize $element into ${raw.canonicalName}")
		}
		return deserializeObject(element, typeToken, emptyList())
	}

	inline fun <reified T : Any> fromValue(value: String): T? = fromValue(value, T::class.java)

	fun <T : Any> fromValue(value: String, clazz: Class<T>): T? = fromValue(value, TypeToken.of(clazz))

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromValue(value: String, typeOf: Type): T? = fromValue(value, TypeToken.of(typeOf) as TypeToken<T>)

	fun <T : Any> fromValue(value: String, typeToken: TypeToken<T>): T? {
		requireNotNull(format) { "format was not specified" }
		return fromTree(format.read(value), typeToken)
	}


	// Adapters

	fun <T : Any> getAdapter(clazz: Class<T>, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? =
		getAdapter(TypeToken.of(clazz), annotations)

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> getAdapter(typeOf: Type, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? =
		getAdapter(TypeToken.of(typeOf), annotations) as? TypeAdapter<T>

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> getAdapter(typeToken: TypeToken<T>, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? {
		val cached = cachedAdapters[typeToken] as? TypeAdapter<T>
		if (cached != null) return cached

		var found: TypeAdapter<*>? = null
		for (factory in adapterFactories) {
			val created = factory.createAdapter(this, typeToken, annotations)
			if (created != null) {
				found = created
				break
			}
		}

		val result = (found as? TypeAdapter<T>) ?: (adapters[typeToken] as? TypeAdapter<T>)
		if (result != null) {
			cachedAdapters[typeToken] = result
		}
		return result
	}

	inline fun <reified T : Any> registerTypeAdapter(adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(T::class.java), adapter)

	fun <T : Any> registerTypeAdapter(clazz: Class<T>, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(clazz), adapter)

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> registerTypeAdapter(typeOf: Type, adapter: TypeAdapter<T>) = registerTypeAdapter(TypeToken.of(typeOf) as TypeToken<T>, adapter)

	fun <T : Any> registerTypeAdapter(typeToken: TypeToken<T>, adapter: TypeAdapter<T>) = adapters.put(typeToken, adapter)

	fun registerTypeAdapterFactory(factory: TypeAdapterFactory) = adapterFactories.add(factory)

	companion object {
		@JvmStatic
		val DEFAULT_TYPE_ADAPTER_FACTORIES = listOf(
			TreeElementTypeAdapterFactory(),
			StringTypeAdapterFactory(),
			BooleanTypeAdapterFactory(),
			NumberTypeAdapterFactory(),
			CollectionTypeAdapterFactory(),
			ArrayTypeAdapterFactory(),
			MapTypeAdapterFactory()
		)
	}
}