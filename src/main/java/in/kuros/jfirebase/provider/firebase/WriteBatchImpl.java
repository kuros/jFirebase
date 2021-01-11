package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.UpdateBuilder;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.metadata.SetAttribute;
import in.kuros.jfirebase.metadata.SetAttribute.Helper;
import in.kuros.jfirebase.metadata.UpdateAttribute;
import in.kuros.jfirebase.metadata.ValuePath;
import in.kuros.jfirebase.transaction.WriteBatch;
import in.kuros.jfirebase.util.BeanMapper;
import in.kuros.jfirebase.util.ClassMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WriteBatchImpl implements WriteBatch {

    private final Firestore firestore;
    private final UpdateBuilder updateBuilder;
    private final EntityHelper entityHelper;
    private final AttributeValueHelper attributeValueHelper;

    WriteBatchImpl(final Firestore firestore, final UpdateBuilder updateBuilder) {
        this.firestore = firestore;
        this.updateBuilder = updateBuilder;
        this.entityHelper = EntityHelperImpl.INSTANCE;
        this.attributeValueHelper = new AttributeValueHelper();
    }

    @Override
    public <T> void create(final T entity) {
        final CollectionReference collectionReference = getCollectionReference(entity);
        final String id = entityHelper.getId(entity);
        entityHelper.setCreateTime(entity);
        final DocumentReference document = id == null ? collectionReference.document() : collectionReference.document(id);
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(getBeanClass(entity));

        updateBuilder.create(document, beanMapper.serialize(entity));
        entityHelper.setId(entity, document.getId());
    }

    @Override
    public <T> void set(final T entity) {
        final DocumentReference document = getDocumentReference(entity);
        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(getBeanClass(entity));
        updateBuilder.set(document, beanMapper.serialize(entity));
    }

    @Override
    public <T> void set(final SetAttribute<T> setAttribute) {
        final Class<T> declaringClass = Helper.getDeclaringClass(setAttribute);
        final List<AttributeValue<T, ?>> keyAttributes = SetAttribute.Helper.getKeys(setAttribute);
        final List<AttributeValue<T, ?>> attributeValues = SetAttribute.Helper.getAttributeValues(setAttribute);
        final List<ValuePath<?>> valuePaths = SetAttribute.Helper.getValuePaths(setAttribute);

        final Map<String, Object> valueMap = attributeValueHelper.convertToObjectMap(attributeValues);
        final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);

        attributeValueHelper.addValuePaths(valueMap, valuePaths);
        final List<FieldPath> valueFieldPaths = attributeValueHelper.convertValuePathToFieldPaths(valuePaths);
        fieldPaths.addAll(valueFieldPaths);

        final Optional<String> updateTimeField = entityHelper.getUpdateTimeFieldName(declaringClass);
        updateTimeField.ifPresent(name -> {
            valueMap.put(name, new Date());
            fieldPaths.add(FieldPath.of(name));
        });

        final DocumentReference documentReference = firestore.document(entityHelper.getDocumentPath(keyAttributes));
        updateBuilder.set(documentReference, valueMap, SetOptions.mergeFieldPaths(fieldPaths));
    }

    @Override
    public <T> void remove(final RemoveAttribute<T> removeAttribute) {
        final List<AttributeValue<T, ?>> attributeValues = RemoveAttribute.Helper.getAttributeValues(removeAttribute, FieldValue::delete);
        final List<ValuePath<?>> valuePaths = RemoveAttribute.Helper.getValuePaths(removeAttribute);
        final Map<String, Object> valueMap = attributeValueHelper.convertToObjectMap(attributeValues);
        final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);
        attributeValueHelper.addValuePaths(valueMap, valuePaths);
        final List<FieldPath> valueFieldPaths = attributeValueHelper.convertValuePathToFieldPaths(valuePaths);
        fieldPaths.addAll(valueFieldPaths);
        final String documentPath = entityHelper.getDocumentPath(RemoveAttribute.Helper.getKeys(removeAttribute));
        final DocumentReference documentReference = firestore.document(documentPath);
        updateBuilder.set(documentReference, valueMap, SetOptions.mergeFieldPaths(fieldPaths));
    }

    @Override
    public <T> void update(final UpdateAttribute<T> updateAttribute) {
        final List<AttributeValue<T, ?>> attributeValues = UpdateAttribute.Helper.getAttributeValues(updateAttribute);
        final Map<String, Object> valueMap = attributeValueHelper.convertToObjectMap(attributeValues);
        attributeValueHelper.addValuePaths(valueMap, UpdateAttribute.Helper.getValuePaths(updateAttribute));
        final Optional<String> updateTimeField = entityHelper.getUpdateTimeFieldName(UpdateAttribute.Helper.getDeclaringClass(updateAttribute));
        updateTimeField.ifPresent(name -> {
            valueMap.put(name, new Date());
        });

        final String documentPath = entityHelper.getDocumentPath(UpdateAttribute.Helper.getKeys(updateAttribute));
        final DocumentReference documentReference = firestore.document(documentPath);
        updateBuilder.update(documentReference, valueMap);
    }

    @Override
    public void update(final String path, final String field, final Object value) {
        final DocumentReference document = firestore.document(path);
        updateBuilder.update(document, field, value);
    }

    @Override
    public <T> void delete(final T entity) {
        final DocumentReference document = getDocumentReference(entity);
        updateBuilder.delete(document);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getBeanClass(final T entity) {
        return (Class<T>) entity.getClass();
    }

    private <T> CollectionReference getCollectionReference(final T entity) {
        return firestore.collection(entityHelper.getCollectionPath(entity));
    }

    private <T> DocumentReference getDocumentReference(final T entity) {
        return firestore.document(entityHelper.getDocumentPath(entity));
    }
}
