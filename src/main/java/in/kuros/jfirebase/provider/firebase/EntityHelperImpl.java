package in.kuros.jfirebase.provider.firebase;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import in.kuros.jfirebase.entity.CreateTime;
import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.EntityDeclarationException;
import in.kuros.jfirebase.entity.EntityParentCache;
import in.kuros.jfirebase.entity.EntityParentCache.MappedClassField;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.UpdateTime;
import in.kuros.jfirebase.exception.PersistenceException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class EntityHelperImpl implements EntityHelper {

    private final Cache<Class<?>, Field> idFields;
    private final Cache<Class<?>, Field> createdFields;
    private final Cache<Class<?>, Field> updatedFields;
    private final Cache<Class<?>, List<Field>> idReferences;

    EntityHelperImpl() {
        idFields = CacheBuilder.newBuilder().build();
        createdFields = CacheBuilder.newBuilder().build();
        updatedFields = CacheBuilder.newBuilder().build();
        idReferences = CacheBuilder.newBuilder().build();
    }

    @Override
    public <T> String getDocumentPath(final T entity) {

        final StringBuilder stringBuilder = getCollectionBuilder(entity);
        final String id = getId(entity);
        if (id != null) {
            stringBuilder.append("/")
                    .append(id);
        }

        return stringBuilder.toString();
    }

    @Override
    public <T> String getCollectionPath(final T entity) {
        return getCollectionBuilder(entity).toString();
    }

    @Override
    public <T> void setId(final T entity, final String id) {
        final Field idField = getIdField(entity.getClass());
        idField.setAccessible(true);

        try {

            if (idField.getType().isEnum()) {
                final Method valueOf = idField.getType().getMethod("valueOf", String.class);
                final Object value = valueOf.invoke(null, id);
                idField.set(entity, value);
            } else {
                idField.set(entity, id);
            }
        } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new EntityDeclarationException("unable to access field: " + idField.getName());
        }
    }

    @Override
    public  <T> String getId(final T entity) {
        return getValueInString(entity, getIdField(entity.getClass()));
    }

    @Override
    public <T> void setCreateTime(final T entity) {
        final Field createTimeField = getCreateTimeField(entity.getClass());
        if (createTimeField != null) {
            createTimeField.setAccessible(true);
            try {
                createTimeField.set(entity, new Date());
            } catch (final IllegalAccessException e) {
                throw new EntityDeclarationException("unable to access field: " + createTimeField.getName());
            }
        }
    }

    @Override
    public <T> boolean setUpdateTime(final T entity) {
        final Field timeField = getUpdateTimeField(entity.getClass());
        if (timeField != null) {
            timeField.setAccessible(true);
            try {
                timeField.set(entity, new Date());
                return true;
            } catch (final IllegalAccessException e) {
                throw new EntityDeclarationException("unable to access field: " + timeField.getName());
            }
        }
        return false;
    }

    private <T> StringBuilder getCollectionBuilder(final T entity) {
        final StringBuilder stringBuilder = new StringBuilder();

        addParentPath(entity, stringBuilder);

        final Entity annotation = EntityHelper.getEntity(entity.getClass());
        stringBuilder.append(annotation.value());
        return stringBuilder;
    }

    private <T> void addParentPath(final T entity, final StringBuilder stringBuilder) {
        final List<MappedClassField> mappedClassFields = EntityParentCache.INSTANCE.getMappedClassFields(entity.getClass());

        for (MappedClassField mappedClassField : mappedClassFields) {

            final String parentId = getValueInString(entity, mappedClassField.getField());
            if (parentId == null) {
                throw new EntityDeclarationException("parent id cannot be null: " + mappedClassField.getField());
            }

            stringBuilder.append(getParentCollection(mappedClassField))
                    .append("/")
                    .append(parentId)
                    .append("/");
        }
    }

    private String getParentCollection(final MappedClassField mappedClassField) {
        final String value;
        if (mappedClassField.getMappedClass() == IdReference.DEFAULT.class) {
            final IdReference reference = mappedClassField.getField().getAnnotation(IdReference.class);
            value = reference.collection();
            if (Strings.isNullOrEmpty(value)) {
                throw new EntityDeclarationException("Id Reference class/collection not provided: " + mappedClassField.getField());
            }
        } else {
            final Entity parentEntity = EntityHelper.getEntity(mappedClassField.getMappedClass());
            value = parentEntity.value();
        }
        return value;
    }

    private Field getIdField(final Class<?> type) {
        try {
            return idFields.get(type, () -> {
                final Field[] declaredFields = type.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.getAnnotation(Id.class) != null) {
                        return declaredField;
                    }
                }

                throw new EntityDeclarationException("@Id not found: " + type);
            });
        } catch (final ExecutionException e) {
            throw new EntityDeclarationException(e);
        }
    }

    private Field getCreateTimeField(final Class<?> type) {
        try {
            return createdFields.get(type, () -> {
                final Field[] declaredFields = type.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.getAnnotation(CreateTime.class) != null) {
                        return declaredField;
                    }
                }

                throw new SkipException();
            });
        } catch (final Exception e) {
            return null;
        }
    }

    public Field getUpdateTimeField(final Class<?> type) {
        try {
            return updatedFields.get(type, () -> {
                final Field[] declaredFields = type.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField.getAnnotation(UpdateTime.class) != null) {
                        return declaredField;
                    }
                }

                throw new SkipException();
            });
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public <T> void validateIdsNotNull(final T object) {
        try {
            final Class<?> type = object.getClass();

            final Set<Field> requiredIdFields = getAllRequiredIdFields(type);

            requiredIdFields.forEach(field -> {
                field.setAccessible(true);
                try {
                    Objects.requireNonNull(field.get(object), "Id/IdReferences are required: " + field);
                } catch (final IllegalAccessException e) {
                    throw new PersistenceException(e);
                }
            });
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> Set<Field> getAllRequiredIdFields(final Class<T> type) {
        try {
            final Set<Field> fields = Sets.newHashSet(getIdReferenceFields(type));
            fields.add(getIdField(type));
            return fields;
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    private List<Field> getIdReferenceFields(final Class<?> type) throws ExecutionException {
        return idReferences.get(type, () -> {
                    final List<Field> references = Lists.newArrayList();
                    final Field[] declaredFields = type.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        if (declaredField.getAnnotation(IdReference.class) != null) {
                            references.add(declaredField);
                        }
                    }

                    return references;
                });
    }

    private <T> String getValueInString(final T entity, final Field declaredField) {
        try {
            declaredField.setAccessible(true);
            final Object id = declaredField.get(entity);
            return id == null ? null : id.toString();
        } catch (final IllegalAccessException e) {
            throw new EntityDeclarationException("Unable to access field: " + declaredField.getName());
        }
    }

    private static class SkipException extends RuntimeException {
    }
}
