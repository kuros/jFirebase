package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.Firestore;
import in.kuros.jfirebase.PersistenceService;
import in.kuros.jfirebase.metadata.MetadataProcessor;

public final class PersistenceServiceFactory {

    public static PersistenceService create(final Firestore firestore, final String... basePackages) {
        MetadataProcessor.init(basePackages);
        return new PersistenceServiceImpl(firestore);
    }

}
