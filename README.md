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

Let's say you want to map sub collection 
