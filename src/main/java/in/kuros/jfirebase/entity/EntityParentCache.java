package in.kuros.jfirebase.entity;

import com.google.common.collect.Lists;
import in.kuros.jfirebase.util.BeanMapper;
import in.kuros.jfirebase.util.ClassMapper;
import in.kuros.jfirebase.util.CustomCollectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EntityParentCache {
    private static final ConcurrentMap<Class<?>, List<Class<?>>> PARENT_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, List<MappedClassField>> MAPPED_FIELD = new ConcurrentHashMap<>();

    public static final EntityParentCache INSTANCE = new EntityParentCache();

    public List<Class<?>> getParents(final Class<?> type) {
        PARENT_CACHE.computeIfAbsent(type, this::findParentsInOrder);
        return PARENT_CACHE.get(type);
    }

    public List<MappedClassField> getMappedClassFields(final Class<?> type) {
        MAPPED_FIELD.computeIfAbsent(type, aClass -> findMappedClassFieldsInOrder(type));
        return MAPPED_FIELD.get(type);
    }

    private List<MappedClassField> findMappedClassFieldsInOrder(final Class<?> type) {


        final List<MappedClassField> allParentFields = getAllParentFields(type);
        final Map<? extends Class<?>, MappedClassField> mappedClassToFieldMap = allParentFields.
                stream()
                .collect(CustomCollectors.toMap(MappedClassField::getMappedClass, Function.identity()));

        final List<MappedClassField> result = new ArrayList<>();
        for (Class<?> parentClass : getParents(type)) {
            result.add(mappedClassToFieldMap.get(parentClass));
        }

        return result;
    }

    private List<Class<?>> findParentsInOrder(final Class<?> type) {
        final List<Class<?>> result = new ArrayList<>();

        final Stack<Class<?>> stack = new Stack<>();

        Class<?> ref = type;
        while (true) {
            final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(ref);
            final Collection<Parent> values = beanMapper.getParent().values();
            if (values.isEmpty()) {
                break;
            }

            final Parent parent = Lists.newArrayList(values).get(0);

            ref = parent.value();
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
        private String field;
        private Class<?> mappedClass;
        private String collection;
    }

    private Class<?> getMappedClass(final Field field) {
        return field.getAnnotation(IdReference.class).value();
    }

    private List<MappedClassField> getAllParentFields(final Class<?> aClass) {
        final BeanMapper<?> beanMapper = ClassMapper.getBeanMapper(aClass);

        final List<MappedClassField> classFields = beanMapper.getIdReferences()
                .entrySet()
                .stream()
                .map(entry -> new MappedClassField(entry.getKey(), entry.getValue().value(), entry.getValue().collection()))
                .collect(Collectors.toList());

        beanMapper.getParent()
                .forEach((key, value) -> classFields.add(new MappedClassField(key, value.value(), value.collection())));

        return classFields;
    }
}
