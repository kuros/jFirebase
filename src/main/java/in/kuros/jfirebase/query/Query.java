package in.kuros.jfirebase.query;


import com.google.cloud.firestore.DocumentSnapshot;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;

import java.util.List;

public interface Query<T> {

    <X> Query<T> whereEqualTo(Attribute<T, X> field, X value);

    <K, V> Query<T> whereEqualTo(MapAttribute<T, K, V> field, K key, V value);

    <X> Query<T> whereEqualTo(String field, X value);

    <X> Query<T> whereNotEqualTo(Attribute<T, X> field, X value);

    <K, V> Query<T> whereNotEqualTo(MapAttribute<T, K, V> field, K key, V value);

    <X> Query<T> whereNotEqualTo(String field, X value);

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

    <X> Query<T> whereArrayContainsAny(Attribute<T, X> field, List<Object> value);

    Query<T> whereArrayContainsAny(String field, List<Object> value);

    <X> Query<T> whereIn(Attribute<T, X> field, List<X> value);

    Query<T> whereIn(String field, List<?> value);

    <X> Query<T> whereNotIn(Attribute<T, X> field, List<X> value);

    Query<T> whereNotIn(String field, List<?> value);

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

    Query<T> startAfter(DocumentSnapshot snapshot);

    Query<T> startAt(Object... values);

    Class<T> getResultType();

}
