package in.kuros.jfirebase.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import in.kuros.jfirebase.metadata.MetadataException;
import in.kuros.jfirebase.util.CustomCollectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EntityParentCache {
    private Cache<Class<?>, List<Class<?>>> parentCache;
    private Cache<Class<?>, List<MappedClassField>> mappedClassFieldCache;

    private EntityParentCache() {
        this.parentCache = CacheBuilder.newBuilder().build();
        this.mappedClassFieldCache = CacheBuilder.newBuilder().build();
    }

    public static final EntityParentCache INSTANCE = new EntityParentCache();

    public List<Class<?>> getParents(final Class<?> type) {
        try {
            return parentCache.get(type, () -> findParentsInOrder(type));
        } catch (final ExecutionException e) {
            throw new EntityDeclarationException(e);
        }
    }

    public List<MappedClassField> getMappedClassFields(final Class<?> type) {
        try {
            return mappedClassFieldCache.get(type, () -> findMappedClassFieldsInOrder(type));
        } catch (final ExecutionException e) {
            throw new EntityDeclarationException(e);
        }
    }

    private List<MappedClassField> findMappedClassFieldsInOrder(final Class<?> type) {

        final List<Field> allParentFields = getAllParentFields(type);
        final Map<? extends Class<?>, Field> mappedClassToFieldMap = allParentFields.
                stream()
                .collect(CustomCollectors.toMap(this::getMappedClass, Function.identity()));

        final List<MappedClassField> result = new ArrayList<>();
        for (Class<?> parentClass : getParents(type)) {
            final Field field = mappedClassToFieldMap.get(parentClass);
            if (field == null) {
                throw new EntityDeclarationException("Use @Parent or @IdReference, no parent reference mapping found: " + parentClass);
            }

            result.add(new MappedClassField(parentClass, field));
        }

        return result;
    }

    private List<Class<?>> findParentsInOrder(final Class<?> type) {
        final List<Class<?>> result = new ArrayList<>();

        final Stack<Class<?>> stack = new Stack<>();

        Class<?> ref = type;
        while (true) {
            final List<IdReference> parents = Arrays.stream(ref.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Parent.class))
                    .map(field -> {
                        final IdReference idReference = field.getAnnotation(IdReference.class);
                        if (idReference == null) {
                            throw new EntityDeclarationException("@IdReference missing for : " + field);
                        }
                        return idReference;
                    })
                    .collect(Collectors.toList());

            if (parents.isEmpty()) {
                break;
            }

            if (parents.size() > 1) {
                throw new MetadataException("Multiple parent defined, only 1 supported: " + ref.getName());
            }

            ref = parents.get(0).value();
            stack.push(ref);
        }

        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    @AllArgsConstructor
    @Getter
    public static class MappedClassField {
        private Class<?> mappedClass;
        private Field field;
    }

    private Class<?> getMappedClass(final Field field) {
        return field.getAnnotation(IdReference.class).value();
    }

    private List<Field> getAllParentFields(final Class<?> aClass) {
        final Field[] declaredFields = aClass.getDeclaredFields();
        final List<Field> fields = Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(IdReference.class))
                .collect(Collectors.toList());
        final Optional<Field> parentMapping = fields.stream().filter(field -> field.isAnnotationPresent(Parent.class)).findFirst();
        if (!fields.isEmpty() && !parentMapping.isPresent()) {
            throw new EntityDeclarationException("One @Parent mapping required with @IdReference for class: " + aClass.getName());
        }
        return fields;
    }
}
