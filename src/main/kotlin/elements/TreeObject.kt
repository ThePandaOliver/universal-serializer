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

package dev.pandasystems.universalserializer.elements

class TreeObject : TreeElement, MutableMap<String, TreeElement> {
	private val elements: MutableMap<String, TreeElement> = mutableMapOf()

	override val keys: MutableSet<String>
		get() = elements.keys
	override val values: MutableCollection<TreeElement>
		get() = elements.values
	override val entries: MutableSet<MutableMap.MutableEntry<String, TreeElement>>
		get() = elements.entries

	override fun put(
		key: String,
		value: TreeElement
	): TreeElement {
		elements[key] = value
		return value
	}

	override fun remove(key: String): TreeElement? {
		return elements.remove(key)
	}

	override fun putAll(from: Map<out String, TreeElement>) {
		elements.putAll(from)
	}

	override fun clear() {
		elements.clear()
	}

	override val size: Int
		get() = elements.size

	override fun isEmpty(): Boolean {
		return elements.isEmpty()
	}

	override fun containsKey(key: String): Boolean {
		return elements.containsKey(key)
	}

	override fun containsValue(value: TreeElement): Boolean {
		return elements.containsValue(value)
	}

	override fun get(key: String): TreeElement? {
		return elements[key]
	}

	override fun toString(): String {
		return elements.entries.joinToString(", ", "{", "}") { (key, value) -> "\"$key\": $value" }
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is TreeObject) return false
		return this.elements == other.elements
	}

	override fun hashCode(): Int = elements.hashCode()
}