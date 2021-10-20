package in.kuros.jfirebase.provider.firebase;

import com.google.cloud.firestore.Firestore;
import in.kuros.jfirebase.PersistenceService;
import in.kuros.jfirebase.metadata.MetadataProcessor;
import in.kuros.jfirebase.util.PropertyNamingStrategy;

public final class PersistenceServiceFactory {

    public static PersistenceService create(final Firestore firestore, final String... basePackages) {
        MetadataProcessor.init(basePackages);
        final PersistenceServiceImpl persistenceService = new PersistenceServiceImpl(firestore);
        PersistenceService.init();
        return persistenceService;
    }

    public static PersistenceService create(final Firestore firestore, PropertyNamingStrategy namingStrategy, final String... basePackages) {
        MetadataProcessor.init(basePackages);
        final PersistenceServiceImpl persistenceService = new PersistenceServiceImpl(firestore, namingStrategy);
        PersistenceService.init();
        return persistenceService;
    }

}
