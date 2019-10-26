package in.kuros.jfirebase.metadata;

import java.lang.reflect.Field;
import java.util.Map;

public interface MapAttribute<T, K, V> extends Attribute<T, Map<K, V>> {

    Field getField();

    Class<T> getDeclaringType();

    String getName();

}
