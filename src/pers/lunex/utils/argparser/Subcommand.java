package pers.lunex.utils.argparser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Subcommand {
    String id();
    String parent();
    boolean required() default false;
    String desc() default "no description.";
}
