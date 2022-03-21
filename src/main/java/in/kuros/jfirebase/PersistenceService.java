package in.kuros.jfirebase;


import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.metadata.SetAttribute;
import in.kuros.jfirebase.metadata.UpdateAttribute;
import in.kuros.jfirebase.query.Query;
import in.kuros.jfirebase.transaction.Transaction;
import in.kuros.jfirebase.transaction.WriteBatch;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public interface PersistenceService {

    <T> T create(T entity);

    <T> List<WriteResult> set(T... entities);

    <T> WriteResult set(T entity, Attribute<T, ?> attribute);

    <T> WriteResult set(SetAttribute<T> setAttribute);

    <T> WriteResult remove(RemoveAttribute<T> removeAttribute);

    <T> WriteResult update(final UpdateAttribute<T> updateAttribute);

    WriteResult updateFields(String path, String field, Object value);

    <T> List<WriteResult> delete(T... entities);

    <T> List<T> find(Query<T> queryBuilder);

    <T> Optional<T> findById(Query<T> queryBuilder);

    <T> Optional<DocumentSnapshot> findSnapshotById(Query<T> queryBuilder);

    <T> T runTransaction(Function<Transaction, T> transactionConsumer);

    List<WriteResult> writeInBatch(Consumer<WriteBatch> batchConsumer);

    static void init() {
    }
}
