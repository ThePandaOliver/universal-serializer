import dev.pandasystems.universalserializer.Serializer;
import dev.pandasystems.universalserializer.elements.TreeArray;
import dev.pandasystems.universalserializer.elements.TreeElement;
import dev.pandasystems.universalserializer.elements.TreeObject;
import dev.pandasystems.universalserializer.elements.TreePrimitive;
import dev.pandasystems.universalserializer.formats.JsonFormat;
import dev.pandasystems.universalserializer.typeadapter.TypeAdapter;
import dev.pandasystems.universalserializer.typeadapter.TypeAdapterFactory;
import kotlin.reflect.KType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class UniversalSerializeJavaTest {
	@Test
	public void serializeTest() {
		Serializer serializer = new Serializer(new JsonFormat());

		TestClass test = new TestClass();
		TreeElement treeA = serializer.toTree(test, TestClass.class);

		TreeObject treeB = new TreeObject();
		treeB.put("test", new TreePrimitive(0));
		treeB.put("test2", new TreePrimitive("Hello World!"));
		var arr = new TreeArray();
		arr.add(new TreePrimitive("Hello"));
		arr.add(new TreePrimitive("World!"));
		treeB.put("test3", arr);
		var object = new TreeObject();
		object.put("Hello", new TreePrimitive(0));
		object.put("World", new TreePrimitive(1));
		treeB.put("test4", object);

		assert (treeA.equals(treeB));
	}

	@Test
	public void deserializeTest() {
		Serializer serializer = new Serializer(new JsonFormat());

		TreeObject tree = new TreeObject();
		tree.put("test", new TreePrimitive(3));
		tree.put("test2", new TreePrimitive("Goodbye Java!"));
		var arr = new TreeArray();
		arr.add(new TreePrimitive("Goodbye"));
		arr.add(new TreePrimitive("Java!"));
		tree.put("test3", arr);
		var object = new TreeObject();
		object.put("Goodbye", new TreePrimitive(1));
		object.put("Java", new TreePrimitive(0));
		tree.put("test4", object);

		TestClass obj = serializer.fromTree(tree, TestClass.class);

		Assertions.assertNotNull(obj);
		assert (obj.test == 3);
		assert (obj.test2.equals("Goodbye Java!"));
		assert (obj.test3.equals(List.of("Goodbye", "Java!")));
		assert (obj.test4.equals(Map.of("Goodbye", 1, "Java", 0)));
	}

	public static class TestClass {
		public int test = 0;
		public String test2 = "Hello World!";
		public List<String> test3 = List.of("Hello", "World!");
		public Map<String, Integer> test4 = Map.of("Hello", 0, "World", 1);
	}

	@Test
	public void serialize_single_value() {
		Serializer serializer = new Serializer(new JsonFormat());
		long number = 2L;
		TreeElement serializedNumber = serializer.toTree(number, long.class);

		assert (serializedNumber instanceof TreePrimitive);
		assert (serializedNumber.getAsPrimitive().getAsNumber().longValue() == 2L);
	}
}
