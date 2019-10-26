package in.kuros.jfirebase.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeValueBuilder<T> {
    private final List<AttributeValue<T, ?>> attributes;

    <V> AttributeValueBuilder(final Attribute<T, V> attribute, final V value) {
        this.attributes = new ArrayList<>();
        this.attributes.add(AttributeValue.of(attribute, value));
    }

    <K, V> AttributeValueBuilder(final MapAttribute<T, K, V> attribute, final K key, final V value) {
        this.attributes = new ArrayList<>();
        this.attributes.add(MapAttributeValue.of(attribute, key, value));
    }

    public <V> AttributeValueBuilder<T> with(final Attribute<T, V> attribute, final V value) {
        this.attributes.add(AttributeValue.of(attribute, value));
        return this;
    }

    public <K, V> AttributeValueBuilder<T> with(final MapAttribute<T, K, V> attribute, final K key, final V value) {
        this.attributes.add(MapAttributeValue.of(attribute, key, value));
        return this;
    }

    public <K, V> AttributeValueBuilder<T> with(final MapAttribute<T, K, V> attribute, final Map<K, V> value) {
        this.attributes.add(MapAttributeValue.of(attribute, value));
        return this;
    }

    public List<AttributeValue<T, ?>> build() {
        return attributes;
    }
}
