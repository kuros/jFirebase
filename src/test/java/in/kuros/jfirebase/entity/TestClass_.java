package in.kuros.jfirebase.entity;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;
import in.kuros.jfirebase.metadata.Metadata;

@Metadata(TestClass.class)
public class TestClass_ {
    @Id
    public static volatile Attribute<TestClass, String> testId;
    public static volatile Attribute<TestClass, String> testValue;
    public static volatile MapAttribute<TestClass, String, String> testMap;
}
