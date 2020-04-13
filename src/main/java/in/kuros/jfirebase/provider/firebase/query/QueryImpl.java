package in.kuros.jfirebase.provider.firebase.query;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;
import in.kuros.jfirebase.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.cloud.firestore.Query.Direction.DESCENDING;

abstract class QueryImpl<T> implements Query<T> {

    private List<Function<com.google.cloud.firestore.Query, com.google.cloud.firestore.Query>> queries;

    QueryImpl() {
        this.queries = new ArrayList<>();
    }

    @Override
    public <X> Query<T> whereEqualTo(final Attribute<T, X> field, final X value) {
        whereEqualTo(field.getName(), value);
        return this;
    }

    @Override
    public <K, V> Query<T> whereEqualTo(final MapAttribute<T, K, V> field, final K key, final V value) {
        return whereEqualTo(field.getName() + "." + key, value);
    }

    @Override
    public <X> Query<T> whereEqualTo(final String field, final X value) {
        queries.add(query -> query.whereEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThan(final Attribute<T, X> field, final X value) {
        whereGreaterThan(field.getName(), value);
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThan(final String field, final X value) {
        queries.add(query -> query.whereGreaterThan(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThanOrEqualTo(final Attribute<T, X> field, final X value) {
        whereGreaterThanOrEqualTo(field.getName(), value);
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThanOrEqualTo(final String field, final X value) {
        queries.add(query -> query.whereGreaterThanOrEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereLessThan(final Attribute<T, X> field, final X value) {
        whereLessThan(field.getName(), value);
        return this;
    }

    @Override
    public <X> Query<T> whereLessThan(final String field, final X value) {
        queries.add(query -> query.whereLessThan(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereLessThanOrEqualTo(final Attribute<T, X> field, final X value) {
        whereLessThanOrEqualTo(field.getName(), value);
        return this;
    }

    @Override
    public <X> Query<T> whereLessThanOrEqualTo(final String field, final X value) {
        queries.add(query -> query.whereLessThanOrEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereArrayContains(final Attribute<T, X> field, final Object value) {
        whereArrayContains(field.getName(), value);
        return this;
    }

    @Override
    public Query<T> whereArrayContains(final String field, final Object value) {
        queries.add(query -> query.whereArrayContains(field, value));
        return this;
    }

    @Override
    public Query<T> limit(final int limit) {
        queries.add(query -> query.limit(limit));
        return this;
    }

    @Override
    public Query<T> endAt(final Object... values) {
        queries.add(query -> query.endAt(values));
        return this;
    }

    @Override
    public Query<T> endBefore(final Object... values) {
        queries.add(query -> query.endBefore(values));
        return this;
    }

    @Override
    public Query<T> offset(final int offset) {
        queries.add(query -> query.offset(offset));
        return this;
    }

    @Override
    public <X> Query<T> orderBy(final Attribute<T, X> attribute) {
        queries.add(query -> query.orderBy(attribute.getName()));
        return this;
    }

    @Override
    public <X> Query<T> orderByDesc(final Attribute<T, X> attribute) {
        queries.add(query -> query.orderBy(attribute.getName(), DESCENDING));
        return this;
    }

    @Override
    public Query<T> orderBy(final String field) {
        queries.add(query -> query.orderBy(field));
        return this;
    }

    @Override
    public Query<T> orderByDesc(final String field) {
        queries.add(query -> query.orderBy(field, DESCENDING));
        return this;
    }

    @SafeVarargs
    @Override
    public final Query<T> select(final Attribute<T, ?>... attributes) {
        final String[] fields = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);
        queries.add(query -> query.select(fields));
        return this;
    }

    @Override
    public Query<T> select(final String... fields) {
        queries.add(query -> query.select(fields));
        return this;
    }

    @Override
    public Query<T> startAfter(final Object... values) {
        queries.add(query -> query.startAfter(values));
        return this;
    }

    @Override
    public Query<T> startAt(final Object... values) {
        queries.add(query -> query.startAt(values));
        return this;
    }

    public com.google.cloud.firestore.Query build(final com.google.cloud.firestore.Query query) {

        Stream<com.google.cloud.firestore.Query> out = Stream.of(query);
        for (Function<com.google.cloud.firestore.Query, com.google.cloud.firestore.Query> queryFunction : queries) {
            out = out.map(queryFunction);
        }

        return out.findFirst().orElseThrow(RuntimeException::new);
    }
}
