import dev.pandasystems.universalserializer.Serializer
import dev.pandasystems.universalserializer.elements.TreeArray
import dev.pandasystems.universalserializer.elements.TreeObject
import dev.pandasystems.universalserializer.elements.TreePrimitive
import dev.pandasystems.universalserializer.formats.JsonFormat
import kotlin.test.Test
import kotlin.test.assertNotNull

class UniversalSerializeTest {
	@Test
	fun serializeTest() {
		val serializer = Serializer(format = JsonFormat())

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
		treeB["test5"] = TreeObject().also {
			it["hello"] = TreePrimitive(0)
			it["world"] = TreePrimitive(1)
		}
		treeB["test6"] = TreePrimitive("A")

		assert(treeA == treeB)
	}

	@Test
	fun deserializeTest() {
		val serializer = Serializer(format = JsonFormat())

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
		tree["test5"] = TreeObject().also {
			it["hello"] = TreePrimitive(0)
			it["world"] = TreePrimitive(1)
		}
		tree["test6"] = TreePrimitive("A")

		val obj = serializer.fromTree<TestClass>(tree)

		assertNotNull(obj)
		assert(obj.test == 3)
		assert(obj.test2 == "Goodbye Java!")
		assert(obj.test3 == listOf("Goodbye", "Java!"))
		assert(obj.test4 == mapOf("Goodbye" to 1, "Java" to 0))
		assert(obj.test5.hello == 0)
		assert(obj.test5.world == 1)
		assert(obj.test6 == TestClass.TestEnum.A)
	}

	class TestClass {
		var test: Int = 0
		var test2: String = "Hello World!"
		var test3: List<String> = listOf("Hello", "World!")
		var test4: Map<String, Int> = mapOf("Hello" to 0, "World" to 1)

		val test5: InnerTestClass = InnerTestClass()

		var test6: TestEnum = TestEnum.A

		class InnerTestClass : AbstractTestClass() {
			override var hello: Int = 0
		}

		abstract class AbstractTestClass {
			abstract var hello: Int
			var world: Int = 1
		}

		enum class TestEnum {
			A, B, C
		}
	}

	@Test
	fun serialize_single_value() {
		val serializer = Serializer(format = JsonFormat())
		val number = 2L
		val serializedNumber = serializer.toTree(number)

		assert(serializedNumber is TreePrimitive)
		assert(serializedNumber.asPrimitive.asNumber == 2L)
	}
}