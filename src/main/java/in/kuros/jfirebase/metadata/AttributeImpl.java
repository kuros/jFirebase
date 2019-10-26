package in.kuros.jfirebase.metadata;

import java.lang.reflect.Field;

public class AttributeImpl<T, X> implements Attribute<T, X> {

    private final Field field;

    public AttributeImpl(final Field field) {
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Class<T> getDeclaringType() {
        return (Class<T>) field.getDeclaringClass();
    }

    @Override
    public String getName() {
        return field.getName();
    }
}
