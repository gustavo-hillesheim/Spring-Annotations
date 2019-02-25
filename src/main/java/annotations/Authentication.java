package hava.annotation.spring.annotations;

import io.jsonwebtoken.SignatureAlgorithm;

public @interface Authentication {

	String secret();
	Class<?> encoder();
	long expiration() default 3600000L;
	SignatureAlgorithm algorithm() default SignatureAlgorithm.HS512;
	Class<?> authenticationSuccessHandler() default Void.class;
	Class<?> authenticationFailureHandler() default Void.class;
}
