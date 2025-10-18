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

class TreeArray : TreeElement, MutableList<TreeElement> {
	private val elements = mutableListOf<TreeElement>()

	override fun add(element: TreeElement): Boolean {
		return elements.add(element)
	}

	override fun remove(element: TreeElement): Boolean {
		return elements.remove(element)
	}

	override fun addAll(elements: Collection<TreeElement>): Boolean {
		return this.elements.addAll(elements)
	}

	override fun addAll(index: Int, elements: Collection<TreeElement>): Boolean {
		return this.elements.addAll(index, elements)
	}

	override fun removeAll(elements: Collection<TreeElement>): Boolean {
		return this.elements.removeAll(elements)
	}

	override fun retainAll(elements: Collection<TreeElement>): Boolean {
		return this.elements.retainAll(elements)
	}

	override fun clear() {
		elements.clear()
	}

	override fun set(
		index: Int,
		element: TreeElement
	): TreeElement {
		return this.elements.set(index, element)
	}

	override fun add(index: Int, element: TreeElement) {
		elements.add(index, element)
	}

	override fun removeAt(index: Int): TreeElement {
		return elements.removeAt(index)
	}

	override fun listIterator(): MutableListIterator<TreeElement> {
		return elements.listIterator()
	}

	override fun listIterator(index: Int): MutableListIterator<TreeElement> {
		return elements.listIterator(index)
	}

	override fun subList(
		fromIndex: Int,
		toIndex: Int
	): MutableList<TreeElement> {
		return elements.subList(fromIndex, toIndex)
	}

	override val size: Int
		get() = elements.size

	override fun isEmpty(): Boolean {
		return elements.isEmpty()
	}

	override fun contains(element: TreeElement): Boolean {
		return elements.contains(element)
	}

	override fun containsAll(elements: Collection<TreeElement>): Boolean {
		return this.elements.containsAll(elements)
	}

	override fun get(index: Int): TreeElement {
		return elements[index]
	}

	override fun indexOf(element: TreeElement): Int {
		return elements.indexOf(element)
	}

	override fun lastIndexOf(element: TreeElement): Int {
		return elements.lastIndexOf(element)
	}

	override fun iterator(): MutableIterator<TreeElement> {
		return elements.iterator()
	}

	override fun toString(): String {
		return elements.joinToString(", ", "{", "}") { element -> element.toString() }
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is TreeArray) return false
		return this.elements == other.elements
	}

	override fun hashCode(): Int = elements.hashCode()
}