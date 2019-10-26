package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.UpdateBuilder;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.transaction.WriteBatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        updateBuilder.create(document, entity);
        entityHelper.setId(entity, document.getId());
    }

    @Override
    public <T> void set(final T entity) {
        final DocumentReference document = getDocumentReference(entity);
        entityHelper.setUpdateTime(entity);
        updateBuilder.set(document, entity);
    }

    @Override
    public <T> void set(final T entity, final Attribute<T, ?>... attributes) {
        final List<String> fields = Arrays.stream(attributes).map(Attribute::getName).collect(Collectors.toList());
        if (entityHelper.setUpdateTime(entity)) {
            final String updateField = entityHelper.getUpdateTimeField(entity.getClass()).getName();
            fields.add(updateField);
        }
        final DocumentReference documentReference = getDocumentReference(entity);
        updateBuilder.set(documentReference, entity, SetOptions.mergeFields(fields));
    }

    @Override
    public <T> void set(final List<AttributeValue<T, ?>> attributeValues) {
        if (attributeValues.isEmpty()) {
            return;
        }

        final Class<T> type = attributeValues.get(0).getAttribute().getDeclaringType();
        final T entity = attributeValueHelper.createEntity(type, attributeValues);
        final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);

        if (entityHelper.setUpdateTime(entity)) {
            fieldPaths.add(FieldPath.of(entityHelper.getUpdateTimeField(type).getName()));
        }

        final DocumentReference documentReference = getDocumentReference(entity);
        updateBuilder.set(documentReference, entity, SetOptions.mergeFieldPaths(fieldPaths));

    }

    @Override
    public <T> void remove(final RemoveAttribute<T> removeAttribute) {
        final List<AttributeValue<T, ?>> attributeValues = RemoveAttribute.Helper.getAttributeValues(removeAttribute, FieldValue::delete);
        final Class<T> type = RemoveAttribute.Helper.getDeclaringClass(removeAttribute);
        final T entity = attributeValueHelper.createEntity(type, RemoveAttribute.Helper.getKeys(removeAttribute));
        final Map<String, Object> valueMap = attributeValueHelper.toFieldValueMap(attributeValues);
        final DocumentReference documentReference = getDocumentReference(entity);
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

    private <T> CollectionReference getCollectionReference(final T entity) {
        return firestore.collection(entityHelper.getCollectionPath(entity));
    }

    private <T> DocumentReference getDocumentReference(final T entity) {
        return firestore.document(entityHelper.getDocumentPath(entity));
    }
}
