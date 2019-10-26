package in.kuros.jfirebase.metadata;

import java.lang.reflect.Field;

public interface Attribute<T, V> {

    Field getField();

    Class<T> getDeclaringType();

    String getName();
}
