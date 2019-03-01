package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface HASConfiguration {

	boolean save() default false;
	String savingOutput() default "src/main/java";
	Suffixes suffixes() default @Suffixes;
	String classesPrefix() default "AG";
}
