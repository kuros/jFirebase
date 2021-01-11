package in.kuros.jfirebase.provider.firebase.query;

import com.google.common.collect.Lists;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.provider.firebase.AttributeValueHelper;
import in.kuros.jfirebase.provider.firebase.EntityHelper;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public final class QueryBuilder<T> extends QueryImpl<T> {
    @Getter private final String path;
    @Getter private Class<T> resultType;
    @Getter private boolean collectionGroup;
    private final AttributeValueHelper attributeValueHelper;

    @SuppressWarnings("unchecked")

    private QueryBuilder(final String path, final Class<T> resultType, final boolean collectionGroup) {
        this.path = path;
        this.resultType = resultType;
        this.attributeValueHelper = new AttributeValueHelper();
        this.collectionGroup = collectionGroup;
    }

    public static <S> QueryBuilder<S> collection(final Class<S> type) {
        return new QueryBuilder<>(getCollectionName(type), type, false);
    }

    public static <S> QueryBuilder<S> collection(final String path, final Class<S> resultType) {
        return new QueryBuilder<>(path, resultType, false);
    }

    public static <S> QueryBuilder<S> collectionGroup(final Class<S> type) {
        return new QueryBuilder<>(getCollectionName(type), type, true);
    }

    public static <S> QueryBuilder<S> collectionGroup(final String path, final Class<S> resultType) {
        return new QueryBuilder<>(path, resultType, true);
    }

    public <S> QueryBuilder<S> subCollection(final Class<S> type) {
        return new QueryBuilder<>(path + "/" + getCollectionName(type), type, collectionGroup);
    }

    public <S> QueryBuilder<S> subCollection(final String collection, final Class<S> resultClass) {
        return new QueryBuilder<>(path + "/" + collection, resultClass, collectionGroup);
    }

    public QueryBuilder<T> withId(final String id) {
        return new QueryBuilder<>(path + "/" + id, resultType, collectionGroup);
    }

    public QueryBuilder<T> withPath(final String pathValue) {
        return new QueryBuilder<>(path + pathValue, resultType, collectionGroup);
    }

    @SuppressWarnings("unchecked")
    public static <T, V> QueryBuilder<T> withKey(Attribute<T, V> attribute, V value) {
        return withKeys(Lists.newArrayList(AttributeValue.of(attribute, value)));
    }

    public static <T> QueryBuilder<T> withKeys(List<AttributeValue<T, ?>> attributeValues) {
        if (Objects.isNull(attributeValues) || attributeValues.size() == 0 ) {
            throw new IllegalArgumentException("key Attriutes are required");
        }
        final AttributeValueHelper attributeValueHelper = new AttributeValueHelper();
        final T entity = attributeValueHelper.createEntity(attributeValues);
        final String documentPath = EntityHelper.INSTANCE.getDocumentPath(entity);

        return new QueryBuilder<>(documentPath, attributeValues.get(0).getAttribute().getDeclaringType(), false);
    }

    public <S> QueryBuilder<S> resultType(final Class<S> resultClass) {
        return new QueryBuilder<>(path, resultClass, collectionGroup);
    }

    private static String getCollectionName(final Class<?> type) {
        return EntityHelper.getMappedCollection(type);
    }
}
