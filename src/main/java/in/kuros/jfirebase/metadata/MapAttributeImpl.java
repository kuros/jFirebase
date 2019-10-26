package in.kuros.jfirebase.metadata;

import java.lang.reflect.Field;

public class MapAttributeImpl<T, K, V> implements MapAttribute<T, K, V> {

    private final Field field;

    MapAttributeImpl(final Field field) {
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
