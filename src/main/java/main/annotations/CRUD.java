package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CRUD {

	String endpoint() default "";
	String name() default "";
	boolean pagination() default true;
	Filter filter() default @Filter;
}
