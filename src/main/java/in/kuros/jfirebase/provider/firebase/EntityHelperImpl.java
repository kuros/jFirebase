package in.kuros.jfirebase.provider.firebase;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.EntityDeclarationException;
import in.kuros.jfirebase.entity.EntityParentCache;
import in.kuros.jfirebase.entity.EntityParentCache.FieldCollectionMapping;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.Value;
import in.kuros.jfirebase.util.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityHelperImpl implements EntityHelper {
    private PropertyNamingStrategy propertyNamingStrategy;
    private String collectionNamePrefix = "";

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> void setId(final T entity, final String id) {

        final Class<T> aClass = (Class<T>) entity.getClass();
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(aClass);

        final Optional<String> idField = beanMapper.getId();
        if (!idField.isPresent()) {
            throw new EntityDeclarationException("unable to access id field: " + entity.getClass());
        }

        beanMapper.setValue(entity, idField.get(), id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> String getId(final T entity) {
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper((Class<T>) entity.getClass());
        return beanMapper.getId()
                .map(idField -> beanMapper.getValue(entity, idField))
                .map(Object::toString)
                .orElse(null);

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void setCreateTime(final T entity) {
        setCreateTime(entity, new Date());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> boolean setUpdateTime(final T entity) {
        return setUpdateTime(entity, new Date());
    }

    @Override public <T> boolean setCreateTime(T entity, Date date) {
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper((Class<T>) entity.getClass());
        final Optional<String> createTime = beanMapper.getCreateTime();
        if (createTime.isPresent()) {
            String createTimeField = createTime.get();
            if (isFieldLocalDateTime(beanMapper, createTimeField)) {
                final LocalDateTime localDateTime = DateHelper.toLocalDateTime(date);
                beanMapper.setValue(entity, createTimeField, localDateTime);
            } else {
                beanMapper.setValue(entity, createTimeField, date);
            }
            return true;
        }

        return false;
    }

    private static <T> boolean isFieldLocalDateTime(BeanMapper<T> beanMapper, String fieldName) {
        final String className = beanMapper.getFields().get(fieldName).getType().getName();
        return className.equals(LocalDateTime.class.getName());
    }

    @Override public <T> Optional<Date> getCreateTime(T entity) {
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper((Class<T>) entity.getClass());
        Optional<String> optional = beanMapper.getCreateTime();
        return optional.map(s -> {
            if (isFieldLocalDateTime(beanMapper, s)) {
                return DateHelper.toDate((LocalDateTime) beanMapper.getValue(entity, s));
            }

            return (Date) beanMapper.getValue(entity, s);
        });
    }

    @Override public <T> boolean setUpdateTime(T entity, Date date) {
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper((Class<T>) entity.getClass());
        final Optional<String> updateTime = beanMapper.getUpdateTime();
        if (updateTime.isPresent()) {
            String updatedTimeField = updateTime.get();
            if (isFieldLocalDateTime(beanMapper, updatedTimeField)) {
                final LocalDateTime localDateTime = DateHelper.toLocalDateTime(date);
                beanMapper.setValue(entity, updatedTimeField, localDateTime);
            } else {
                beanMapper.setValue(entity, updatedTimeField, date);
            }
            return true;
        }

        return false;
    }

    private <T> StringBuilder getCollectionBuilder(final T entity) {
        final StringBuilder stringBuilder = new StringBuilder();

        addParentPath(entity, stringBuilder);

        final Entity annotation = EntityHelper.getEntity(entity.getClass());
        stringBuilder.append(collectionNamePrefix);
        stringBuilder.append(annotation.value());
        return stringBuilder;
    }

    private <T> void addParentPath(final T entity, final StringBuilder stringBuilder) {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(entity.getClass());

        for (FieldCollectionMapping mappedClassField : mappedClassFields) {

            final String parentId = getValueInString(entity, mappedClassField);
            if (parentId == null) {
                throw new EntityDeclarationException("parent id cannot be null: " + mappedClassField.getField());
            }

            stringBuilder.append(getParentCollection(mappedClassField))
                    .append("/")
                    .append(parentId)
                    .append("/");
        }
    }

    private String getParentCollection(final FieldCollectionMapping mappedClassField) {
        final String value;
        if (mappedClassField.getMappedClass() == IdReference.DEFAULT.class) {
            value = mappedClassField.getCollection();
            if (Strings.isNullOrEmpty(value)) {
                throw new EntityDeclarationException("Id Reference class/collection not provided: " + mappedClassField.getField());
            }
        } else {
            final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(mappedClassField.getMappedClass());
            value = beanMapper.getEntity().value();
        }
        return value;
    }

    @Override
    public Optional<String> getUpdateTimeFieldName(final Class<?> type) {
        return ClassMapper.getBeanMapper(type).getUpdateTime();
    }

    @Override
    public Optional<String> getCreateTimeFieldName(final Class<?> type) {
        return ClassMapper.getBeanMapper(type).getCreateTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void validateIdsNotNull(final T object) {
        try {
            final Class<T> type = (Class<T>) object.getClass();
            final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(type);
            final Set<String> requiredIdFields = getAllRequiredIdFields(type);
            requiredIdFields.forEach(field -> Objects.requireNonNull(beanMapper.getValue(object, field), "Id/IdReferences are required: " + field));
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public String getMappedCollection(Class<?> aClass) {
        return collectionNamePrefix + EntityHelper.getEntity(aClass).value();
    }

    @Override
    public <T> Set<String> getAllRequiredIdFields(final Class<T> type) {
        try {
            final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(type);
            final Set<String> properties = Sets.newHashSet(beanMapper.getIdReferences().keySet());
            beanMapper.getId().ifPresent(properties::add);
            return properties;
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> String getDocumentPath(final List<AttributeValue<T, ?>> attributeValues) {
        final Class<T> declaringClass = getDeclaringClass(attributeValues);
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(declaringClass);
        final Optional<String> idField = beanMapper.getId();
        if (!idField.isPresent()) {
            throw new EntityDeclarationException("@Id not found for class: " + declaringClass);
        }

        final String idValue = attributeValues.stream()
                .filter(attr -> attr.getAttribute().getName().equals(idField.get()))
                .findFirst()
                .map(AttributeValue::getAttributeValue)
                .map(Value::getValue)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Id value not set"));

        return getCollectionPath(attributeValues) + "/" + idValue;
    }

    @Override
    public <T> String getCollectionPath(final List<AttributeValue<T, ?>> attributeValues) {
        final Class<T> declaringClass = getDeclaringClass(attributeValues);
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(declaringClass);
        final Entity entity = beanMapper.getEntity();

        return getParentPath(attributeValues) + entity.value();
    }

    private <T> String getParentPath(final List<AttributeValue<T, ?>> attributeValues) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Class<T> declaringClass = getDeclaringClass(attributeValues);

        final Map<String, AttributeValue<T, ?>> valueMap = attributeValues.stream()
                .collect(Collectors.toMap(attr -> attr.getAttribute().getName(), Function.identity()));

        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(declaringClass);

        for (FieldCollectionMapping mappedClassField : mappedClassFields) {

            final String parentId = valueMap.get(mappedClassField.getField()).getAttributeValue().getValue().toString();
            if (parentId == null) {
                throw new EntityDeclarationException("parent id cannot be null: " + mappedClassField.getField());
            }

            stringBuilder.append(getParentCollection(mappedClassField))
                    .append("/")
                    .append(parentId)
                    .append("/");
        }

        return stringBuilder.toString();
    }

    private <T> Class<T> getDeclaringClass(final List<AttributeValue<T, ?>> attributeValues) {
        return attributeValues.stream().findFirst().map(AttributeValue::getAttribute).map(Attribute::getDeclaringType).orElseThrow(() -> new IllegalArgumentException("Keys not set"));
    }


    @SuppressWarnings("unchecked")
    private <T> String getValueInString(final T entity, final FieldCollectionMapping declaredField) {
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper((Class<T>) entity.getClass());
        return beanMapper.getValue(entity, declaredField.getField()).toString();
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        if (Objects.isNull(propertyNamingStrategy)) {
            return PropertyNamingStrategies.NONE;
        }
        return propertyNamingStrategy;
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
    }

    @Override
    public void setCollectionNamePrefix(String collectionNamePrefix) {
        this.collectionNamePrefix = collectionNamePrefix;
    }
}
