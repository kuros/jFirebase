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
import in.kuros.jfirebase.metadata.SetAttribute;
import in.kuros.jfirebase.metadata.SetAttribute.Helper;
import in.kuros.jfirebase.metadata.UpdateAttribute;
import in.kuros.jfirebase.metadata.ValuePath;
import in.kuros.jfirebase.provider.firebase.query.QueryBuilder;
import in.kuros.jfirebase.query.Query;
import in.kuros.jfirebase.transaction.Transaction;
import in.kuros.jfirebase.transaction.WriteBatch;
import in.kuros.jfirebase.util.BeanMapper;
import in.kuros.jfirebase.util.ClassMapper;

import in.kuros.jfirebase.util.PropertyNamingStrategy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    public PersistenceServiceImpl(Firestore firestore, PropertyNamingStrategy namingStrategy) {
        this(firestore);
        this.entityHelper.setPropertyNamingStrategy(namingStrategy);
    }

    @Override
    public <T> T create(final T entity) {

        try {
            final CollectionReference collectionReference = getCollectionReference(entity);
            final String id = entityHelper.getId(entity);
            final DocumentReference documentReference = id == null ? collectionReference.document() : collectionReference.document(id);
            entityHelper.setCreateTime(entity);

            final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(getClass(entity));
            documentReference.create(beanMapper.serialize(entity)).get();
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
            final DocumentReference documentReference = getDocumentReference(entity);
            final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(getClass(entity));
            entityHelper.setUpdateTime(entity, new Date());
            setCreateTimeOnUpdate(entity);
            batch.set(documentReference, beanMapper.serialize(entity), SetOptions.merge());
        }

        try {
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    private <T> void setCreateTimeOnUpdate(T entity) {
        Optional<Date> createTimeOptional = entityHelper.getCreateTime(entity);
        Optional<String> createTimeField = entityHelper.getCreateTimeFieldName(getClass(entity));
        if (createTimeField.isPresent() && !createTimeOptional.isPresent()) {
            entityHelper.setCreateTime(entity);
        }
    }

    @Override
    public <T> void set(final T entity, final Attribute<T, ?> attribute) {
        final DocumentReference documentReference = getDocumentReference(entity);
        final List<String> fields = Lists.newArrayList(attribute.getName());

        final BeanMapper<T> beanMapper = ClassMapper.getBeanMapper(getClass(entity));
        entityHelper.setUpdateTime(entity, new Date());
        setCreateTimeOnUpdate(entity);
        try {
            documentReference.set(beanMapper.serialize(entity), SetOptions.mergeFields(fields)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void set(final SetAttribute<T> setAttribute) {
        try {
            final Class<T> declaringClass = Helper.getDeclaringClass(setAttribute);
            final List<AttributeValue<T, ?>> keyAttributes = SetAttribute.Helper.getKeys(setAttribute);
            final List<AttributeValue<T, ?>> updateValues = SetAttribute.Helper.getAttributeValues(setAttribute);
            final List<ValuePath<?>> valuePaths = SetAttribute.Helper.getValuePaths(setAttribute);

            final Map<String, Object> updateMap = attributeValueHelper.convertToObjectMap(updateValues);
            final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(updateValues);

            attributeValueHelper.addValuePaths(updateMap, valuePaths);
            final List<FieldPath> valueFieldPaths = attributeValueHelper.convertValuePathToFieldPaths(valuePaths);
            fieldPaths.addAll(valueFieldPaths);

            final Optional<String> updateTimeField = entityHelper.getUpdateTimeFieldName(declaringClass);
            updateTimeField.ifPresent(name -> {
                updateMap.put(name, FieldValue.serverTimestamp());
                fieldPaths.add(FieldPath.of(name));
            });

            final String documentPath = entityHelper.getDocumentPath(keyAttributes);
            final DocumentReference documentReference = firestore.document(documentPath);
            documentReference.set(updateMap, SetOptions.mergeFieldPaths(fieldPaths)).get();

        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void remove(final RemoveAttribute<T> removeAttribute) {
        try {
            final List<AttributeValue<T, ?>> attributeValues = RemoveAttribute.Helper.getAttributeValues(removeAttribute, FieldValue::delete);
            final List<ValuePath<?>> valuePaths = RemoveAttribute.Helper.getValuePaths(removeAttribute);
            final Map<String, Object> valueMap = attributeValueHelper.convertToObjectMap(attributeValues);
            final List<FieldPath> fieldPaths = attributeValueHelper.getFieldPaths(attributeValues);
            attributeValueHelper.addValuePaths(valueMap, valuePaths);
            final List<FieldPath> valueFieldPaths = attributeValueHelper.convertValuePathToFieldPaths(valuePaths);
            fieldPaths.addAll(valueFieldPaths);
            final String documentPath = entityHelper.getDocumentPath(RemoveAttribute.Helper.getKeys(removeAttribute));
            final DocumentReference documentReference = firestore.document(documentPath);
            documentReference.set(valueMap, SetOptions.mergeFieldPaths(fieldPaths)).get();
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    public <T> void update(final UpdateAttribute<T> updateAttribute) {
        try {
            final Map<String, Object> valueMap = attributeValueHelper.convertToObjectMap(UpdateAttribute.Helper.getAttributeValues(updateAttribute));
            attributeValueHelper.addValuePaths(valueMap, UpdateAttribute.Helper.getValuePaths(updateAttribute));
            final Optional<String> updateTimeField = entityHelper.getUpdateTimeFieldName(UpdateAttribute.Helper.getDeclaringClass(updateAttribute));
            updateTimeField.ifPresent(name -> valueMap.put(name, FieldValue.serverTimestamp()));

            final String documentPath = entityHelper.getDocumentPath(UpdateAttribute.Helper.getKeys(updateAttribute));
            final DocumentReference documentReference = firestore.document(documentPath);
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
                        entityHelper.setUpdateTime(object, Objects.requireNonNull(
                            document.getUpdateTime()).toDate());
                        entityHelper.setCreateTime(object, Objects.requireNonNull(
                            document.getCreateTime()).toDate());
                        return object;
                    })
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> Optional<T> findById(final Query<T> query) {
        try {
            final QueryBuilder<T> queryBuilder = (QueryBuilder<T>) query;
            final DocumentReference document = firestore.document(queryBuilder.getPath());
            final DocumentSnapshot documentSnapshot = document.get().get();
            final T object = documentSnapshot.toObject(queryBuilder.getResultType());
            return Optional.ofNullable(object)
                    .map(e -> {
                        entityHelper.setId(e, documentSnapshot.getId());
                        entityHelper.setUpdateTime(e, Objects.requireNonNull(
                            documentSnapshot.getUpdateTime()).toDate());
                        entityHelper.setCreateTime(e, Objects.requireNonNull(
                            documentSnapshot.getCreateTime()).toDate());
                        return e;
                    });
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

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClass(final T entity) {
        return (Class<T>) entity.getClass();
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
