package in.kuros.jfirebase.metadata;

import lombok.Data;

@Data(staticConstructor = "of")
public class AttributeValueImpl<T, V> implements AttributeValue<T, V> {

    private final Attribute<T, V> attribute;
    private final Value value;

    @Override
    public Attribute<T, V> getAttribute() {
        return attribute;
    }

    @Override
    public Value getAttributeValue() {
        return value;
    }
}
