package in.kuros.jfirebase.reflection;

import in.kuros.jfirebase.metadata.Metadata;
import javassist.bytecode.ClassFile;
import org.reflections.Store;
import org.reflections.scanners.AbstractScanner;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;

public  class MetadataScanner extends AbstractScanner {
    private Class<?> loadClass(ClassFile clf) throws ClassNotFoundException {
        try {
            return Class.forName(clf.getName());
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(clf.getName());
        }
    }

    public Object scan(Vfs.File file, Object classObject, Store store) {
        final ClassFile clf = (ClassFile) classObject;
        try {
            if (loadClass(clf).isAnnotationPresent(Metadata.class)) {
                store.put(Utils.index(this.getClass()), "metadata", "");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classObject;
    }

    @Override public void scan(Object classObject, Store store) {
        scan(null, classObject, store);
    }
}
