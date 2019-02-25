package main.generators.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import main.generators.CodeGenerator;
import main.generators.Generator;
import main.generators.args.TwoArgs;

public class AuthenticationFilterGenerator extends Generator<TwoArgs<TypeMirror, TypeMirror>> {


  private String classesPrefix;

  public AuthenticationFilterGenerator(CodeGenerator codeGenerator, String classesPrefix) {

    super(codeGenerator);
    
    this.classesPrefix = classesPrefix;
  }

  public TypeSpec generate(TwoArgs<TypeMirror, TypeMirror> args) {

    TypeMirror successHandler = args.one();
    TypeMirror failureHandler = args.two();

    TypeSpec.Builder builder = TypeSpec.classBuilder(
        this.classesPrefix + "JWTAuthenticationFilter")
        .superclass(UsernamePasswordAuthenticationFilter.class);

    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
        .addParameter(
            this.parBuilder.name("authManager")
                .type(AuthenticationManager.class)
                .build())
        .addParameter(
            this.parBuilder.name("jwtUtil")
                .type(ClassName.get("", this.classesPrefix + "JWTUtil"))
                .build())
        .addStatement("this.authManager = authManager")
        .addStatement("this.jwtUtil = jwtUtil");

    if ("java.lang.Void".equals(successHandler.toString())) {

      MethodSpec successfulAuthentication = MethodSpec.methodBuilder("successfulAuthentication")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PROTECTED)
          .addException(IOException.class)
          .addException(ServletException.class)
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
          .addParameter(
              this.parBuilder.name("authResult")
                  .type(Authentication.class)
                  .build())
          .addStatement("$T username = authResult.getName()", String.class)
          .beginControlFlow("if (username != null && !username.isEmpty())")
          .addStatement("$T token = this.jwtUtil.generateToken(username)", String.class)
          .addStatement("response.setStatus($T.SC_OK)", HttpServletResponse.class)
          .addStatement("response.setContentType($S)", "application/json")
          .addStatement("response.setCharacterEncoding($S)", "UTF-8")
          .addStatement("response.getWriter().append(this.createSuccessBody(token))")
          .endControlFlow()
          .build();

      builder.addMethod(successfulAuthentication);
    } else {

      constructorBuilder.addStatement("setAuthenticationSuccessHandler(new $T())", successHandler);
    }

    if ("java.lang.Void".equals(failureHandler.toString())) {

      MethodSpec unsuccessfulAuthentication = MethodSpec.methodBuilder("unsuccessfulAuthentication")
          .addAnnotation(Override.class).addModifiers(Modifier.PROTECTED)
          .addException(IOException.class).addException(ServletException.class)
          .addParameter(
              this.parBuilder.name("request")
                  .type(HttpServletRequest.class)
                  .build())
          .addParameter(
              this.parBuilder.name("response")
                  .type(HttpServletResponse.class)
                  .build())
          .addParameter(
              this.parBuilder.name("e")
                  .type(AuthenticationException.class)
                  .build())
          .addStatement("response.setStatus($T.SC_UNAUTHORIZED)", HttpServletResponse.class)
          .addStatement("response.setContentType($S)", "application/json")
          .addStatement("response.setCharacterEncoding($S)", "UTF-8")
          .addStatement("response.getWriter().append(this.createUnsuccessBody(e.getMessage()))")
          .build();

      builder.addMethod(unsuccessfulAuthentication);
    } else {

      constructorBuilder.addStatement("setAuthenticationFailureHandler(new $T())", failureHandler);
    }

    MethodSpec constructor = constructorBuilder.build();

    MethodSpec attemptAuthentication = MethodSpec.methodBuilder("attemptAuthentication")
        .addParameter(
            this.parBuilder.name("request")
                .type(HttpServletRequest.class)
                .build())
        .addParameter(
            this.parBuilder.name("response")
                .type(HttpServletResponse.class)
                .build())
        .addException(AuthenticationException.class)
        .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
        .addStatement("$T jsonString = \"\"", String.class)
        .addStatement("$T line = \"\"", String.class)
        .beginControlFlow("try")
            .beginControlFlow("while ((line = request.getReader().readLine()) != null)")
                .addStatement("jsonString += line")
            .endControlFlow()
        .nextControlFlow("catch ($T e)", IOException.class)
            .addStatement("throw new $T(e.getMessage())", RuntimeException.class)
        .endControlFlow()
        .addStatement("$T jsonObj = new $T(jsonString)", JSONObject.class, JSONObject.class)
        .addStatement("$T username = jsonObj.get($S).toString()", String.class, "username")
        .addStatement("$T password = jsonObj.get($S).toString()", String.class, "password")
        .addStatement("$T authToken = new $T(username, password, new $T<>())",
            UsernamePasswordAuthenticationToken.class,
            UsernamePasswordAuthenticationToken.class,
            ArrayList.class)
        .addStatement("return this.authManager.authenticate(authToken)")
        .returns(Authentication.class)
        .build();

    MethodSpec createSuccessBody = MethodSpec.methodBuilder("createSuccessBody")
        .addModifiers(Modifier.PRIVATE)
        .addParameter(
            this.parBuilder.name("token")
                .type(String.class)
                .build())
        .addStatement("return \"" 
            + "{\\\"timestamp\\\": \" + new $T().getTime() + \", "
            + "\\\"status\\\": \" + $T.SC_OK + \", " 
            + "\\\"token\\\": \\\"\" + token + \"\\\", "
            + "\\\"message\\\": \\\"Authenticated successfully\\\"}\"", 
            Date.class, HttpServletResponse.class)
        .returns(String.class).build();

    MethodSpec createUnsuccessBody = MethodSpec.methodBuilder("createUnsuccessBody")
        .addModifiers(Modifier.PRIVATE)
        .addParameter(
            this.parBuilder.name("message")
                .type(String.class)
                .build())
        .addStatement("return \"" 
            + "{\\\"timestamp\\\": \" + new $T().getTime() + \", "
            + "\\\"status\\\": \" + $T.SC_UNAUTHORIZED + \", "
            + "\\\"error\\\": \\\"Could not authenticate\\\", "
            + "\\\"message\\\": \" + message + \"}\"", 
            Date.class, HttpServletResponse.class)
        .returns(String.class).build();

    FieldSpec jwtUtilField =
        FieldSpec.builder(
            ClassName.get("", this.classesPrefix + "JWTUtil"),
            "jwtUtil", Modifier.PRIVATE)
        .build();

    FieldSpec authManagerField = FieldSpec.builder(
            AuthenticationManager.class,
            "authManager", Modifier.PRIVATE)
        .build();

    return builder
        .addMethod(constructor)
        .addMethod(attemptAuthentication)
        .addMethod(createSuccessBody)
        .addMethod(createUnsuccessBody)
        .addField(jwtUtilField)
        .addField(authManagerField)
        .build();
  }
}
