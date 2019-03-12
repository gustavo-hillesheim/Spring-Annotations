package has.generators.authentication;

import java.io.IOException;
import javax.lang.model.element.Modifier;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import has.generators.CodeGenerator;
import has.generators.Generator;
import has.generators.args.NoArgs;

public class AuthorizationFilterGenerator extends Generator<NoArgs> {


	private String classesPrefix;

	public AuthorizationFilterGenerator(CodeGenerator codeGenerator, String classesPrefix) {

		super(codeGenerator);
		
		this.classesPrefix = classesPrefix;
	}

	public TypeSpec generate(NoArgs args) {

		MethodSpec constructor = MethodSpec.constructorBuilder()
			.addParameter(
			    this.parBuilder.name("authManager")
    			    .type(AuthenticationManager.class)
    			    .build())
			.addParameter(
			    this.parBuilder.name("jwtUtil")
			        .type(ClassName.get("", this.classesPrefix + "JWTUtil"))
			        .build())
			.addParameter(
			    this.parBuilder.name("userDetailsService")
    			    .type(UserDetailsService.class)
    			    .build())
			.addStatement("super(authManager)")
			.addStatement("this.jwtUtil = jwtUtil")
			.addStatement("this.userDetailsService = userDetailsService")
			.build();

		MethodSpec doFiterInternal = MethodSpec.methodBuilder("doFilterInternal")
			.addModifiers(Modifier.PROTECTED)
			.addAnnotation(Override.class)
			.addParameter(
			    this.parBuilder.name("request")
    			    .type(HttpServletRequest.class)
    			    .build())
			.addParameter(
			    this.parBuilder.name("response")
			        .type(HttpServletResponse.class)
			        .build())
			.addParameter(
			    this.parBuilder.name("chain")
			        .type(FilterChain.class)
			        .build())
			.addException(IOException.class)
			.addException(ServletException.class)
			.addStatement("$T header = request.getHeader($S)", String.class, "Authorization")
			.addStatement("$T auth = null",UsernamePasswordAuthenticationToken.class)
			.beginControlFlow(
                "if ((header != null && !header.isEmpty()) && header.startsWith($S))", "Bearer ")
            .addStatement("auth = this.getAuthentication(header.substring(7))")
        .endControlFlow()
        .addStatement("$T.getContext().setAuthentication(auth)", SecurityContextHolder.class)
        .addStatement("chain.doFilter(request, response)")
        .build();

        MethodSpec getAuthentication = MethodSpec.methodBuilder("getAuthentication")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(
                this.parBuilder.name("token")
                    .type(String.class)
                    .build())
            .addStatement("$T auth = null", UsernamePasswordAuthenticationToken.class)
    		.beginControlFlow("if (this.jwtUtil.validToken(token))")
				.addStatement("$T username = this.jwtUtil.getUsername(token)", String.class)
				.beginControlFlow("try")
					.addStatement("$T user = this.userDetailsService.loadUserByUsername(username)",
						UserDetails.class)
					.addStatement("auth = new $T(user, null, user.getAuthorities())",
					    UsernamePasswordAuthenticationToken.class)
				.nextControlFlow("catch ($T e)", UsernameNotFoundException.class)
				.endControlFlow()
			.endControlFlow()
			.addStatement("return auth")
			.returns(UsernamePasswordAuthenticationToken.class)
			.build();

		FieldSpec jwtUtilField = FieldSpec.builder(
			ClassName.get("", this.classesPrefix + "JWTUtil"),
			"jwtUtil",
			Modifier.PRIVATE)
			.build();

		FieldSpec udsField = FieldSpec.builder( UserDetailsService.class, 
		    "userDetailsService", Modifier.PRIVATE)
			.build();

		return TypeSpec.classBuilder(this.classesPrefix + "JWTAuthorizationFilter")
			.superclass(BasicAuthenticationFilter.class)
			.addField(jwtUtilField)
			.addField(udsField)
			.addMethod(constructor)
			.addMethod(doFiterInternal)
			.addMethod(getAuthentication)
			.build();
	}
}
