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

interface TreeElement {
	val isNull get() = this is TreeNull

	val isPrimitive get() = this is TreePrimitive
	val asPrimitive get() = this as TreePrimitive

	val isObject get() = this is TreeObject
	val asObject get() = this as TreeObject

	val isArray get() = this is TreeArray
	val asArray get() = this as TreeArray

	override fun toString(): String
}