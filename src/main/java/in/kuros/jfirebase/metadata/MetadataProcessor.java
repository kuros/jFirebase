package in.kuros.jfirebase.metadata;

import in.kuros.jfirebase.entity.EntityDeclarationException;
import in.kuros.jfirebase.reflection.MetadataScanner;
import in.kuros.jfirebase.util.CustomCollectors;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class MetadataProcessor {

    private MetadataProcessor() {
    }

    public static void init(final String... basePackages) {
        final MetadataProcessor metadataProcessor = new MetadataProcessor();
        for (String basePackage : basePackages) {
            metadataProcessor.initializeMetaModel(basePackage);
        }
    }

    private void initializeMetaModel(final String basePackage) {
        try {
            final Reflections reflections = new Reflections(basePackage, new MetadataScanner());

            final Set<String> classNames = reflections.getStore().get(MetadataScanner.class.getSimpleName()).keySet();

            for (String className : classNames) {
                final Class<?> type = loadClass(className);

                final Metadata metadata = type.getAnnotation(Metadata.class);
                if (metadata == null) {
                    continue;
                }

                final Class<?> referencedClass = metadata.value();
                final Map<String, Field> allFields = Arrays.stream(referencedClass.getDeclaredFields())
                        .collect(CustomCollectors.toMap(Field::getName, Function.identity()));

                final Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    if (!allFields.containsKey(field.getName())) {
                        throw new MetadataException("field not mapped correctly: " + field.getName() + ", class: " + className);
                    }

                    if (!Attribute.class.isAssignableFrom(field.getType())) {
                        throw new MetadataException("field should be of type Attribute: " + field.getName() + ", class: " + className);
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

    private Class<?> loadClass(String clf) throws ClassNotFoundException {
        try {
            return Class.forName(clf);
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(clf);
        }
    }
}
