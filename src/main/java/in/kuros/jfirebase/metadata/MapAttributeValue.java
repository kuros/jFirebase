package in.kuros.jfirebase.metadata;

import java.util.Map;

public interface MapAttributeValue<T, K, V> extends AttributeValue<T, Map<K, V>>  {
    K getKey();
    Value getMapValue();
    boolean isKeyUpdate();


    static <T, K, V> MapAttributeValue<T, K, V> of(MapAttribute<T, K, V> attribute, K key, V value) {
        return new MapAttributeValueImpl<>(attribute, key, Value.of(value));
    }

    static <T, K, V> MapAttributeValue<T, K, V> of(MapAttribute<T, K, V> attribute, Map<K, V> value) {
        return new MapAttributeValueImpl<>(attribute, Value.of(value));
    }
}
