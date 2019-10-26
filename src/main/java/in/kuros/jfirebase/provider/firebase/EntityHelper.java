package in.kuros.jfirebase.provider.firebase;

import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.EntityDeclarationException;

import java.lang.reflect.Field;
import java.util.Set;

public interface EntityHelper {

    <T> String getDocumentPath(T entity);

    <T> String getCollectionPath(T entity);

    <T> void setId(T entity, String id);

    <T> String getId(T entity);

    <T> void setCreateTime(T entity);

    <T> boolean setUpdateTime(T entity);

    Field getUpdateTimeField(Class<?> type);

    <T> Set<Field> getAllRequiredIdFields(Class<T> type);

    <T> void validateIdsNotNull(T object);

    static String getMappedCollection(final Class<?> aClass) {
        return getEntity(aClass).value();
    }

    static Entity getEntity(final Class<?> aClass) {
        final Entity annotation = aClass.getAnnotation(Entity.class);
        if (annotation == null) {
            throw new EntityDeclarationException("No annotation found for Entity: " + aClass);
        }
        return annotation;
    }

    EntityHelper INSTANCE = new EntityHelperImpl();
}
