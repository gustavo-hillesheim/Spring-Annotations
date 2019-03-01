package main.generators.authentication;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import main.configurators.AuthenticationConfigurator;
import main.generators.CodeGenerator;
import main.generators.Generator;
import main.generators.args.TwoArgs;

public class AuthenticationConfiguratorGenerator extends Generator<TwoArgs<TypeMirror, Boolean>> {


	private String classesPrefix;

	public AuthenticationConfiguratorGenerator(CodeGenerator codeGenerator, String classesPrefix) {

		super(codeGenerator);
		
		this.classesPrefix = classesPrefix;
	}

	@Override
	public TypeSpec generate(TwoArgs<TypeMirror, Boolean> args) {
	  
	    TypeMirror passwordEncoder = args.one();
	    boolean useEncoderGetInstance = args.two();
	  
		MethodSpec configureHttp = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addException(Exception.class)
			.addParameter(
			    this.parBuilder.name("http")
			        .type(HttpSecurity.class)
			        .build())
			.addParameter(
			    this.parBuilder.name("authenticationManager")
			        .type(AuthenticationManager.class)
			        .build())
			.addStatement(
				"http.addFilter(new $L(authenticationManager, jwtUtil))",
				ClassName.get("", this.classesPrefix + "JWTAuthenticationFilter"))
			.addStatement(
				"http.addFilter(new $L(authenticationManager, jwtUtil, userDetailsService))",
				ClassName.get("", this.classesPrefix + "JWTAuthorizationFilter"))
			.addStatement(
				"http.authorizeRequests().antMatchers(\"/login\").permitAll()",
				HttpMethod.class)
			.build();

		MethodSpec configureAuth = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addException(Exception.class)
			.addParameter(
			    this.parBuilder.name("auth")
			        .type(AuthenticationManagerBuilder.class)
			        .build())
			.addStatement("auth.userDetailsService(this.userDetailsService).passwordEncoder(this.getPasswordEncoder())")
			.build();

		MethodSpec getPasswordEncoder = generateGetPasswordEncoder(passwordEncoder, useEncoderGetInstance);

		return TypeSpec.classBuilder(this.classesPrefix + "AuthenticationConfiguratorImpl")
			.addAnnotation(Service.class)
			.addSuperinterface(AuthenticationConfigurator.class)
			.addField(
			    FieldSpec.builder(ClassName.get("", this.classesPrefix + "JWTUtil"), 
			        "jwtUtil", Modifier.PRIVATE)
    			    .addAnnotation(Autowired.class)
    			    .build())
			.addField(
			    FieldSpec.builder(UserDetailsService.class, 
			        "userDetailsService", Modifier.PRIVATE)
    			    .addAnnotation(Autowired.class)
    			    .build())
			.addMethod(configureHttp)
			.addMethod(configureAuth)
			.addMethod(getPasswordEncoder)
			.build();
	}

	private MethodSpec generateGetPasswordEncoder(TypeMirror passwordEncoder, boolean useEncoderGetInstance) {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("getPasswordEncoder")
			.addModifiers(Modifier.PRIVATE)
			.returns(PasswordEncoder.class);

		System.out.println(passwordEncoder.toString());
		if (useEncoderGetInstance)
			builder.addStatement("return $T.getInstance()", passwordEncoder);
		else {
			builder.addStatement("return new $T()", passwordEncoder)
				.addException(IllegalAccessException.class)
				.addException(InstantiationException.class);
		}

		return builder.build();
	}
}
