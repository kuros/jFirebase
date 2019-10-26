package in.kuros.jfirebase.demo;

import com.google.cloud.firestore.Firestore;
import in.kuros.jfirebase.PersistenceService;
import in.kuros.jfirebase.demo.entity.Employee;
import in.kuros.jfirebase.demo.entity.Employee_;
import in.kuros.jfirebase.demo.entity.Person;
import in.kuros.jfirebase.demo.entity.Person_;
import in.kuros.jfirebase.metadata.AttributeValue;
import in.kuros.jfirebase.metadata.RemoveAttribute;
import in.kuros.jfirebase.provider.firebase.PersistenceServiceFactory;
import in.kuros.jfirebase.provider.firebase.query.QueryBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void createSubCollection() {
        final Employee employee = new Employee();
        employee.setPersonId("1");
        employee.setEmployeeId("123"); // Optional if you want custom id
        employee.setJoiningDate(new Date());
        employee.setSalary(5000);

        final Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("office", "123-345-567");
        phoneNumbers.put("home", "456-789-456");
        employee.setPhoneNumbers(phoneNumbers);

        persistenceService.create(employee);
    }

    public void updateMapUsingKeyValue() {
        persistenceService.set(AttributeValue
                .with(Employee_.employeeId, "123") // Required field
                .with(Employee_.personId, "1") // Required field
                .with(Employee_.phoneNumbers, "home", "111-111-111")
                .build());
    }

    public void updateCompleteMapValues() {
        final Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("office", "123-345-XXX");
        phoneNumbers.put("home", "456-789-XXX");

        persistenceService.set(AttributeValue
                .with(Employee_.employeeId, "123") // Required field
                .with(Employee_.personId, "1") // Required field
                .with(Employee_.phoneNumbers, phoneNumbers)
                .build());
    }

    public void removeFields() {
        persistenceService.remove(RemoveAttribute.withKeys(Employee_.personId, "1")
                .withKey(Employee_.employeeId, "123")
                .remove(Employee_.salary)
                .removeMapKey(Employee_.phoneNumbers, "home"));
    }

    public void deleteCompleteRecord() {
        final Employee employee = new Employee();
        employee.setEmployeeId("123");
        employee.setPersonId("1");
        persistenceService.delete(employee);
    }

    public void query() {
        final List<Employee> employees = persistenceService
                .find(QueryBuilder
                        .collection(Person.class)
                        .withId("1")
                        .subCollection(Employee.class)
                        .withId("123")
                        .whereGreaterThan(Employee_.salary, 1000));
        System.out.println(employees);
    }

    public void queryFindById() {
        final Employee employee = persistenceService
                .findById(QueryBuilder
                        .collection(Person.class)
                        .withId("1")
                        .subCollection(Employee.class)
                        .withId("123"));
    }

    public void querySelectedFields() {
        final List<Employee> employees = persistenceService
                .find(QueryBuilder
                        .collection(Person.class)
                        .withId("1")
                        .subCollection(Employee.class)
                        .withId("123")
                        .select(Employee_.employeeId, Employee_.salary));

    }

    public void runTransactionExample() {
        final List<Employee> updatedEmployees = persistenceService.runTransaction(transaction -> {
            final List<Employee> employees = transaction.get(QueryBuilder
                    .collection(Person.class)
                    .withId("1")
                    .subCollection(Employee.class)
                    .withId("123"));

            employees.stream()
                    .peek(emp -> emp.setSalary(6000))
                    .forEach(transaction::set);
            return employees;
        });
    }

    public void runBatchExample() {
        persistenceService.writeInBatch(writeBatch -> {
            writeBatch.set(AttributeValue
                    .with(Person_.personId, "2") // Required field
                    .with(Person_.name, "Jon") // Required field
                    .with(Person_.age, 25)
                    .build());

            writeBatch.set(AttributeValue
                    .with(Employee_.employeeId, "123") // Required field
                    .with(Employee_.personId, "2") // Required field
                    .with(Employee_.phoneNumbers, "home", "222-222-222")
                    .build());
        });
    }
}
