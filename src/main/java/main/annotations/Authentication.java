package main.annotations;

import io.jsonwebtoken.SignatureAlgorithm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Authentication {

	String secret();
	Class<?> encoder();
	long expiration() default 3600000L;
	SignatureAlgorithm algorithm() default SignatureAlgorithm.HS512;
	Class<?> authenticationSuccessHandler() default Void.class;
	Class<?> authenticationFailureHandler() default Void.class;
}
