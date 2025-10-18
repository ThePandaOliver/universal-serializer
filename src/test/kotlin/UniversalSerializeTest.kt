import com.google.common.reflect.TypeToken
import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.elements.TreePrimitive
import kotlin.test.Test
import kotlin.test.assertNotNull

class UniversalSerializeTest {
	@Test
	fun serializeTest() {
		val serializer = Serializer.create()

		val test = TestClass()
		val treeA = serializer.toTree(test)

		val treeB = TreeObject()
		treeB["test"] = TreePrimitive(0)
		treeB["test2"] = TreePrimitive("Hello World!")
		treeB["test3"] = TreeArray().also {
			it.add(TreePrimitive("Hello"))
			it.add(TreePrimitive("World!"))
		}
		treeB["test4"] = TreeObject().also {
			it["Hello"] = TreePrimitive(0)
			it["World"] = TreePrimitive(1)
		}

		assert(treeA == treeB)
	}

	@Test
	fun deserializeTest() {
		val serializer = Serializer.create()

		val tree = TreeObject()
		tree["test"] = TreePrimitive(3)
		tree["test2"] = TreePrimitive("Goodbye Java!")
		tree["test3"] = TreeArray().also {
			it.add(TreePrimitive("Goodbye"))
			it.add(TreePrimitive("Java!"))
		}
		tree["test4"] = TreeObject().also {
			it["Goodbye"] = TreePrimitive(1)
			it["Java"] = TreePrimitive(0)
		}

		val obj = serializer.fromTree(tree, TypeToken.of(TestClass::class.java))

		assertNotNull(obj)
		assert(obj.test == 3)
		assert(obj.test2 == "Goodbye Java!")
		assert(obj.test3 == listOf("Goodbye", "Java!"))
		assert(obj.test4 == mapOf("Goodbye" to 1, "Java" to 0))
	}

	class TestClass {
		var test: Int = 0
		var test2: String = "Hello World!"
		var test3: List<String> = listOf("Hello", "World!")
		var test4: Map<String, Int> = mapOf("Hello" to 0, "World" to 1)
	}
}