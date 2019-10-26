package in.kuros.jfirebase.demo.entity;

import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.Parent;
import in.kuros.jfirebase.entity.UpdateTime;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
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
