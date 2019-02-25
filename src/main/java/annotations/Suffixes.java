package hava.annotation.spring.annotations;

public @interface Suffixes {

	String repository() default "Repository";
	String service() default "Service";
	String controller() default "Controller";
}
