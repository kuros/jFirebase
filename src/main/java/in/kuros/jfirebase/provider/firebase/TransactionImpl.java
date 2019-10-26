package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.transaction.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionImpl extends WriteBatchImpl implements Transaction {

    private final Firestore firestore;
    private final com.google.cloud.firestore.Transaction transaction;
    private final EntityHelper entityHelper;

    TransactionImpl(final Firestore firestore, final com.google.cloud.firestore.Transaction transaction) {
        super(firestore, transaction);
        this.firestore = firestore;
        this.transaction = transaction;
        this.entityHelper = EntityHelperImpl.INSTANCE;
    }

    @Override
    public <T> List<T> get(final in.kuros.jfirebase.query.Query<T> queryBuilder) {
        try {
            final Query query = QueryAdapter.toFirebaseQuery(firestore, queryBuilder);
            return transaction
                    .get(query)
                    .get()
                    .getDocuments()
                    .stream()
                    .map(doc -> {
                        final T object = doc.toObject(queryBuilder.getResultType());
                        entityHelper.setId(object, doc.getId());
                        return object;
                    })
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            throw new PersistenceException(e);
        }
    }
}
