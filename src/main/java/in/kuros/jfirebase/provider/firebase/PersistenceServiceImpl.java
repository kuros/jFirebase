package in.kuros.jfirebase.provider.firebase;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.common.collect.Lists;
import in.kuros.jfirebase.PersistenceService;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.metadata.UpdateAttribute;
import in.kuros.jfirebase.provider.firebase.query.QueryBuilder;
import in.kuros.jfirebase.query.Query;
import in.kuros.jfirebase.transaction.Transaction;
import in.kuros.jfirebase.transaction.WriteBatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class PersistenceServiceImpl implements PersistenceService {

    private final Firestore firestore;
    private final EntityHelper entityHelper;
    private final AttributeValueHelper attributeValueHelper;

    PersistenceServiceImpl(final Firestore firestore) {
        this.firestore = firestore;
        this.entityHelper = EntityHelper.INSTANCE;
        this.attributeValueHelper = new AttributeValueHelper();
    }

    @Override
    public <T> T create(final T entity) {

        try {
            final CollectionReference collectionReference = getCollectionReference(entity);
            final String id = entityHelper.getId(entity);
            final DocumentReference documentReference = id == null ? collectionReference.document() : collectionReference.document(id);
            entityHelper.setCreateTime(entity);
            documentReference.create(entity).get();
            entityHelper.setId(entity, documentReference.getId());
            return entity;
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    @SafeVarargs
    @Override
    public final <T> void set(final T... entities) {
        final com.google.cloud.firestore.WriteBatch batch = firestore.batch();

        for (T entity : entities) {
            entityHelper.setUpdateTime(entity);
            final DocumentReference documentReference = getDocumentReference(entity);
            batch.set(documentReference, entity);
        }

        try {
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void set(final T entity, final Attribute<T, ?> attribute) {
        final DocumentReference documentReference = getDocumentReference(entity);

        final List<String> fields = Lists.newArrayList(attribute.getName());

        if (entityHelper.setUpdateTime(entity)) {
            final String updateFieldName = entityHelper.getUpdateTimeField(entity.getClass()).getName();
            fields.add(updateFieldName);
        }

        try {
            documentReference.set(entity, SetOptions.mergeFields(fields)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }


    @Override
    public <T> void set(final List<AttributeValue<T, ?>> attributeValues) {
        if (attributeValues.isEmpty()) {
            return;
        }

        final Class<T> type = attributeValues.get(0).getAttribute().getDeclaringType();

        try {
            final T entity = attributeValueHelper.createEntity(type, attributeValues);

            final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);

            if (entityHelper.setUpdateTime(entity)) {
                fieldPaths.add(FieldPath.of(entityHelper.getUpdateTimeField(type).getName()));
            }

            final DocumentReference documentReference = getDocumentReference(entity);
            documentReference.set(entity, SetOptions.mergeFieldPaths(fieldPaths)).get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void remove(final RemoveAttribute<T> removeAttribute) {
        final List<AttributeValue<T, ?>> attributeValues = RemoveAttribute.Helper.getAttributeValues(removeAttribute, FieldValue::delete);
        final Class<T> type = RemoveAttribute.Helper.getDeclaringClass(removeAttribute);
        try {
            final T entity = attributeValueHelper.createEntity(type, RemoveAttribute.Helper.getKeys(removeAttribute));

            final Map<String, Object> valueMap = attributeValueHelper.toFieldValueMap(attributeValues);

            final DocumentReference documentReference = getDocumentReference(entity);
            documentReference.update(valueMap).get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    public <T> void update(final UpdateAttribute<T> updateAttribute) {
        final Class<T> type = UpdateAttribute.Helper.getDeclaringClass(updateAttribute);
        try {
            final T entity = attributeValueHelper.createEntity(type, UpdateAttribute.Helper.getKeys(updateAttribute));

            final Map<String, Object> valueMap = attributeValueHelper.toFieldValueMap(UpdateAttribute.Helper.getAttributeValues(updateAttribute));
            valueMap.putAll(UpdateAttribute.Helper.getValuePaths(updateAttribute));

            final DocumentReference documentReference = getDocumentReference(entity);
            documentReference.update(valueMap).get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void updateFields(final String path, final String field, final Object value) {
        try {
            firestore.document(path).update(field, value).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @SafeVarargs
    public final <T> void delete(final T... entities) {

        final com.google.cloud.firestore.WriteBatch batch = firestore.batch();

        for (T entity : entities) {
            final DocumentReference documentReference = getDocumentReference(entity);
            batch.delete(documentReference);
        }

        try {
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> List<T> find(final Query<T> query) {
        try {

            final ApiFuture<QuerySnapshot> querySnapshot = QueryAdapter.toFirebaseQuery(firestore, query).get();
            final List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            return documents
                    .stream()
                    .map(document -> {
                        final T object = document.toObject(query.getResultType());
                        entityHelper.setId(object, document.getId());
                        return object;
                    })
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> T findById(final Query<T> query) {
        try {
            final QueryBuilder<T> queryBuilder = (QueryBuilder<T>) query;
            final DocumentReference document = firestore.document(queryBuilder.getPath());
            final DocumentSnapshot documentSnapshot = document.get().get();
            final T object = documentSnapshot.toObject(queryBuilder.getResultType());
            if (object != null) {
                entityHelper.setId(object, documentSnapshot.getId());
            }
            return object;
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> T runTransaction(final Function<Transaction, T> transactionConsumer) {
        try {
            return firestore
                    .runTransaction(transaction ->
                            transactionConsumer.apply(new TransactionImpl(firestore, transaction))).get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void writeInBatch(final Consumer<WriteBatch> batchConsumer) {
        try {
            final com.google.cloud.firestore.WriteBatch batch = firestore.batch();
            final WriteBatchImpl writeBatch = new WriteBatchImpl(firestore, batch);
            batchConsumer.accept(writeBatch);
            batch.commit().get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    private <T> DocumentReference getDocumentReference(final T entity) {
        final String documentPath = entityHelper.getDocumentPath(entity);
        return firestore.document(documentPath);
    }

    private <T> CollectionReference getCollectionReference(final T entity) {
        final String documentPath = entityHelper.getCollectionPath(entity);
        return firestore.collection(documentPath);
    }
}
