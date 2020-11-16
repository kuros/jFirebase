package in.kuros.jfirebase.provider.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.FieldPath;
import com.google.common.collect.Lists;
import in.kuros.jfirebase.entity.TestClass;
import in.kuros.jfirebase.entity.TestClass_;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.MetadataProcessor;
import in.kuros.jfirebase.metadata.ValuePath;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttributeValueHelperTest {

    private AttributeValueHelper attributeValueHelper;

    @BeforeEach
    void setUp() {
        attributeValueHelper = new AttributeValueHelper();
        MetadataProcessor.init("in.kuros.jfirebase.entity");
    }

    @Test
    void shouldCreateEntityForGivenAttributeValues() {

        final String id = RandomStringUtils.randomAlphanumeric(3);
        final String value = RandomStringUtils.randomAlphanumeric(5);
        final TestClass testEntity = attributeValueHelper.createEntity(AttributeValue
                .with(TestClass_.testId, id)
                .with(TestClass_.testValue, value)
                .build());

        assertNotNull(testEntity);
        assertEquals(id, testEntity.getTestId());
        assertEquals(value, testEntity.getTestValue());
        assertNull(testEntity.getTestMap());
    }

    @Test
    void shouldThrowExceptionWhenCreateEntityForGivenAttributeValuesWithoutId() {

        final String value = RandomStringUtils.randomAlphanumeric(5);
        assertThrows(PersistenceException.class, () -> attributeValueHelper.createEntity(AttributeValue
                .with(TestClass_.testValue, value)
                .build()));
    }

    @Test
    void shouldConvertToFieldValueMap() {
        final String id = RandomStringUtils.randomAlphanumeric(3);
        final String value = RandomStringUtils.randomAlphanumeric(5);
        final String mapKey = RandomStringUtils.randomAlphanumeric(4);
        final String mapValue = RandomStringUtils.randomAlphanumeric(4);
        final List<AttributeValue<TestClass, ?>> attributeValues = AttributeValue
                .with(TestClass_.testId, id)
                .with(TestClass_.testValue, value)
                .with(TestClass_.testMap, mapKey, mapValue)
                .build();

        final Map<String, Object> resultMap = attributeValueHelper.toFieldValueMap(attributeValues);

        assertEquals(3, resultMap.size());
        assertEquals(id,  resultMap.get("testId"));
        assertEquals(value,  resultMap.get("testValue"));
        assertEquals(mapValue,  resultMap.get("testMap." + mapKey));
    }

    @Test
    void shouldConvertToFieldValueMapWithCompleteMapPath() {
        final Map<String, String> map = new HashMap<>();
        map.put(RandomStringUtils.randomAlphanumeric(4), RandomStringUtils.randomAlphanumeric(4));

        final List<AttributeValue<TestClass, ?>> attributeValues = AttributeValue
                .with(TestClass_.testMap, map)
                .build();

        final Map<String, Object> resultMap = attributeValueHelper.toFieldValueMap(attributeValues);

        assertEquals(1, resultMap.size());
        assertEquals(map,  resultMap.get("testMap"));
    }

    @Test
    void shouldCreateFieldPathForAttributeValues() {
        final String id = RandomStringUtils.randomAlphanumeric(3);
        final String value = RandomStringUtils.randomAlphanumeric(5);
        final String mapKey = RandomStringUtils.randomAlphanumeric(4);
        final String mapValue = RandomStringUtils.randomAlphanumeric(4);
        final List<AttributeValue<TestClass, ?>> attributeValues = AttributeValue
                .with(TestClass_.testId, id)
                .with(TestClass_.testValue, value)
                .with(TestClass_.testMap, mapKey, mapValue)
                .build();

        final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);

        final ArrayList<FieldPath> expected = Lists.newArrayList(FieldPath.of("testId"), FieldPath.of("testValue"), FieldPath.of("testMap", mapKey));
        assertIterableEquals(expected, fieldPaths);
    }

    @Test
    void shouldAddValuePathsToMap() throws JsonProcessingException {
        final Map<String, Object> map = new HashMap<>();
        map.put("xyz", "lkasd");

        attributeValueHelper.addValuePaths(map, Lists.newArrayList(ValuePath.of(1, "a", "b", "c"), ValuePath.of(1, "a", "x", "d")));

        assertEquals("{\"a\":{\"b\":{\"c\":1},\"x\":{\"d\":1}},\"xyz\":\"lkasd\"}", new ObjectMapper().writeValueAsString(map));
    }

    @Test
    void name() {
        final Map<String, Object> map = new HashMap<>();
        map.put("xyz", new TestClass());

        final Map map1 = new ObjectMapper().convertValue(map, Map.class);
        System.out.println(map1);
    }
}
