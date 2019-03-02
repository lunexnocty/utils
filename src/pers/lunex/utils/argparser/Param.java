package pers.lunex.utils.argparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Param {
    String id();
    String[] names() default { };
    boolean required() default false;
    Type type() default Type.BOOL;
    int arity() default 0;
    int index() default -1;
    String desc() default "no description.";
    boolean dynamic() default false;
    String[] conflicts() default { };
    String def() default "";
}
