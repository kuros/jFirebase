package in.kuros.jfirebase.reflection;

import in.kuros.jfirebase.metadata.Metadata;
import javassist.bytecode.ClassFile;
import org.reflections.scanners.AbstractScanner;

public class MetadataScanner extends AbstractScanner {
    @Override
    public void scan(final Object cls) {
        final ClassFile clf = (ClassFile) cls;
        try {
            if (loadClass(clf).isAnnotationPresent(Metadata.class)) {
                getStore().put(clf.getName(), "");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Class<?> loadClass(ClassFile clf) throws ClassNotFoundException {
        try {
            return Class.forName(clf.getName());
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(clf.getName());
        }
    }
}
