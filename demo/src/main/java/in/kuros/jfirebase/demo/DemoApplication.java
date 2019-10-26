package in.kuros.jfirebase.demo;

import com.google.cloud.firestore.Firestore;
import in.kuros.jfirebase.PersistenceService;
import in.kuros.jfirebase.demo.entity.Person;
import in.kuros.jfirebase.demo.entity.Person_;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.provider.firebase.PersistenceServiceFactory;

public class DemoApplication {

    private final PersistenceService persistenceService;

    public DemoApplication(final PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public static DemoApplication createDemoApplication(final Firestore firestore) {
        final PersistenceService persistenceService = PersistenceServiceFactory.create(firestore, "in.kuros.jrap.demo.entity");
        return new DemoApplication(persistenceService);
    }

    public void createPersonExample() {
        final Person p = new Person();
        p.setName("Rohit");

        persistenceService.create(p);

        System.out.println(p.getPersonId());
    }

    public void createPersonWithCustomIdExample() {
        final Person p = new Person();
        p.setPersonId("1");
        p.setName("Rohit");

        persistenceService.create(p);

        System.out.println(p.getPersonId());
    }

    public void updatePersonExample() {
        final Person p = new Person();
        p.setPersonId("1"); // optional if you want to create a new entry
        p.setName("Rohit");

        persistenceService.set(p);

        System.out.println(p.getPersonId());
    }

    public void updateField() {
        final Person p = new Person();
        p.setPersonId("1"); // optional if you want to create a new entry
        p.setAge(20);

        persistenceService.set(p, Person_.age);
    }

    public void updateMultipleFields() {
        persistenceService.set(AttributeValue.with(Person_.personId, "1")
                .with(Person_.name, "I changed my name")
                .with(Person_.age, 25).build());
    }
}
