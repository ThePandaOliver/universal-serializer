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

package dev.pandasystems.universalserializer.formats

import dev.pandasystems.universalserializer.elements.*

class JsonFormat(
	val prettyPrint: Boolean = false
) : SerializerFormat {
	override val fileExtension: String = "json"

	override fun write(element: TreeElement): String {
		val sb = StringBuilder()
		serialize(element, sb, if (prettyPrint) 0 else -1)
		return sb.toString()
	}

	override fun read(string: String): TreeElement {
		return Parser(string).parse()
	}

	private fun serialize(element: TreeElement, sb: StringBuilder, indentLevel: Int) {
		when (element) {
			is TreeNull -> sb.append("null")
			is TreePrimitive -> serializePrimitive(element, sb)
			is TreeObject -> serializeObject(element, sb, indentLevel)
			is TreeArray -> serializeArray(element, sb, indentLevel)
		}
	}

	private fun serializePrimitive(p: TreePrimitive, sb: StringBuilder) {
		when {
			p.isString -> sb.append('"').append(escapeString(p.asString)).append('"')
			p.isNumber -> sb.append(p.asNumber.toString())
			p.isBoolean -> sb.append(p.asBoolean.toString())
			else -> sb.append("null")
		}
	}

	private fun serializeObject(obj: TreeObject, sb: StringBuilder, indentLevel: Int) {
		sb.append('{')
		if (obj.isEmpty()) { sb.append('}'); return }
		val pretty = indentLevel >= 0
		var first = true
		for ((key, value) in obj) {
			if (first) first = false else sb.append(',')
			if (pretty) {
				sb.append('\n')
				repeat(indentLevel + 1) { sb.append('\t') }
			}
			sb.append('"').append(escapeString(key)).append('"').append(':')
			if (pretty) sb.append(' ')
			serialize(value, sb, if (pretty) indentLevel + 1 else -1)
		}
		if (pretty) {
			sb.append('\n')
			repeat(indentLevel) { sb.append('\t') }
		}
		sb.append('}')
	}

	private fun serializeArray(arr: TreeArray, sb: StringBuilder, indentLevel: Int) {
		sb.append('[')
		if (arr.isEmpty()) { sb.append(']'); return }
		val pretty = indentLevel >= 0
		var first = true
		for (el in arr) {
			if (first) first = false else sb.append(',')
			if (pretty) {
				sb.append('\n')
				repeat(indentLevel + 1) { sb.append('\t') }
			}
			serialize(el, sb, if (pretty) indentLevel + 1 else -1)
		}
		if (pretty) {
			sb.append('\n')
			repeat(indentLevel) { sb.append('\t') }
		}
		sb.append(']')
	}

	private fun escapeString(s: String): String {
		val out = StringBuilder()
		for (ch in s) {
			when (ch) {
				'\\' -> out.append("\\\\")
				'\"' -> out.append("\\\"")
				'\b' -> out.append("\\b")
				'\u000C' -> out.append("\\f")
				'\n' -> out.append("\\n")
				'\r' -> out.append("\\r")
				'\t' -> out.append("\\t")
				else -> {
					if (ch < ' ') {
						out.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
					} else out.append(ch)
				}
			}
		}
		return out.toString()
	}

	private class Parser(private val src: String) {
		private var i = 0

		fun parse(): TreeElement {
			skipWs()
			val value = readValue()
			skipWs()
			return value
		}

		private fun readValue(): TreeElement {
			return when (peek()) {
				'{' -> readObject()
				'[' -> readArray()
				'"' -> TreePrimitive(readString())
				in '0'..'9', '-', '+' -> readNumber()
				't' -> { expectLiteral("true"); TreePrimitive(true) }
				'f' -> { expectLiteral("false"); TreePrimitive(false) }
				'n' -> { expectLiteral("null"); TreeNull }
				else -> error("Unexpected character '${peek()}' at position $i")
			}
		}

		private fun readObject(): TreeObject {
			expect('{')
			skipWs()
			val obj = TreeObject()
			if (peek() == '}') { i++; return obj }
			while (true) {
				skipWs()
				val key = readString()
				skipWs()
				expect(':')
				skipWs()
				val value = readValue()
				obj[key] = value
				skipWs()
				when (peek()) {
					',' -> { i++; skipWs(); continue }
					'}' -> { i++; return obj }
					else -> error("Expected ',' or '}' at position $i")
				}
			}
		}

		private fun readArray(): TreeArray {
			expect('[')
			skipWs()
			val arr = TreeArray()
			if (peek() == ']') { i++; return arr }
			while (true) {
				skipWs()
				arr.add(readValue())
				skipWs()
				when (peek()) {
					',' -> { i++; skipWs(); continue }
					']' -> { i++; return arr }
					else -> error("Expected ',' or ']' at position $i")
				}
			}
		}

		private fun readString(): String {
			expect('"')
			val out = StringBuilder()
			while (true) {
				if (i >= src.length) error("Unterminated string")
				val ch = src[i++]
				when (ch) {
					'"' -> return out.toString()
					'\\' -> {
						if (i >= src.length) error("Unterminated escape sequence")
						when (val esc = src[i++]) {
							'"' -> out.append('"')
							'\\' -> out.append('\\')
							'/' -> out.append('/')
							'b' -> out.append('\b')
							'f' -> out.append('\u000C')
							'n' -> out.append('\n')
							'r' -> out.append('\r')
							't' -> out.append('\t')
							'u' -> {
								if (i + 4 > src.length) error("Invalid unicode escape")
								val hex = src.substring(i, i + 4)
								val code = hex.toInt(16)
								i += 4
								out.append(code.toChar())
							}
							else -> error("Invalid escape character '$esc'")
						}
					}
					else -> out.append(ch)
				}
			}
		}

		private fun readNumber(): TreePrimitive {
			val start = i
			if (peek() == '+' || peek() == '-') i++
			while (i < src.length && src[i].isDigit()) i++
			if (i < src.length && src[i] == '.') {
				i++
				while (i < src.length && src[i].isDigit()) i++
			}
			if (i < src.length && (src[i] == 'e' || src[i] == 'E')) {
				i++
				if (i < src.length && (src[i] == '+' || src[i] == '-')) i++
				while (i < src.length && src[i].isDigit()) i++
			}
			val numStr = src.substring(start, i)
			return try {
				if (numStr.contains('.') || numStr.contains('e', true)) TreePrimitive(numStr.toDouble()) else TreePrimitive(numStr.toLong())
			} catch (e: NumberFormatException) {
				TreePrimitive(numStr.toDouble())
			}
		}

		private fun expect(c: Char) {
			if (peek() != c) error("Expected '$c' at position $i but found '${peek()}'")
			i++
		}

		private fun expectLiteral(lit: String) {
			if (!src.regionMatches(i, lit, 0, lit.length, ignoreCase = false)) error("Expected '$lit' at position $i")
			i += lit.length
		}

		private fun skipWs() {
			while (i < src.length && src[i].isWhitespace()) i++
		}

		private fun peek(): Char {
			if (i >= src.length) error("Unexpected end of input at position $i")
			return src[i]
		}
	}
}