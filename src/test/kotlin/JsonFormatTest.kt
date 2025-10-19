import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeNull
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.formats.JsonFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JsonFormatTest {
	private fun buildSampleTree(): TreeObject {
		val root = TreeObject()
		root["a"] = TreePrimitive(1)
		root["b"] = TreePrimitive("x")
		root["c"] = TreeArray().also {
			it.add(TreePrimitive(true))
			it.add(TreeNull)
			it.add(TreePrimitive(3.5))
		}
		root["d"] = TreeObject().also {
			it["e"] = TreePrimitive("f")
		}
		return root
	}

	@Test
	fun write_compact_json() {
		val fmt = JsonFormat()
		val json = fmt.write(buildSampleTree())
		val expected = "{" +
			"\"a\":1," +
			"\"b\":\"x\"," +
			"\"c\":[true,null,3.5]," +
			"\"d\":{\"e\":\"f\"}" +
			"}"
		assertEquals(expected, json)
	}

	@Test
	fun write_pretty_print_json() {
		val fmt = JsonFormat(prettyPrint = true)
		val json = fmt.write(buildSampleTree())
		val expected = buildString {
			append("{\n")
			append("\t\"a\": 1,\n")
			append("\t\"b\": \"x\",\n")
			append("\t\"c\": [\n")
			append("\t\ttrue,\n")
			append("\t\tnull,\n")
			append("\t\t3.5\n")
			append("\t],\n")
			append("\t\"d\": {\n")
			append("\t\t\"e\": \"f\"\n")
			append("\t}\n")
			append("}")
		}
		assertEquals(expected, json)
	}

	@Test
	fun read_compact_and_pretty_equivalence() {
		val compact = "{\"a\":1,\"b\":\"x\",\"c\":[true,null,3.5],\"d\":{\"e\":\"f\"}}"
		val pretty = """
			{
				"a": 1,
				"b": "x",
				"c": [
					true,
					null,
					3.5
				],
				"d": {
					"e": "f"
				}
			}
		""".trimIndent()
		val fmt = JsonFormat()
		val parsedCompact = fmt.read(compact)
		val parsedPretty = fmt.read(pretty)
		// Compare by writing both in compact form to avoid any subtle object equality issues
		val outCompact = fmt.write(parsedCompact)
		val outPrettyAsCompact = fmt.write(parsedPretty)
		assertEquals(outCompact, outPrettyAsCompact)
		assertEquals(compact, outCompact)
	}

	@Test
	fun string_escaping_round_trip() {
		val tricky = "Quote: \" Backslash: \\ Newline:\n Tab:\t Slash:/ Unicode:\u263A"
		val root = TreeObject()
		root["s"] = TreePrimitive(tricky)

		val fmtCompact = JsonFormat()
		val fmtPretty = JsonFormat(prettyPrint = true)
		val jsonCompact = fmtCompact.write(root)
		val jsonPretty = fmtPretty.write(root)

		// ensure escapes emitted for required characters
		assertTrue(jsonCompact.contains("\\\""))
		assertTrue(jsonCompact.contains("\\\\"))
		assertTrue(jsonCompact.contains("\\n"))
		assertTrue(jsonCompact.contains("\\t"))
		// forward slash escaping is optional in JSON; writer may leave it as '/'
		assertTrue(jsonCompact.contains("/"))
		// unicode may be emitted as raw character; accept either raw or escaped
		assertTrue(jsonCompact.contains("\u263A") || jsonCompact.contains("\u263a") || jsonCompact.contains("☺"))

		// and reading gives back the same string
		val readBack = fmtCompact.read(jsonCompact).asObject["s"]!!.asPrimitive.asString
		assertEquals(tricky, readBack)

		// pretty should read the same as well
		val readPretty = fmtPretty.read(jsonPretty).asObject["s"]!!.asPrimitive.asString
		assertEquals(tricky, readPretty)
	}

	@Test
	fun numbers_various_forms() {
		val fmt = JsonFormat()
		val arr = fmt.read("[-12,0,3.14,1e3,-2E-2,+7]").asArray

		fun num(idx: Int) = arr[idx].asPrimitive.asNumber
		assertEquals(-12L, num(0).toLong())
		assertEquals(0L, num(1).toLong())
		assertEquals(3.14, num(2).toDouble(), 1e-9)
		assertEquals(1000.0, num(3).toDouble(), 1e-9)
		assertEquals(-0.02, num(4).toDouble(), 1e-9)
		assertEquals(7L, num(5).toLong())
	}

	@Test
	fun serializer_round_trip_value() {
		class T {
			var i: Int = 42
			var s: String = "hello"
			var list: List<String> = listOf("a", "b")
			var map: Map<String, Int> = mapOf("x" to 1, "y" to 2)
		}

		val serializer = Serializer(format = JsonFormat(prettyPrint = true))
		val original = T()
		val json = serializer.toValue(original)
		val decoded = serializer.fromValue<T>(json)

		assertNotNull(decoded)
		assertEquals(original.i, decoded.i)
		assertEquals(original.s, decoded.s)
		assertEquals(original.list, decoded.list)
		assertEquals(original.map, decoded.map)
	}
}
