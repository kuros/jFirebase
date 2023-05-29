package in.kuros.jfirebase.util;

import in.kuros.jfirebase.metadata.Metadata;
import javassist.bytecode.ClassFile;
import org.reflections.vfs.Vfs;

import java.util.Arrays;
import java.util.Optional;

public class ClassLoaderUtil {

    public static Class<?> loadClass(String clf) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(clf);
        } catch (ClassNotFoundException e) {
            return Class.forName(clf);
        }
    }

    public static Class<?> loadClass(Vfs.File file) throws ClassNotFoundException {
        String path = file.getRelativePath();
        String fqn = path.replace('/', '.');
        String className = fqn.replace(".class", "");
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return Class.forName(className);
        }
    }

    public static Class<?> loadClass(ClassFile clf) throws ClassNotFoundException {
        try {
            return Class.forName(clf.getName());
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(clf.getName());
        }
    }

    public static boolean isAnnotationPresent(Class<?> clazz, Class annotationClass) {
        Optional<String> annotationOptional = Arrays.stream(clazz.getAnnotations())
                .map(k -> k.annotationType().getName())
                .filter(a -> a.equals(annotationClass.getName()))
                .findAny();
        return annotationOptional.isPresent();
    }
}
