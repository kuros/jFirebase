package in.kuros.jfirebase.reflection;

import in.kuros.jfirebase.metadata.Metadata;
import java.util.Objects;
import javassist.bytecode.ClassFile;
import org.reflections.Store;
import org.reflections.scanners.AbstractScanner;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;

public  class MetadataScanner extends AbstractScanner {

    public Object scan(Vfs.File file, Object classObject, Store store) {
        final ClassFile clf = (ClassFile) classObject;
        Class<?> clazz = null;
        try {
            if (Objects.isNull(classObject)) {
                clazz = loadClass(file);
            } else {
                clazz = loadClass(clf);
            }
            if (clazz.isAnnotationPresent(Metadata.class)) {
                store.put(Utils.index(this.getClass()), "metadata", clazz.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(clf)) {
            return clazz;
        }
        return classObject;
    }

    @Override public void scan(Object classObject, Store store) {
        scan(null, classObject, store);
    }

    private Class<?> loadClass(ClassFile clf) throws ClassNotFoundException {
        try {
            return Class.forName(clf.getName());
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(clf.getName());
        }
    }

    private Class<?> loadClass(Vfs.File file) throws ClassNotFoundException {
        String path = file.getRelativePath();
        String fqn = path.replace('/', '.');
        String className = fqn.replace(".class", "");
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
    }
}
