package in.kuros.jfirebase.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IdReference {
    Class<?> value() default DEFAULT.class;

    String collection() default "";

    final class DEFAULT {}
}
