# JRAP

A utility to help users to work with nosql databases.

# Supported NOSQL DB's

- Firebase

# Usage

## Pojo
We will use a simple pojo class and convert it into an jrap Entity.

```java

package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.entity.CreateTime;
import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.UpdateTime;
import lombok.Data;

import java.util.Date;

@Data
@Entity("person")
public class Person {

    @Id
    private String personId;
    private String name;

    @CreateTime
    private Date created;
    @UpdateTime
    private Date lastModified;
}
```
You will also need to crate a Metadata class for corresponding entity.

```java
package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.Metadata;

import java.util.Date;

@Metadata(Person.class)
public class Person_ {

    public static volatile Attribute<Person, String> personId;
    public static volatile Attribute<Person, String> name;
    public static volatile Attribute<Person, Date> created;
    public static volatile Attribute<Person, Date> lastModified;
}
```

## initialise PeristenceService

```java
// initialise with firestore & base package
final PersistenceService persistenceService = PersistenceServiceFactory.create(firestore, "in.kuros.jfirebase.demo.entity");
``` 

## Creating object

To create a new entry in database use _create_ method. It will auto generate Id & createTime will be updated.

```java
    public void createPersonExample() {
        final Person p = new Person();
        p.setName("Rohit");

        persistenceService.create(p);

        System.out.println(p.getPersonId());
    }
```

## Creating object with id

Create method will throw exception if entity with id already exists.

```java
    public void createPersonWithCustomIdExample() {
        final Person p = new Person();
        p.setPersonId("1");
        p.setName("Rohit");

        persistenceService.create(p);

        System.out.println(p.getPersonId());
    }
```

## Update/Silent create

Use _set_ method to create/update entity silently, ie. if entity exists, it will be updated else a new entry will be created.

```java
    public void updatePersonExample() {
        final Person p = new Person();
        p.setPersonId("1"); // optional if you want to create a new entry
        p.setName("Rohit");

        persistenceService.set(p);

        System.out.println(p.getPersonId());
    }
```

## Updating field

Let's say I want to update age of person. We need to provide entity with id and age field populated (in this case personId is a requiredField). 
```java
    public void updateField() {
        final Person p = new Person();
        p.setPersonId("1"); // optional if you want to create a new entry
        p.setAge(20);

        persistenceService.set(p, Person_.age);
    }
```

## Updating multiple fields

We can update multiple fields, Use AttributeValue

```java
    public void updateMultipleFields() {
        persistenceService.set(AttributeValue.with(Person_.personId, "1")
                .with(Person_.name, "I changed my name")
                .with(Person_.age, 25).build());
    }
```

## Working with sub hierarchy

Let's say you want to map sub collection, you need to create a pojo with parent id reference.

```java
package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.Parent;
import in.kuros.jfirebase.entity.UpdateTime;

import java.util.Date;
import java.util.Map;

@Entity("employee")
public class Employee {

    @Id
    private String employeeId;

    @Parent
    @IdReference(Person.class)
    private String personId;

    private Date joiningDate;
    private Integer salary;
    private Map<String, String> phoneNumbers;

    @UpdateTime
    private Date modifiedDate;
}
```

Here we are using @Parent with @IdReference to map parent information.

Also note, to save phone numbers we are using a map. Its corresponding Metadata class will be:

```java
package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;

import java.util.Date;

@Entity("employee")
public class Employee_ {

    public static volatile Attribute<Employee, String> employeeId;
    public static volatile Attribute<Employee, String> personId;
    public static volatile Attribute<Employee, Date> joiningDate;
    public static volatile Attribute<Employee, Integer> salary;
    public static volatile MapAttribute<Employee, String, String> phoneNumbers;
    public static volatile Attribute<Employee, Date> modifiedDate;

}
```

Now to create a Employee entry within Person collection, simply create Employee object with person reference.

```java
    public void createSubCollection() {
        final Employee employee = new Employee();
        employee.setEmployeeId("123"); // Optional if you want custom id
        employee.setPersonId("1");
        employee.setJoiningDate(new Date());
        employee.setSalary(5000);

        final Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("office", "123-345-567");
        phoneNumbers.put("home", "456-789-456");
        employee.setPhoneNumbers(phoneNumbers);

        persistenceService.create(employee);
    }
```

## Update Map values

Let's we want to update specific value in a map (home phone number).

```java
    public void updateMapUsingKeyValue() {
        persistenceService.set(AttributeValue
                .with(Employee_.employeeId, "123") // Required field
                .with(Employee_.personId, "1") // Required field
                .with(Employee_.phoneNumbers, "home", "111-111-111")
                .build());
    }
```

or we can completely replace all the phoneNumbers
```java
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
```

## Removing Fields
In order to delete just a field from the entry, Use: 
```java
    public void removeFields() {
        persistenceService.remove(RemoveAttribute.withKeys(Employee_.personId, "1")
                .withKey(Employee_.employeeId, "123")
                .remove(Employee_.salary)
                .removeMapKey(Employee_.phoneNumbers, "home"));
    }
``` 
Here we have deleted salary field and an entry of 'home' from phone numbers.

## Delete Record

To delete a complete record:

```java
    public void deleteCompleteRecord() {
        final Employee employee = new Employee();
        employee.setEmployeeId("123");
        employee.setPersonId("1");
        persistenceService.delete(employee);
    }
```
You need to populate required id fields.

## Query

To query you need to provide classes in order.

Let's say you want to find with salary greater than 1000.
```java
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
```

## Find by Id

To find a record by Id:
```java
    public void queryFindById() {
        final Employee employee = persistenceService
                .findById(QueryBuilder
                        .collection(Person.class)
                        .withId("1")
                        .subCollection(Employee.class)
                        .withId("123"));
    }
```

## Select few fields
 
Let's say you want to fetch only salaries of all the employees:
```java
    public void querySelectedFields() {
        final List<Employee> employees = persistenceService
                .find(QueryBuilder
                        .collection(Person.class)
                        .withId("1")
                        .subCollection(Employee.class)
                        .withId("123")
                        .select(Employee_.employeeId, Employee_.salary));

    }
```

## Running Transaction

You can run the transaction and execute multiple queries in it.

```java
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
```

## Batching 

You can batch multiple statements and commit them at once.

```java
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
``` 
 
