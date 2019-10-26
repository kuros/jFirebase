package in.kuros.jfirebase.metadata;

public interface Value<V> {
    V getValue();

    static <V> Value<V> of(V value) {
        return () -> value;
    }
}
