package in.kuros.jfirebase.util;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.annotation.Exclude;
import in.kuros.jfirebase.entity.CreateTime;
import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.EntityDeclarationException;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.Parent;
import in.kuros.jfirebase.entity.UpdateTime;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Getter
public class BeanMapper<T> {

    private final Class<T> clazz;
    private final Entity entity;
    private final String id;
    private final Map<String, Parent> parent;
    private final Constructor<T> constructor;
    // Case insensitive mapping of properties to their case sensitive versions
    private final Map<String, String> properties;
    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Field> fields;
    private final String createTime;
    private final String updateTime;
    private final Map<String, IdReference> idReferences;

    @SneakyThrows
    public BeanMapper(final Class<T> clazz) {
        this.clazz = clazz;
        this.entity = clazz.getAnnotation(Entity.class);
        this.properties = new HashMap<>();
        this.getters = new HashMap<>();
        this.setters = new HashMap<>();
        this.fields = new HashMap<>();
        this.idReferences = new HashMap<>();
        this.parent = new HashMap<>();

        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // We will only fail at deserialization time if no constructor is present
            constructor = null;
        }
        this.constructor = constructor;

        String idProperty = null;
        String createdProperty = null;
        String updatedProperty = null;
        for (Method method : clazz.getMethods()) {
            if (shouldIncludeGetter(method)) {
                String propertyName = propertyName(method);
                addProperty(propertyName);
                method.setAccessible(true);
                if (getters.containsKey(propertyName)) {
                    throw new RuntimeException(
                            "Found conflicting getters for name "
                                    + method.getName()
                                    + " on class "
                                    + clazz.getName());
                }
                getters.put(propertyName, method);

                final Field declaredField = clazz.getDeclaredField(propertyName);
                declaredField.setAccessible(true);
                if (idProperty != null && declaredField.isAnnotationPresent(Id.class)) {
                    throw new EntityDeclarationException("Multiple @Id mapping found or class: " + clazz.getName());
                } else if (declaredField.isAnnotationPresent(Id.class)) {
                    idProperty = propertyName;
                }

                if (!parent.isEmpty() && declaredField.isAnnotationPresent(Parent.class)) {
                    throw new EntityDeclarationException("Multiple @Parent mapping found or class: " + clazz.getName());
                } else if (declaredField.isAnnotationPresent(Parent.class)) {
                    parent.put(propertyName, declaredField.getAnnotation(Parent.class));
                }

                if (createdProperty != null && declaredField.isAnnotationPresent(CreateTime.class)) {
                    throw new EntityDeclarationException("Multiple @CreateTime mapping found or class: " + clazz.getName());
                } else if (declaredField.isAnnotationPresent(CreateTime.class)) {
                    fields.put(propertyName, declaredField);
                    createdProperty = propertyName;
                }

                if (updatedProperty != null && declaredField.isAnnotationPresent(UpdateTime.class)) {
                    throw new EntityDeclarationException("Multiple @UpdateTime mapping found or class: " + clazz.getName());
                } else if (declaredField.isAnnotationPresent(UpdateTime.class)) {
                    fields.put(propertyName, declaredField);
                    updatedProperty = propertyName;
                }

                if (declaredField.isAnnotationPresent(IdReference.class)) {
                    idReferences.put(propertyName, declaredField.getAnnotation(IdReference.class));
                }

            }
        }

        this.id = idProperty;
        this.createTime = createdProperty;
        this.updateTime = updatedProperty;

        Class<? super T> currentClass = clazz;
        do {
            // Add any setters
            for (Method method : currentClass.getDeclaredMethods()) {
                if (shouldIncludeSetter(method)) {
                    String propertyName = propertyName(method);
                    String existingPropertyName = properties.get(propertyName.toLowerCase(Locale.US));
                    if (existingPropertyName != null) {
                        if (!existingPropertyName.equals(propertyName)) {
                            throw new RuntimeException(
                                    "Found setter on "
                                            + currentClass.getName()
                                            + " with invalid case-sensitive name: "
                                            + method.getName());
                        } else {
                            Method existingSetter = setters.get(propertyName);
                            if (existingSetter == null) {
                                method.setAccessible(true);
                                setters.put(propertyName, method);
                            }
                        }
                    }
                }
            }

            for (Field field : currentClass.getDeclaredFields()) {
                String propertyName = propertyName(field);

                // Case sensitivity is checked at deserialization time
                // Fields are only added if they don't exist on a subclass
                if (properties.containsKey(propertyName.toLowerCase(Locale.US))
                        && !fields.containsKey(propertyName)) {
                    field.setAccessible(true);
                    fields.put(propertyName, field);
                }
            }

            // Traverse class hierarchy until we reach java.lang.Object which contains a bunch
            // of fields/getters we don't want to serialize
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null && !currentClass.equals(Object.class));
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<String> getCreateTime() {
        return Optional.ofNullable(createTime);
    }

    public Optional<String> getUpdateTime() {
        return Optional.ofNullable(updateTime);
    }

    private void addProperty(String property) {
        String oldValue = properties.put(property.toLowerCase(Locale.US), property);
        if (oldValue != null && !property.equals(oldValue)) {
            throw new RuntimeException(
                    "Found two getters or fields with conflicting case "
                            + "sensitivity for property: "
                            + property.toLowerCase(Locale.US));
        }
    }

    Map<String, Object> serialize(T object) {
        if (!clazz.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException(
                    "Can't serialize object of class "
                            + object.getClass()
                            + " with BeanMapper for class "
                            + clazz);
        }
        Map<String, Object> result = new HashMap<>();
        for (String property : properties.values()) {

            Object propertyValue;
            if (getters.containsKey(property)) {
                Method getter = getters.get(property);
                try {
                    propertyValue = getter.invoke(object);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Must be a field
                Field field = fields.get(property);
                if (field == null) {
                    throw new IllegalStateException("Bean property without field or getter: " + property);
                }
                try {
                    propertyValue = field.get(object);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            Object serializedValue;
            if (updateTime != null && updateTime.equals(property)) {
                serializedValue = FieldValue.serverTimestamp();
            } else {
                serializedValue = ClassMapper.serialize(propertyValue);
            }

            result.put(property, serializedValue);
        }
        return result;
    }

    public void setValue(final T object, final String propertyName, Object value) {

        if (setters.containsKey(propertyName) && !propertyName.equals(createTime) && !propertyName.equals(updateTime)) {
            Method setter = setters.get(propertyName);
            Type[] params = setter.getGenericParameterTypes();
            if (params.length != 1) {
                throw new   EntityDeclarationException("Setter does not have exactly one parameter");
            }
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else if (fields.containsKey(propertyName)) {
            Field field = fields.get(propertyName);

            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String message =
                    "No setter/field for " + propertyName + " found on class " + clazz.getName();
            if (properties.containsKey(propertyName.toLowerCase(Locale.US))) {
                message += " (fields/setters are case sensitive!)";
            }

            throw new EntityDeclarationException(message);
        }
    }

    public Object getValue(final T object, final String propertyName) {
        if (getters.containsKey(propertyName)) {
            Method getter = getters.get(propertyName);
            Type[] params = getter.getGenericParameterTypes();
            if (params.length != 0) {
                throw new   EntityDeclarationException("found parameters for getter");
            }
            try {
                return getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else if (fields.containsKey(propertyName)) {
            Field field = fields.get(propertyName);

            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String message =
                    "No setter/field for " + propertyName + " found on class " + clazz.getName();
            if (properties.containsKey(propertyName.toLowerCase(Locale.US))) {
                message += " (fields/setters are case sensitive!)";
            }

            throw new EntityDeclarationException(message);
        }
    }

    private static boolean shouldIncludeSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        // Exclude methods from Object.class
        if (method.getDeclaringClass().equals(Object.class)) {
            return false;
        }
        // Static methods
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        // Has a return type
        if (!method.getReturnType().equals(Void.TYPE)) {
            return false;
        }
        // Methods without exactly one parameters
        if (method.getParameterTypes().length != 1) {
            return false;
        }
        // Excluded methods
        if (method.isAnnotationPresent(Exclude.class)) {
            return false;
        }
        return true;
    }

    private static boolean shouldIncludeField(Field field) {
        // Exclude methods from Object.class
        if (field.getDeclaringClass().equals(Object.class)) {
            return false;
        }
        // Non-public fields
        if (!Modifier.isPublic(field.getModifiers())) {
            return false;
        }
        // Static fields
        if (Modifier.isStatic(field.getModifiers())) {
            return false;
        }
        // Transient fields
        if (Modifier.isTransient(field.getModifiers())) {
            return false;
        }

        return true;
    }

    private static boolean shouldIncludeGetter(final Method method) {
        if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
            return false;
        }
        // Exclude methods from Object.class
        if (method.getDeclaringClass().equals(Object.class)) {
            return false;
        }
        // Non-public methods
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        // Static methods
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        // No return type
        if (method.getReturnType().equals(Void.TYPE)) {
            return false;
        }
        // Non-zero parameters
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        // Excluded methods
        if (method.isAnnotationPresent(Exclude.class)) {
            return false;
        }
        return true;
    }

    public static String propertyName(Field field) {
        return field.getName();
    }

    private static String propertyName(Method method) {
        // future release - handle propertyName
        return serializedName(method.getName());
    }

    private static String serializedName(String methodName) {
        String[] prefixes = new String[] {"get", "set", "is"};
        String methodPrefix = null;
        for (String prefix : prefixes) {
            if (methodName.startsWith(prefix)) {
                methodPrefix = prefix;
            }
        }
        if (methodPrefix == null) {
            throw new IllegalArgumentException("Unknown Bean prefix for method: " + methodName);
        }
        String strippedName = methodName.substring(methodPrefix.length());

        // Make sure the first word or upper-case prefix is converted to lower-case
        char[] chars = strippedName.toCharArray();
        int pos = 0;
        while (pos < chars.length && Character.isUpperCase(chars[pos])) {
            chars[pos] = Character.toLowerCase(chars[pos]);
            pos++;
        }
        return new String(chars);
    }

}
