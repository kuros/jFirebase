package in.kuros.jfirebase.metadata;

import com.google.cloud.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public final class RemoveAttribute<T> {

    private final List<AttributeValue<T, ?>> keys;
    private final List<Attribute<T, ?>> attributesToDelete;
    private final List<MapAttributeValue<T, ?, ?>> mapAttributesToDelete;
    private final List<ValuePath<?>> valuePaths;

    private RemoveAttribute(final AttributeValue<T, ?> key) {
        keys = new ArrayList<>();
        keys.add(key);
        attributesToDelete = new ArrayList<>();
        mapAttributesToDelete = new ArrayList<>();
        valuePaths = new ArrayList<>();
    }

    public static <T, K, V> RemoveAttribute<T> withKeys(final Attribute<T, V> attribute, final V value) {
        return new RemoveAttribute<>(AttributeValue.of(attribute, value));
    }

    public <V> RemoveAttribute<T> withKey(final Attribute<T, V> attribute, final V value) {
        keys.add(AttributeValue.of(attribute, value));
        return this;
    }

    public RemoveAttribute<T> remove(final Attribute<T, ?> attribute) {
        attributesToDelete.add(attribute);
        return this;
    }

    public <K> RemoveAttribute<T> removeMapKey(final MapAttribute<T, K, ?> mapAttribute, final K key) {
        mapAttributesToDelete.add(MapAttributeValue.of(mapAttribute, key, null));
        return this;
    }

    public <K> RemoveAttribute<T> removeMap(final MapAttribute<T, K, ?> mapAttribute) {
        mapAttributesToDelete.add(MapAttributeValue.of(mapAttribute, null));
        return this;
    }

    public RemoveAttribute<T> removeByPath(final String... path) {
        valuePaths.add(ValuePath.of(FieldValue.delete(), path));
        return this;
    }

    private  <V> List<AttributeValue<T, ?>> toAttributeValues(final Supplier<V> supplier) {
        final List<AttributeValue<T, ?>> attributeValues = getAttributesToDelete(supplier);
        attributeValues.addAll(getMapAttributesToDelete(supplier));

        return attributeValues;
    }

    private  <V> List<AttributeValue<T, ?>> getAttributesToDelete(final Supplier<V> supplier) {
        return attributesToDelete.stream()
                .map(attr -> AttributeValueImpl.of(attr, Value.of(supplier.get())))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private  <K, V> List<AttributeValue<T, ?>> getMapAttributesToDelete(final Supplier<V> supplier) {
        return mapAttributesToDelete
                .stream()
                .map(attr -> {
                    final MapAttributeValue<T, K, ?> attr1 = (MapAttributeValue<T, K, ?>) attr;
                    if (attr1.isKeyUpdate()) {
                        return new MapAttributeValueImpl<>(attr1.getAttribute(), attr1.getKey(), Value.of(supplier.get()));
                    }
                    return new MapAttributeValueImpl<>(attr1.getAttribute(), Value.of(supplier.get()));
                })
                .collect(Collectors.toList());
    }

    public interface Helper {
        static <T, V> List<AttributeValue<T, ?>> getKeys(RemoveAttribute<T> removeAttribute) {
            return removeAttribute.keys;
        }

        static <T, V> List<AttributeValue<T, ?>> getAttributeValues(RemoveAttribute<T> removeAttribute, Supplier<V> supplier) {
            return removeAttribute.toAttributeValues(supplier);
        }

        static <T> List<ValuePath<?>> getValuePaths(RemoveAttribute<T> removeAttribute) {
            return removeAttribute.valuePaths;
        }

        static <T> Class<T> getDeclaringClass(RemoveAttribute<T> removeAttribute) {
            if (removeAttribute.keys.isEmpty()) {
                throw new IllegalStateException("No Keys provided");
            }

            return removeAttribute.keys.get(0).getAttribute().getDeclaringType();
        }
    }
}
