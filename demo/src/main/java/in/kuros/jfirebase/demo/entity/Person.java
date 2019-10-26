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
    private Integer age;

    @CreateTime
    private Date created;
    @UpdateTime
    private Date lastModified;
}
