import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.formats.JsonFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * Tests focused on Kotlin `object` (singleton) classes serialization/deserialization.
 */
class ObjectSingletonSerializationTest {

	object SampleSingleton {
		var count: Int = 0
		var name: String = "init"
		var tags: List<String> = listOf("a", "b")
		var mapping: Map<String, Int> = mapOf("x" to 1, "y" to 2)
		val constantValue: String = "const" // should serialize, but won't be mutated during deserialize
	}

	@Test
	fun serialize_object_singleton_to_tree() {
		// Arrange
		SampleSingleton.count = 7
		SampleSingleton.name = "singleton"
		SampleSingleton.tags = listOf("one", "two")
		SampleSingleton.mapping = mapOf("left" to 10, "right" to 20)

		val serializer = Serializer(format = JsonFormat())

		// Act
		val tree = serializer.toTree(SampleSingleton)

		// Assert structure equals an explicitly-built tree
		val expected = TreeObject().also { root ->
			root["count"] = TreePrimitive(7)
			root["name"] = TreePrimitive("singleton")
			root["tags"] = TreeArray().also {
				it.add(TreePrimitive("one"))
				it.add(TreePrimitive("two"))
			}
			root["mapping"] = TreeObject().also {
				it["left"] = TreePrimitive(10)
				it["right"] = TreePrimitive(20)
			}
			root["constantValue"] = TreePrimitive("const")
		}

		assertEquals(expected, tree)
	}

	@Test
	fun deserialize_into_object_singleton_from_tree_identity_and_state() {
		// Arrange input tree with different values
		val input = TreeObject().also { root ->
			root["count"] = TreePrimitive(42)
			root["name"] = TreePrimitive("updated")
			root["tags"] = TreeArray().also {
				it.add(TreePrimitive("alpha"))
				it.add(TreePrimitive("beta"))
			}
			root["mapping"] = TreeObject().also {
				it["a"] = TreePrimitive(1)
				it["b"] = TreePrimitive(2)
			}
			// constantValue present in input should be ignored during mutation because it's a val
			root["constantValue"] = TreePrimitive("ignored")
		}

		val serializer = Serializer(format = JsonFormat())

		// Act
		val before = SampleSingleton
		val result = serializer.fromTree(input, SampleSingleton::class)

		// Assert returned instance is the singleton itself and fields mutated
		assertNotNull(result)
		assertSame(before, result)
		assertSame(SampleSingleton, result)
		assertEquals(42, SampleSingleton.count)
		assertEquals("updated", SampleSingleton.name)
		assertEquals(listOf("alpha", "beta"), SampleSingleton.tags)
		assertEquals(mapOf("a" to 1, "b" to 2), SampleSingleton.mapping)
		// val should remain original
		assertEquals("const", SampleSingleton.constantValue)
	}

	@Test
	fun object_singleton_round_trip_json_value() {
		// Arrange
		SampleSingleton.count = 3
		SampleSingleton.name = "roundtrip"
		SampleSingleton.tags = listOf("t1", "t2", "t3")
		SampleSingleton.mapping = mapOf("k" to 9)

		val serializer = Serializer(format = JsonFormat(prettyPrint = true))

		// Act: toValue and then fromValue specifying the KClass overload
		val json = serializer.toValue(SampleSingleton)
		val decoded = serializer.fromValue(json, SampleSingleton::class)

		// Assert identity and content
		assertNotNull(decoded)
		assertSame(SampleSingleton, decoded)
		assertEquals(3, decoded.count)
		assertEquals("roundtrip", decoded.name)
		assertEquals(listOf("t1", "t2", "t3"), decoded.tags)
		assertEquals(mapOf("k" to 9), decoded.mapping)
	}
}
