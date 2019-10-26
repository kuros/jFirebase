package in.kuros.jfirebase.metadata;

public interface AttributeValue<T, V> {
    Attribute<T, V> getAttribute();
    Value getAttributeValue();

    static <T, V> AttributeValue<T, V> of(Attribute<T, V> attribute, V value) {
        return AttributeValueImpl.of(attribute, Value.of(value));
    }

    static <T, V> AttributeValueBuilder<T> with(Attribute<T, V> attribute, V value) {
        return new AttributeValueBuilder<>(attribute, value);
    }

    static <T, K, V> AttributeValueBuilder<T> with(MapAttribute<T, K, V> attribute, K key, V value) {
        return new AttributeValueBuilder<>(attribute, key, value);
    }

}
