package in.kuros.jfirebase.util;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Blob;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.GeoPoint;
import com.google.firestore.v1.Value;
import in.kuros.jfirebase.entity.EntityDeclarationException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassMapper {

    private static final ConcurrentMap<Class<?>, BeanMapper<?>> mappers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> BeanMapper<T> getBeanMapper(final Class<T> clazz) {
        mappers.computeIfAbsent(clazz, aClass -> new BeanMapper<>(clazz));
        return (BeanMapper<T>) mappers.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> Object serialize(T o) {
        if (o == null) {
            return null;
        } else if (o instanceof Number) {
            if (o instanceof Long || o instanceof Integer || o instanceof Double || o instanceof Float) {
                return o;
            } else {
                throw new EntityDeclarationException(
                        String.format(
                                "Numbers of type %s are not supported, please use an int, long, float or double",
                                o.getClass().getSimpleName()));
            }
        } else if (o instanceof String) {
            return o;
        } else if (o instanceof Boolean) {
            return o;
        } else if (o instanceof Character) {
            throw new EntityDeclarationException("Characters are not supported, please use Strings");
        } else if (o instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    String keyString = (String) key;
                    result.put(keyString, serialize(entry.getValue()));
                } else {
                    throw new EntityDeclarationException("Maps with non-string keys are not supported");
                }
            }
            return result;
        } else if (o instanceof Collection) {
            if (o instanceof List) {
                List<Object> list = (List<Object>) o;
                List<Object> result = new ArrayList<>(list.size());
                for (Object value : list) {
                    result.add(serialize(value));
                }
                return result;
            } else {
                throw new EntityDeclarationException("Serializing Collections is not supported, please use Lists instead");
            }
        } else if (o.getClass().isArray()) {
            throw new EntityDeclarationException("Serializing Arrays is not supported, please use Lists instead");
        } else if (o instanceof Enum) {
            String enumName = ((Enum<?>) o).name();
            try {
                Field enumField = o.getClass().getField(enumName);
                return BeanMapper.propertyName(enumField);
            } catch (NoSuchFieldException ex) {
                return enumName;
            }
        } else if (o instanceof LocalDate) {
            return DateHelper.toDate((LocalDate) o);
        } else if (o instanceof LocalDateTime) {
            return DateHelper.toDate((LocalDateTime) o);
        } else if (o instanceof Date
                || o instanceof Timestamp
                || o instanceof GeoPoint
                || o instanceof Blob
                || o instanceof DocumentReference
                || o instanceof FieldValue
                || o instanceof Value) {
            return o;
        } else {
            Class<T> clazz = (Class<T>) o.getClass();
            BeanMapper<T> mapper = getBeanMapper(clazz);
            return mapper.serialize(o);
        }
    }
}
