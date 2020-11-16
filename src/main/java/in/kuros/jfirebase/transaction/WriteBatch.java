package in.kuros.jfirebase.transaction;

import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.metadata.SetAttribute;
import in.kuros.jfirebase.metadata.UpdateAttribute;

public interface WriteBatch {

    <T> void create(T entity);

    <T> void set(T entity);

    <T> void set(SetAttribute<T> setAttribute);

    <T> void remove(RemoveAttribute<T> removeAttribute);

    <T> void update(final UpdateAttribute<T> updateAttribute);

    void update(String path, String field, Object value);

    <T> void delete(T entity);
}
