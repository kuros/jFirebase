package in.kuros.jfirebase.util;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ClassMapperHelper {

    private ClassMapperHelper() {
    }

    @Nonnull
    public static <T> T toObject(@Nonnull Class<T> valueType, QueryDocumentSnapshot snapshot) {
        Map<String, Object> data = snapshot.getData();
        T result = NamingStrategyClassMapper.convertToCustomClass(data, valueType, snapshot.getReference());
        Preconditions.checkNotNull(result, "Object in a QueryDocumentSnapshot should be non-null");
        return result;
    }

    @Nullable
    public static <T> T toObject(Class<T> resultType, DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();
        return data == null ? null : NamingStrategyClassMapper.convertToCustomClass(data, resultType, documentSnapshot.getReference());
    }
}
