package in.kuros.jfirebase.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CollectionParent {
    Class<?> value() default IdReference.DEFAULT.class;
    String collection() default "";
}