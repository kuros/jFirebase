package in.kuros.jfirebase.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class SetAttribute<T> {

    private final List<AttributeValue<T, ?>> keys;
    private final List<AttributeValue<T, ?>> attributesToUpdate;

    private SetAttribute(final AttributeValue<T, ?> key) {
        keys = new ArrayList<>();
        keys.add(key);
        attributesToUpdate = new ArrayList<>();
    }

    public static <T, K, V> SetAttribute<T> withKeys(final Attribute<T, V> attribute, final V value) {
        return new SetAttribute<>(AttributeValue.of(attribute, value));
    }

    public <V> SetAttribute<T> withKey(final Attribute<T, V> attribute, final V value) {
        keys.add(AttributeValue.of(attribute, value));
        return this;
    }

    public <V> SetAttribute<T> set(final Attribute<T, V> attribute, final V value) {
        attributesToUpdate.add(AttributeValue.of(attribute, value));
        return this;
    }

    public <K, V> SetAttribute<T> set(final MapAttribute<T, K, V> mapAttribute, final K key, final V value) {
        attributesToUpdate.add(MapAttributeValue.of(mapAttribute, key, value));
        return this;
    }

    public <K, V> SetAttribute<T> set(final MapAttribute<T, K, V> mapAttribute, final Map<K, V> value) {
        attributesToUpdate.add(MapAttributeValue.of(mapAttribute, value));
        return this;
    }


    public interface Helper {
        static <T> List<AttributeValue<T, ?>> getKeys(SetAttribute<T> updateAttribute) {
            return updateAttribute.keys;
        }

        static <T> List<AttributeValue<T, ?>> getAttributeValues(SetAttribute<T> updateAttribute) {
            return updateAttribute
                    .attributesToUpdate;
        }

        static <T> Class<T> getDeclaringClass(SetAttribute<T> updateAttribute) {
            if (updateAttribute.keys.isEmpty()) {
                throw new IllegalStateException("No Keys provided");
            }

            return updateAttribute.keys.get(0).getAttribute().getDeclaringType();
        }
    }
}
