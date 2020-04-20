package in.kuros.jfirebase.transaction;

import in.kuros.jfirebase.query.Query;

import java.util.List;
import java.util.Optional;

public interface Transaction extends WriteBatch {

    <T> List<T> get(Query<T> queryBuilder);

    <T> Optional<T> findById(Query<T> queryBuilder);

}
