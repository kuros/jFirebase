package in.kuros.jfirebase.transaction;

import in.kuros.jfirebase.query.Query;

import java.util.List;

public interface Transaction extends WriteBatch {

    <T> List<T> get(Query<T> queryBuilder);

}
