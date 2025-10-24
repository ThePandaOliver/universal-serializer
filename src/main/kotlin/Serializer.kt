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

import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreeNull
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.formats.SerializerFormat
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory
import dev.pandasystems.universalserializer.typeadapter.factories.*
import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class Serializer @JvmOverloads constructor(
	val format: SerializerFormat? = null,

	private val adapters: MutableMap<KType, TypeAdapter<Any>> = mutableMapOf(),
	private val adapterFactories: MutableList<TypeAdapterFactory> = DEFAULT_TYPE_ADAPTER_FACTORIES.toMutableList()
) {
	private val cachedAdapters = mutableMapOf<KType, TypeAdapter<Any>>()


	// Serialize

	inline fun <reified T> toTree(obj: T): TreeElement = toTree(obj, typeOf<T>())
	fun <T : Any> toTree(obj: T?, clazz: Class<T>): TreeElement = toTree(obj, clazz.kotlin.createType())
	fun <T : Any> toTree(obj: T?, kClass: KClass<T>): TreeElement = toTree(obj, kClass.createType())

	fun toTree(obj: Any?, type: KType): TreeElement {
		fun serializeObject(obj: Any?, type: KType, annotations: List<Annotation>): TreeElement {
			if (obj == null) return TreeNull

			val adapter = getAdapter(type, annotations)
			if (adapter != null) return adapter.encode(obj)

			val treeObject = TreeObject()
			val classifier = type.classifier
			if (classifier is KClass<*>) {
				for (property in classifier.memberProperties) {
					@Suppress("UNCHECKED_CAST")
					val property = property as KProperty1<Any, Any?>
					property.isAccessible = true

					val value = property.get(obj)
					val fieldType = property.returnType
					treeObject[property.name] = serializeObject(value, fieldType, property.annotations)
				}
			}

			return treeObject
		}

		return serializeObject(obj, type, emptyList())
	}

	inline fun <reified T> toValue(obj: T): String = toValue(obj, typeOf<T>())
	fun <T : Any> toValue(obj: T?, clazz: Class<T>): String = toValue(obj, clazz.kotlin.createType())
	fun <T : Any> toValue(obj: T?, kClass: KClass<T>): String = toValue(obj, kClass.createType())

	fun toValue(obj: Any?, type: KType): String {
		requireNotNull(format) { "format was not specified" }
		return format.write(toTree(obj, type))
	}


	// Deserialize

	inline fun <reified T> fromTree(element: TreeElement): T? = fromTree(element, typeOf<T>()) as T?
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromTree(element: TreeElement, clazz: Class<T>): T? = fromTree(element, clazz.kotlin.createType()) as T?
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromTree(element: TreeElement, kClass: KClass<T>): T? = fromTree(element, kClass.createType()) as T?

	fun fromTree(element: TreeElement, type: KType): Any? {
		fun deserializeObject(element: TreeElement, type: KType, annotations: List<Annotation>): Any? {
			if (element is TreeNull) return null

			val adapter = getAdapter(type, annotations)
			if (adapter != null) return adapter.decode(element)

			if (element is TreeObject) {
				val classifier = type.classifier
				if (classifier is KClass<*>) {
					val instance = classifier.objectInstance ?: classifier.createInstance()

 				for (prop in classifier.memberProperties) {
						if (prop !is KMutableProperty1<*, *>) continue
						@Suppress("UNCHECKED_CAST")
						val property = prop as KMutableProperty1<Any, Any?>
						property.isAccessible = true

						val valueElement = element[property.name] ?: continue
						val fieldType = property.returnType

						property.set(instance, deserializeObject(valueElement, fieldType, property.annotations))
					}

					return instance
				}
			}

			throw IllegalArgumentException("Cannot deserialize $element into $type")
		}
		return deserializeObject(element, type, emptyList())
	}

	inline fun <reified T> fromValue(value: String): T? = fromValue(value, typeOf<T>()) as T?
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromValue(value: String, clazz: Class<T>): T? = fromValue(value, clazz.kotlin.createType()) as T?
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> fromValue(value: String, kClass: KClass<T>): T? = fromValue(value, kClass.createType()) as T?

	fun fromValue(value: String, type: KType): Any? {
		requireNotNull(format) { "format was not specified" }
		return fromTree(format.read(value), type)
	}


	// Adapters

	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : Any> getAdapter(annotations: List<Annotation> = emptyList()): TypeAdapter<T>? =
		getAdapter(typeOf<T>(), annotations) as? TypeAdapter<T>
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> getAdapter(clazz: Class<T>, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? =
		getAdapter(clazz.kotlin.createType(), annotations) as? TypeAdapter<T>
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> getAdapter(kClass: KClass<T>, annotations: List<Annotation> = emptyList()): TypeAdapter<T>? =
		getAdapter(kClass.createType(), annotations) as? TypeAdapter<T>

	@Suppress("UNCHECKED_CAST")
	fun getAdapter(type: KType, annotations: List<Annotation> = emptyList()): TypeAdapter<Any>? {
		val cached = cachedAdapters[type]
		if (cached != null) return cached

		var found: TypeAdapter<Any>? = null
		for (factory in adapterFactories) {
			val created = factory.createAdapter(this, type, annotations)
			if (created != null) {
				found = created
				break
			}
		}

		val result = found ?: adapters[type]
		if (result != null) {
			cachedAdapters[type] = result
		}
		return result
	}

	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : Any> registerTypeAdapter(adapter: TypeAdapter<T>) = registerTypeAdapter(typeOf<T>(), adapter as TypeAdapter<Any>) as T
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> registerTypeAdapter(clazz: Class<T>, adapter: TypeAdapter<T>) = registerTypeAdapter(clazz.kotlin.createType(), adapter as TypeAdapter<Any>)
	@Suppress("UNCHECKED_CAST")
	fun <T : Any> registerTypeAdapter(kClass: KClass<T>, adapter: TypeAdapter<T>) = registerTypeAdapter(kClass.createType(), adapter as TypeAdapter<Any>)

	fun registerTypeAdapter(type: KType, adapter: TypeAdapter<Any>) {
		adapters[type] = adapter
	}

	fun registerTypeAdapterFactory(factory: TypeAdapterFactory) = adapterFactories.add(factory)

	companion object {
		@JvmStatic
		val DEFAULT_TYPE_ADAPTER_FACTORIES = listOf(
			TreeElementTypeAdapterFactory(),
			StringTypeAdapterFactory(),
			BooleanTypeAdapterFactory(),
			NumberTypeAdapterFactory(),
			EnumTypeAdapterFactory(),
			CollectionTypeAdapterFactory(),
			ArrayTypeAdapterFactory(),
			MapTypeAdapterFactory()
		)
	}
}