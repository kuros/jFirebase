package in.kuros.jfirebase.provider.firebase.query;

import in.kuros.jfirebase.provider.firebase.EntityHelper;
import lombok.Getter;

public final class QueryBuilder<T> extends QueryImpl<T> {
    @Getter private final String path;
    @Getter private Class<T> resultType;

    @SuppressWarnings("unchecked")
    private QueryBuilder(final Class<T> type) {
        path = getCollectionName(type);
        resultType = type;
    }

    private QueryBuilder(final String path, final Class<T> resultType) {
        this.path = path;
        this.resultType = resultType;
    }

    public static <S> QueryBuilder<S> collection(final Class<S> type) {
        return new QueryBuilder<>(type);
    }

    public static <S> QueryBuilder<S> collection(final String path, final Class<S> resultType) {
        return new QueryBuilder<>(path, resultType);
    }

    public <S> QueryBuilder<S> subCollection(final Class<S> type) {
        return new QueryBuilder<>(path + "/" + getCollectionName(type), type);
    }

    public <S> QueryBuilder<S> subCollection(final String collection, final Class<S> resultClass) {
        return new QueryBuilder<>(path + "/" + collection, resultClass);
    }

    public QueryBuilder<T> withId(final String id) {
        return new QueryBuilder<>(path + "/" + id, resultType);
    }

    public QueryBuilder<T> withPath(final String pathValue) {
        return new QueryBuilder<>(path + pathValue, resultType);
    }

    private String getCollectionName(final Class<?> type) {
        return EntityHelper.getMappedCollection(type);
    }
}
