package in.kuros.jfirebase.entity;

import com.google.common.collect.Lists;
import in.kuros.jfirebase.exception.PersistenceException;
import in.kuros.jfirebase.util.BeanMapper;
import in.kuros.jfirebase.util.ClassMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EntityParentCache {
    private static final ConcurrentMap<Class<?>, List<FieldCollectionMapping>> MAPPED_FIELD = new ConcurrentHashMap<>();

    public static final EntityParentCache INSTANCE = new EntityParentCache();

    public List<FieldCollectionMapping> getFieldCollectionMappings(final Class<?> type) {
        MAPPED_FIELD.computeIfAbsent(type, aClass -> findParentFieldsInOrder(type));
        return MAPPED_FIELD.get(type);
    }

    private List<FieldCollectionMapping> findParentFieldsInOrder(final Class<?> type) {

        final List<MappedClassField> allParentFields = getAllParentFields(type);
        final Map<String, MappedClassField> byCollection = allParentFields.stream().filter(field -> notEmpty(field.getCollection())).collect(Collectors.toMap(MappedClassField::getCollection, Function.identity()));
        final Map<? extends Class<?>, MappedClassField> byMappedClass = allParentFields.stream().filter(Objects::nonNull).filter(field -> field.getMappedClass() != IdReference.DEFAULT.class).collect(Collectors.toMap(MappedClassField::getMappedClass, Function.identity()));

        final List<MappedClassField> result = new ArrayList<>();

        final Stack<MappedClassField> stack = new Stack<>();

        ParentClassOrName ref = new ParentClassOrName(type, null, null);
        int count = 100;
        while (true) {

            if (--count < 0) {
                throw new PersistenceException("Cyclic loop found or subCollection depth exceeded");
            }

            if (ref == null) {
                break;
            }

            if (notEmpty(ref.getName())) {
                final MappedClassField mappedClassField = byCollection.get(ref.getName());
                stack.add(mappedClassField);
                final CollectionParent collectionParent = mappedClassField.getCollectionParent();
                if (notEmpty(collectionParent)) {
                    ref = new ParentClassOrName(null, null, collectionParent);
                } else {
                    ref = null;
                }
                continue;
            }

            MappedClassField mappedClassField = null;
            Parent parent = null;
            if (ref.getCollectionParent() == null) {
                final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(ref.getClazz());
                final Collection<Parent> parentProperty = beanMapper.getParent().values();

                if (parentProperty.isEmpty()) {
                    break;
                }
                parent = Lists.newArrayList(parentProperty).get(0);

                if (parent.value() != IdReference.DEFAULT.class) {
                    mappedClassField = byMappedClass.get(parent.value());
                } else if (notEmpty(parent.collection())) {
                    mappedClassField = byCollection.get(parent.collection());
                }
            } else {
                final CollectionParent collectionParent = ref.getCollectionParent();
                if (collectionParent.value() != IdReference.DEFAULT.class) {
                    mappedClassField = byMappedClass.get(collectionParent.value());
                } else {
                    mappedClassField = byCollection.get(collectionParent.collection());
                }
            }


            if (mappedClassField != null) {
                stack.push(mappedClassField);
                final CollectionParent collectionParent = mappedClassField.getCollectionParent();
                if (notEmpty(collectionParent)) {
                    ref = new ParentClassOrName(null, null, collectionParent);
                    continue;
                }
            }

            if (parent == null || parent.value() == IdReference.DEFAULT.class) {
                ref = null;
            } else {
                ref = new ParentClassOrName(parent.value(), null, null);
            }
        }

        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result
                .stream()
                .map(in -> new FieldCollectionMapping(in.getField(), in.getMappedClass(), in.getCollection()))
                .collect(Collectors.toList());
    }

    private boolean notEmpty(final String value) {
        return value != null && !value.equals("");
    }

    private boolean notEmpty(final CollectionParent collectionParent) {
        return collectionParent != null && (collectionParent.value() != IdReference.DEFAULT.class || notEmpty(collectionParent.collection()));
    }

    private List<MappedClassField> getAllParentFields(final Class<?> aClass) {
        final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(aClass);

        final List<MappedClassField> classFields = beanMapper.getIdReferences()
                .entrySet()
                .stream()
                .map(entry -> new MappedClassField(entry.getKey(), entry.getValue().value(), entry.getValue().collection(), entry.getValue().collectionParent()))
                .collect(Collectors.toList());

        beanMapper.getParent()
                .forEach((key, value) -> classFields.add(new MappedClassField(key, value.value(), value.collection(), value.collectionParent())));

        return classFields;
    }

    @AllArgsConstructor
    @Getter
    public static class FieldCollectionMapping {
        private final String field;
        private final Class<?> mappedClass;
        private final String collection;
    }


    @AllArgsConstructor
    @Getter
    public static class MappedClassField {
        private final String field;
        private final Class<?> mappedClass;
        private final String collection;
        private final CollectionParent collectionParent;
    }

    @AllArgsConstructor
    @Getter
    private static class ParentClassOrName {
        private final Class<?> clazz;
        private final String name;
        private final CollectionParent collectionParent;
    }
}
