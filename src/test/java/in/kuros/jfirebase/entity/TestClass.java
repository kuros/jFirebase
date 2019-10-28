package in.kuros.jfirebase.entity;

import lombok.Data;

import java.util.Map;

@Data
@Entity("test")
public class TestClass {
    @Id
    private String testId;
    private String testValue;
    private Map<String, String> testMap;
}
