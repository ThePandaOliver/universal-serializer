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

class TreePrimitive private constructor(
	private val string: String?,
	private val boolean: Boolean?,
	private val number: Number?
): TreeElement {
	constructor(string: String) : this(string, null, null)
	constructor(boolean: Boolean) : this(null, boolean, null)
	constructor(number: Number) : this(null, null, number)

	override fun toString(): String = string ?: number?.toString() ?: boolean?.toString() ?: "null"

	val isString: Boolean get() = string != null
	val asString: String get() = string ?: throw IllegalStateException("Cannot get string value for non-string primitive")
	val isBoolean: Boolean get() = boolean != null
	val asBoolean: Boolean get() = boolean ?: throw IllegalStateException("Cannot get boolean value for non-boolean primitive")
	val isNumber: Boolean get() = number != null
	val asNumber: Number get() = number ?: throw IllegalStateException("Cannot get number value for non-number primitive")

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is TreePrimitive) return false
		return string == other.string && boolean == other.boolean && number == other.number
	}

	override fun hashCode(): Int {
		var result = string?.hashCode() ?: 0
		result = 31 * result + (boolean?.hashCode() ?: 0)
		result = 31 * result + (number?.hashCode() ?: 0)
		return result
	}
}