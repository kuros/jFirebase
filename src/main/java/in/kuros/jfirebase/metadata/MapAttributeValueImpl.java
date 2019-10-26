package in.kuros.jfirebase.metadata;

import lombok.Data;

import java.util.Map;

@Data
class MapAttributeValueImpl<T, K, V> implements MapAttributeValue<T, K, V> {

    private final Attribute<T, Map<K, V>> attribute;
    private final K key;
    private final Value mapValue;
    private final Value attributeValue;
    private final boolean keyUpdate;

    MapAttributeValueImpl(final Attribute<T, Map<K, V>> attribute, final K key, final Value value) {
        this.attribute = attribute;
        this.key = key;
        this.mapValue = value;
        this.attributeValue = null;
        this.keyUpdate = true;
    }

    MapAttributeValueImpl(final Attribute<T, Map<K, V>> attribute, final Value value) {
        this.attribute = attribute;
        this.key = null;
        this.mapValue = null;
        this.attributeValue = value;
        this.keyUpdate = false;
    }
}
