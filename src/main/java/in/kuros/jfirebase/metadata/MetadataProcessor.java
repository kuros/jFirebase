package in.kuros.jfirebase.metadata;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import in.kuros.jfirebase.entity.EntityDeclarationException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MetadataProcessor {

    private MetadataProcessor() {
    }

    public static void init(final String... basePackages) {
        final MetadataProcessor metadataProcessor = new MetadataProcessor();
        for (String basePackage : basePackages) {
            metadataProcessor.initializeMetaModel(basePackage);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initializeMetaModel(final String basePackage) {
        try {
            final ClassPath classPath = ClassPath.from(MetadataProcessor.class.getClassLoader());
            final ImmutableSet<ClassPath.ClassInfo> classInfo = classPath.getTopLevelClassesRecursive(basePackage);

            for (ClassPath.ClassInfo info : classInfo) {
                final Class<?> type = Class.forName(info.getName());
                final Metadata metadata = type.getAnnotation(Metadata.class);
                if (metadata == null) {
                    continue;
                }

                final Class<?> referencedClass = metadata.value();
                final Map<String, Field> allFields = Arrays.stream(referencedClass.getDeclaredFields())
                        .collect(Collectors.toMap(Field::getName, Function.identity()));

                final Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    if (!allFields.containsKey(field.getName())) {
                        throw new MetadataException("field not mapped correctly: " + field.getName() + ", class: " + info.getName());
                    }

                    if (!Attribute.class.isAssignableFrom(field.getType())) {
                        throw new MetadataException("field should be of type Attribute: " + field.getName() + ", class: " + info.getName());
                    }

                    if (MapAttribute.class.isAssignableFrom(field.getType())) {
                        field.set(null, new MapAttributeImpl<>(allFields.get(field.getName())));
                    } else {
                        field.set(null, new AttributeImpl<>(allFields.get(field.getName())));
                    }
                }
            }
        } catch (final Exception e) {
            throw new EntityDeclarationException(e);
        }
    }
}
