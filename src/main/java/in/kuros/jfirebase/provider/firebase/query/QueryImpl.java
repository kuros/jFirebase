package in.kuros.jfirebase.provider.firebase.query;

import com.google.cloud.firestore.DocumentSnapshot;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;
import in.kuros.jfirebase.provider.firebase.EntityHelper;
import in.kuros.jfirebase.query.Query;
import in.kuros.jfirebase.util.PropertyNamingStrategy;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.cloud.firestore.Query.Direction.DESCENDING;

abstract class QueryImpl<T> implements Query<T> {

    private final PropertyNamingStrategy namingStrategy;
    private List<Function<com.google.cloud.firestore.Query, com.google.cloud.firestore.Query>> queries;

    QueryImpl() {
        this.queries = new ArrayList<>();
        this.namingStrategy = EntityHelper.INSTANCE.getPropertyNamingStrategy();
    }

    @Override
    public <X> Query<T> whereEqualTo(final Attribute<T, X> field, final X value) {
        whereEqualTo(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <K, V> Query<T> whereEqualTo(final MapAttribute<T, K, V> field, final K key, final V value) {
        return whereEqualTo(getNameWithStrategy(field) + "." + key, value);
    }

    @Override
    public <X> Query<T> whereEqualTo(final String field, final X value) {
        queries.add(query -> query.whereEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereNotEqualTo(final Attribute<T, X> field, final X value) {
        whereNotEqualTo(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <K, V> Query<T> whereNotEqualTo(final MapAttribute<T, K, V> field, final K key, final V value) {
        return whereNotEqualTo(getNameWithStrategy(field) + "." + key, value);
    }

    @Override
    public <X> Query<T> whereNotEqualTo(final String field, final X value) {
        queries.add(query -> query.whereNotEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThan(final Attribute<T, X> field, final X value) {
        whereGreaterThan(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThan(final String field, final X value) {
        queries.add(query -> query.whereGreaterThan(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThanOrEqualTo(final Attribute<T, X> field, final X value) {
        whereGreaterThanOrEqualTo(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <X> Query<T> whereGreaterThanOrEqualTo(final String field, final X value) {
        queries.add(query -> query.whereGreaterThanOrEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereLessThan(final Attribute<T, X> field, final X value) {
        whereLessThan(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <X> Query<T> whereLessThan(final String field, final X value) {
        queries.add(query -> query.whereLessThan(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereLessThanOrEqualTo(final Attribute<T, X> field, final X value) {
        whereLessThanOrEqualTo(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public <X> Query<T> whereLessThanOrEqualTo(final String field, final X value) {
        queries.add(query -> query.whereLessThanOrEqualTo(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereArrayContains(final Attribute<T, X> field, final Object value) {
        whereArrayContains(getNameWithStrategy(field), value);
        return this;
    }

    @Override
    public Query<T> whereArrayContains(final String field, final Object value) {
        queries.add(query -> query.whereArrayContains(field, value));
        return this;
    }

    @Override
    public <X> Query<T> whereArrayContainsAny(final Attribute<T, X> field, final List<Object> values) {
        whereArrayContainsAny(getNameWithStrategy(field), values);
        return this;
    }

    @Override
    public Query<T> whereArrayContainsAny(final String field, final List<Object> values) {
        queries.add(query -> query.whereArrayContainsAny(field, values));
        return this;
    }

    @Override
    public <X> Query<T> whereIn(final Attribute<T, X> field, final List<X> values) {
        whereIn(getNameWithStrategy(field), values);
        return this;
    }

    @Override
    public Query<T> whereIn(final String field, final List<?> values) {
        queries.add(query -> query.whereIn(field, values));
        return this;
    }

    @Override
    public <X> Query<T> whereNotIn(final Attribute<T, X> field, final List<X> values) {
        whereNotIn(getNameWithStrategy(field), values);
        return this;
    }

    private <X> String getNameWithStrategy(Attribute<T, X> field) {
        return namingStrategy.translate(field.getName());
    }

    @Override
    public Query<T> whereNotIn(final String field, final List<?> values) {
        queries.add(query -> query.whereNotIn(field, values));
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
        queries.add(query -> query.orderBy(getNameWithStrategy(attribute)));
        return this;
    }

    @Override
    public <X> Query<T> orderByDesc(final Attribute<T, X> attribute) {
        queries.add(query -> query.orderBy(getNameWithStrategy(attribute), DESCENDING));
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
    public Query<T> startAfter(@Nonnull final DocumentSnapshot snapshot) {
        queries.add(query -> query.startAfter(snapshot));
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
