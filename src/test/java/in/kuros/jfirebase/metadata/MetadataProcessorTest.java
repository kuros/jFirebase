package in.kuros.jfirebase.metadata;

import in.kuros.jfirebase.entity.TestClass;
import in.kuros.jfirebase.entity.TestClass_;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetadataProcessorTest {

    @Test
    void shouldInitialiseMetaModel() throws Exception {
        MetadataProcessor.init("in.kuros.jfirebase.metadata");

        assertNotNull(TestClass_.testId);
        assertEquals(TestClass.class, TestClass_.testId.getDeclaringType());
        assertEquals(TestClass.class.getDeclaredField("testId"), TestClass_.testId.getField());
        assertEquals(TestClass.class.getDeclaredField("testId").getName(), TestClass_.testId.getName());

        assertNotNull(TestClass_.testValue);
        assertEquals(TestClass.class, TestClass_.testValue.getDeclaringType());
        assertEquals(TestClass.class.getDeclaredField("testValue"), TestClass_.testValue.getField());
        assertEquals(TestClass.class.getDeclaredField("testValue").getName(), TestClass_.testValue.getName());

        assertNotNull(TestClass_.testMap);
        assertEquals(TestClass.class, TestClass_.testMap.getDeclaringType());
        assertEquals(TestClass.class.getDeclaredField("testMap"), TestClass_.testMap.getField());
        assertEquals(TestClass.class.getDeclaredField("testMap").getName(), TestClass_.testMap.getName());
    }
}
