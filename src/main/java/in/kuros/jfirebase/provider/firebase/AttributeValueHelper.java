package in.kuros.jfirebase.provider.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.MapAttributeValue;
import in.kuros.jfirebase.metadata.ValuePath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributeValueHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EntityHelper entityHelper;

    public AttributeValueHelper() {
        entityHelper = EntityHelper.INSTANCE;
    }

    public <T> T createEntity(final List<AttributeValue<T, ?>> attributeValues) {
        try {
            final Constructor<T> constructor = getDeclaringClass(attributeValues).getConstructor();
            constructor.setAccessible(true);
            final T entity = constructor.newInstance();
            populateValues(entity, attributeValues);
            entityHelper.validateIdsNotNull(entity);
            return entity;
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    public <T> List<FieldPath> getFieldPaths(final List<AttributeValue<T, ?>> attributeValues) {
        return attributeValues
                .stream()
                .map(this::toFieldPath)
                .collect(Collectors.toList());
    }

    private <T> void populateValues(final T entity, final List<AttributeValue<T, ?>> attributeValues) {
        attributeValues
                .forEach(attributeValue -> {
                    final Field field = attributeValue.getAttribute().getField();
                    field.setAccessible(true);
                    try {
                        if (MapAttributeValue.class.isAssignableFrom(attributeValue.getClass())) {
                            final Map map = getMapValueForField(entity, attributeValue, field);
                            field.set(entity, map);
                        } else {
                            field.set(entity, attributeValue.getAttributeValue().getValue());
                        }
                    } catch (final IllegalAccessException e) {
                        throw new PersistenceException(e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <T> Map getMapValueForField(final T entity, final AttributeValue attributeValue, final Field field) throws IllegalAccessException {
        final MapAttributeValue mapAttribute = (MapAttributeValue) attributeValue;
        Map map = (Map) field.get(entity);
        if (mapAttribute.isKeyUpdate()) {
            if (map == null) {
                map = new HashMap();
            }
            map.put(mapAttribute.getKey(), mapAttribute.getMapValue().getValue());
        } else {
            map = (Map) mapAttribute.getAttributeValue().getValue();
        }
        return map;
    }

    private <T> FieldPath toFieldPath(final AttributeValue<T, ?> attributeValue) {
        if (attributeValue instanceof MapAttributeValue) {
            final MapAttributeValue mapAttributeValue = (MapAttributeValue) attributeValue;
            if (mapAttributeValue.isKeyUpdate()) {
                return FieldPath.of(mapAttributeValue.getAttribute().getName(), mapAttributeValue.getKey().toString());
            }
        }
        return FieldPath.of(attributeValue.getAttribute().getName());
    }

    private <T> Class<T> getDeclaringClass(final List<AttributeValue<T, ?>> attributeValues) {
        if (attributeValues.size() == 0) {
            throw new IllegalStateException("No Keys provided");
        }

        return attributeValues.get(0).getAttribute().getDeclaringType();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Map<String, Object> convertToObjectMap(final List<AttributeValue<T, ?>> attributeValues) {
        final Map<String, Object> result = new HashMap<>();

        for (AttributeValue<T, ?> attributeValue : attributeValues) {
            if (MapAttributeValue.class.isAssignableFrom(attributeValue.getClass())
                    && ((MapAttributeValue) attributeValue).isKeyUpdate()) {
                final MapAttributeValue mapAttributeValue = (MapAttributeValue) attributeValue;
                if (!(mapAttributeValue.getKey() instanceof String)) {
                    throw new IllegalArgumentException("Object keys are not supported in firebase/firestore");
                }
                final String attributeName = attributeValue.getAttribute().getName();
                final Map<String, Object> mapField = (Map<String, Object>) result.getOrDefault(attributeName, new HashMap<>());
                mapField.put((String) mapAttributeValue.getKey(), parseValue(mapAttributeValue.getMapValue().getValue()));
                result.put(attributeName, mapField);
            } else {
                result.put(attributeValue.getAttribute().getName(), parseValue(attributeValue.getAttributeValue().getValue()));
            }
        }

        return result;
    }

    private Object parseValue(final Object value) {
        if (value instanceof Number || value instanceof String || value instanceof List || value instanceof FieldValue) {
            return value;
        }

        if (value instanceof Enum<?>) {
            return ((Enum<?>)value).name();
        }

        return OBJECT_MAPPER.convertValue(value, Map.class);
    }

    public void addValuePaths(final Map<String, Object> objectMap, final List<ValuePath<?>> valuePaths) {
        for (ValuePath<?> valuePath : valuePaths) {
            addValuePath(objectMap, valuePath, 0);
        }
    }

    public List<FieldPath> convertValuePathToFieldPaths(List<ValuePath<?>> valuePaths) {
        return valuePaths
                .stream()
                .map(valuePath -> FieldPath.of(valuePath.getPath()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void addValuePath(final Map<String, Object> objectMap, final ValuePath valuePath, final int index) {
        if (index + 1 >= valuePath.getPath().length) {
            objectMap.put(valuePath.getPath()[index], parseValue(valuePath.getValue()));
            return;
        }

        final Map<String, Object> out = (Map<String, Object>) objectMap.getOrDefault(valuePath.getPath()[index], new HashMap<>());
        addValuePath(out, valuePath, index + 1);
        objectMap.put(valuePath.getPath()[index], out);

    }
}
