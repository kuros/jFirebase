package in.kuros.jfirebase.provider.firebase.query;

import com.google.common.collect.Lists;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.UpdateAttribute;
import in.kuros.jfirebase.provider.firebase.AttributeValueHelper;
import in.kuros.jfirebase.provider.firebase.EntityHelper;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class QueryBuilder<T> extends QueryImpl<T> {
    @Getter private final String path;
    @Getter private Class<T> resultType;
    private final AttributeValueHelper attributeValueHelper;

    @SuppressWarnings("unchecked")
    private QueryBuilder(final Class<T> type) {
        this(getCollectionName(type), type);
    }

    private QueryBuilder(final String path, final Class<T> resultType) {
        this.path = path;
        this.resultType = resultType;
        this.attributeValueHelper = new AttributeValueHelper();
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

        return new QueryBuilder<>(documentPath, attributeValues.get(0).getAttribute().getDeclaringType());
    }

    public <S> QueryBuilder<S> resultType(final Class<S> resultClass) {
        return new QueryBuilder<>(path, resultClass);
    }

    private static String getCollectionName(final Class<?> type) {
        return EntityHelper.getMappedCollection(type);
    }
}
