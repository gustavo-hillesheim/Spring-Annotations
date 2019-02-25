package main.generators.authentication;

import javax.lang.model.element.Modifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import main.configurators.AuthenticationConfigurator;
import main.generators.CodeGenerator;
import main.generators.Generator;
import main.generators.args.NoArgs;

public class WebConfigGenerator extends Generator<NoArgs> {
  

	private String classesPrefix;

	public WebConfigGenerator(CodeGenerator codeGenerator, String classesPrefix) {

		super(codeGenerator);
		
		this.classesPrefix = classesPrefix;
	}

	public TypeSpec generate(NoArgs args) {

		MethodSpec configureHttp = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
			    this.parBuilder.name("http")
			        .type(HttpSecurity.class)
			        .build())
			.addStatement("http.cors().and().csrf().disable()")
			.addStatement("this.authenticationConfig.configure(http, authenticationManager())")
			.addStatement("http.authorizeRequests().anyRequest().authenticated().antMatchers(\"/login\").permitAll()")
			.build();

		MethodSpec configureAuth = MethodSpec.methodBuilder("configure")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PROTECTED)
			.addException(Exception.class)
			.addParameter(
			    this.parBuilder.name("auth")
			        .type(AuthenticationManagerBuilder.class)
			        .build())
			.addStatement("this.authenticationConfig.configure(auth)")
			.build();

		FieldSpec authConfig = FieldSpec.builder(AuthenticationConfigurator.class, 
		    "authenticationConfig", Modifier.PRIVATE)
		    .addAnnotation(Autowired.class)
		    .build();
		
		return TypeSpec.classBuilder(this.classesPrefix + "WebSecurityConfigurer")
			.addAnnotation(Configuration.class)
			.addAnnotation(EnableWebSecurity.class)
			.superclass(WebSecurityConfigurerAdapter.class)
			.addField(authConfig)
			.addMethod(configureHttp)
			.addMethod(configureAuth)
			.build();
	}
}