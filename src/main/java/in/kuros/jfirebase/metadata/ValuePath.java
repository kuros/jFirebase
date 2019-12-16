package in.kuros.jfirebase.metadata;

import lombok.Data;

@Data
public class ValuePath<V> {
    private final V value;
    private final String[] path;

    public ValuePath(final V value, final String... path) {
        this.value = value;
        this.path = path;
    }

    public static <V> ValuePath<V> of(final V value, final String... path) {
        return new ValuePath<>(value, path);
    }
}
