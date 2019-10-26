package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.metadata.Attribute;
import in.kuros.jfirebase.metadata.MapAttribute;
import in.kuros.jfirebase.metadata.Metadata;

import java.util.Date;

@Metadata(Employee.class)
public class Employee_ {

    public static volatile Attribute<Employee, String> employeeId;
    public static volatile Attribute<Employee, String> personId;
    public static volatile Attribute<Employee, Date> joiningDate;
    public static volatile Attribute<Employee, Integer> salary;
    public static volatile MapAttribute<Employee, String, String> phoneNumbers;
    public static volatile Attribute<Employee, Date> modifiedDate;

}
