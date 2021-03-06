package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.Firestore;
import in.kuros.jfirebase.provider.firebase.query.QueryBuilder;
import in.kuros.jfirebase.query.Query;

public class QueryAdapter {

    public static <T> com.google.cloud.firestore.Query toFirebaseQuery(final Firestore firestore, final Query<T> query) {
        final QueryBuilder<T> queryBuilder = (QueryBuilder<T>) query;
        final String path = queryBuilder.getPath();
        if (queryBuilder.isCollectionGroup()) {
            return queryBuilder.build(firestore.collectionGroup(path));
        }
        return queryBuilder.build(firestore.collection(path));
    }
}
