package in.kuros.jfirebase.transaction;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.RemoveAttribute;

import java.util.List;

public interface WriteBatch {

    <T> void create(T entity);

    <T> void set(T entity);

    <T> void set(T entity, Attribute<T, ?>... attributes);

    <T> void set(List<AttributeValue<T, ?>> attributeValues);

    <T> void remove(RemoveAttribute<T> removeAttribute);

    void update(String path, String field, Object value);

    <T> void delete(T entity);
}
