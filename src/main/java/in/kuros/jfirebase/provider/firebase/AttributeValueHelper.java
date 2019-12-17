package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.FieldPath;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.MapAttributeValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributeValueHelper {

    private final EntityHelper entityHelper;

    public AttributeValueHelper() {
        entityHelper = EntityHelper.INSTANCE;
    }

    public <T> T createEntity(final Class<T> type, final List<AttributeValue<T, ?>> attributeValues) {
        try {
            final Constructor<T> constructor = type.getConstructor();
            constructor.setAccessible(true);
            final T entity = constructor.newInstance();
            populateValues(entity, attributeValues);
            entityHelper.validateIdsNotNull(entity);
            return entity;
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    public <T> Map<String, Object> toFieldValueMap(final List<AttributeValue<T, ?>> attributeValues) {
        return attributeValues
                .stream()
                .collect(Collectors.toMap(
                        attributeValue -> {
                            if (MapAttributeValue.class.isAssignableFrom(attributeValue.getClass())) {
                                final MapAttributeValue mapAttributeValue = (MapAttributeValue) attributeValue;
                                if (mapAttributeValue.isKeyUpdate()) {
                                    return mapAttributeValue.getAttribute().getName() + "." + mapAttributeValue.getKey();
                                }
                            }
                            return attributeValue.getAttribute().getName();
                        },

                        attributeValue -> {
                            if (MapAttributeValue.class.isAssignableFrom(attributeValue.getClass())) {
                                final MapAttributeValue mapAttributeValue = (MapAttributeValue) attributeValue;
                                if (mapAttributeValue.isKeyUpdate()) {
                                    return mapAttributeValue.getMapValue().getValue();
                                }
                            }
                            return attributeValue.getAttributeValue().getValue();
                        }));
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

}
