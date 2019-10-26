package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.Metadata;

import java.util.Date;

@Metadata(Person.class)
public class Person_ {

    public static volatile Attribute<Person, String> personId;
    public static volatile Attribute<Person, String> name;
    public static volatile Attribute<Person, Integer> age;
    public static volatile Attribute<Person, Date> created;
    public static volatile Attribute<Person, Date> lastModified;
}
