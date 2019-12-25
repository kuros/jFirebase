package in.kuros.jfirebase.reflection;

import in.kuros.jfirebase.metadata.Metadata;
import javassist.bytecode.ClassFile;
import org.reflections.scanners.AbstractScanner;

public class MetadataScanner extends AbstractScanner {
    @Override
    public void scan(final Object cls) {
        final ClassFile clf = (ClassFile) cls;
        try {
            if (Class.forName(clf.getName()).isAnnotationPresent(Metadata.class)) {
                getStore().put(clf.getName(), "");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
