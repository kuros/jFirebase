package in.kuros.jfirebase.query;


import in.kuros.jfirebase.metadata.Attribute;

public interface Query<T> {

    <X> Query<T> whereEqualTo(Attribute<T, X> field, X value);

    <X> Query<T> whereEqualTo(String field, X value);

    <X> Query<T> whereGreaterThan(Attribute<T, X> field, X value);

    <X> Query<T> whereGreaterThan(String field, X value);

    <X> Query<T> whereGreaterThanOrEqualTo(Attribute<T, X> field, X value);

    <X> Query<T> whereGreaterThanOrEqualTo(String field, X value);

    <X> Query<T> whereLessThan(Attribute<T, X> field, X value);

    <X> Query<T> whereLessThan(String field, X value);

    <X> Query<T> whereLessThanOrEqualTo(Attribute<T, X> field, X value);

    <X> Query<T> whereLessThanOrEqualTo(String field, X value);

    <X> Query<T> whereArrayContains(Attribute<T, X> field, Object value);

    Query<T> whereArrayContains(String field, Object value);

    Query<T> limit(int limit);

    Query<T> endAt(Object... values);

    Query<T> endBefore(Object... values);

    Query<T> offset(int offset);

    <X> Query<T> orderBy(Attribute<T, X> attribute);

    <X> Query<T> orderByDesc(Attribute<T, X> attribute);

    <X> Query<T> orderBy(String field);

    <X> Query<T> orderByDesc(String field);

    Query<T> select(Attribute<T, ?>... attributes);

    Query<T> select(String... fields);

    Query<T> startAfter(Object... values);

    Query<T> startAt(Object... values);

    Class<T> getResultType();

}
