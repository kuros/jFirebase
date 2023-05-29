package in.kuros.jfirebase.reflection;

import in.kuros.jfirebase.metadata.Metadata;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import in.kuros.jfirebase.util.ClassLoaderUtil;
import javassist.bytecode.ClassFile;
import org.reflections.Store;
import org.reflections.scanners.AbstractScanner;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;

public  class MetadataScanner extends AbstractScanner {

    public static final String METADATA = "metadata";

    public Object scan(Vfs.File file, Object classObject, Store store) {
        final ClassFile clf = (ClassFile) classObject;
        Class<?> clazz = null;
        try {
            if (Objects.isNull(classObject)) {
                clazz = ClassLoaderUtil.loadClass(file);
            } else {
                clazz = ClassLoaderUtil.loadClass(clf);
            }

            indexAnnotatedClass(store, clazz);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(clf)) {
            return clazz;
        }
        return classObject;
    }

    private void indexAnnotatedClass(Store store, Class<?> clazz) {
        if (clazz.isAnnotationPresent(Metadata.class)) {
            store.put(Utils.index(this.getClass()), METADATA, clazz.getName());
        }

        // check if any of the annotations is annotated with @Metadata if previous check failed
        if (ClassLoaderUtil.isAnnotationPresent(clazz, Metadata.class)) {
            store.put(Utils.index(this.getClass()), METADATA, clazz.getName());
        }
    }

    @Override public void scan(Object classObject, Store store) {
        scan(null, classObject, store);
    }
}
