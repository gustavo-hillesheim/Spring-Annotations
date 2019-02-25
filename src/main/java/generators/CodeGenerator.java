package hava.annotation.spring.generators;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Entity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.annotations.Authentication;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.HASConfiguration;
import hava.annotation.spring.annotations.Suffixes;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.args.Args;
import hava.annotation.spring.generators.authentication.AuthenticationConfiguratorGenerator;
import hava.annotation.spring.generators.authentication.AuthenticationFilterGenerator;
import hava.annotation.spring.generators.authentication.AuthorizationFilterGenerator;
import hava.annotation.spring.generators.authentication.UtilGenerator;
import hava.annotation.spring.generators.authentication.WebConfigGenerator;
import hava.annotation.spring.generators.crud.ControllerGenerator;
import hava.annotation.spring.generators.crud.RepositoryGenerator;
import hava.annotation.spring.generators.crud.ServiceGenerator;
import hava.annotation.spring.utils.ElementUtils;
import io.jsonwebtoken.SignatureAlgorithm;

public class CodeGenerator {


  public ParameterBuilder parBuilder = new ParameterBuilder();
  AnnotationBuilder annBuilder = new AnnotationBuilder();
  ElementUtils eleUtils;
  private StringUtils strUtils = new StringUtils();

  private Filer filer;
  private Elements elementUtils;
  private Types typeUtils;

  private boolean debug;
  private String repSuffix;
  private String serSuffix;
  private String conSuffix;
  private String classesPrefix;

  private String generatedFor;

  public CodeGenerator(Elements elementUtils, Filer filer, Types typeUtils) {

    this.filer = filer;
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;

    try {
      setDebug((Boolean) HASConfiguration.class.getDeclaredMethod("debug").getDefaultValue());
      setSuffixes(
          (Suffixes) HASConfiguration.class.getDeclaredMethod("suffixes").getDefaultValue());
      setClassesPrefix(
          (String) HASConfiguration.class.getDeclaredMethod("classesPrefix").getDefaultValue());
    } catch (Exception e) {
      throw new RuntimeException("Could not load default HASConfigurations: " + e.getMessage());
    }
  }


  public void processAuthentication(final Authentication auth, Element element,
      RoundEnvironment roundEnv) throws RuntimeException {

    this.generatedFor = "JWT Authentication";
    
    String packageName = this.elementUtils.getPackageOf(element).getQualifiedName().toString();

    boolean createWebConfig = shouldCreateWebConfig(roundEnv);

    TypeMirror encoderType = this.getTypeMirror(auth::encoder);
    TypeMirror successAuthHandlerType = this.getTypeMirror(auth::authenticationSuccessHandler);
    TypeMirror failureAuthHandlerType = this.getTypeMirror(auth::authenticationFailureHandler);

    boolean useEncoderGetInstance = validateEncoder(encoderType);
    validateHandlers(successAuthHandlerType, failureAuthHandlerType);

    String secret = auth.secret();
    SignatureAlgorithm algorithm = auth.algorithm();
    Long expiration = auth.expiration();

    generateAuthClasses(encoderType, successAuthHandlerType, failureAuthHandlerType, 
                    packageName, secret, useEncoderGetInstance, createWebConfig, 
                    expiration, algorithm);
  }
  
  private void generateAuthClasses(TypeMirror encoderType, TypeMirror successAuthHandlerType, TypeMirror failureAuthHandlerType,
                               String packageName, String secret, boolean useEncoderGetInstance, boolean createWebConfig, 
                               Long expiration, SignatureAlgorithm algorithm) {
    
    WebConfigGenerator webConfigGenerator = new WebConfigGenerator(this, this.classesPrefix);
    
    UtilGenerator utilGenerator = new UtilGenerator(this, this.classesPrefix);
    
    AuthenticationFilterGenerator authFilterGenerator =
        new AuthenticationFilterGenerator(this, this.classesPrefix);
    
    AuthorizationFilterGenerator authoFilterGenerator =
        new AuthorizationFilterGenerator(this, this.classesPrefix);
    
    AuthenticationConfiguratorGenerator authConfigGenerator =
        new AuthenticationConfiguratorGenerator(this, this.classesPrefix);


    save(authConfigGenerator.generate(Args.of(encoderType, useEncoderGetInstance)), packageName);

    if (createWebConfig)
      save(webConfigGenerator.generate(Args.of()), packageName);

    save(utilGenerator.generate(Args.of(secret, expiration, algorithm)), packageName);

    save(authFilterGenerator.generate(Args.of(successAuthHandlerType, failureAuthHandlerType)), packageName);

    save(authoFilterGenerator.generate(Args.of()), packageName);
  }

  private boolean shouldCreateWebConfig(RoundEnvironment roundEnv) {

    return roundEnv.getElementsAnnotatedWith(EnableWebSecurity.class).stream()
        .filter(ewsEl -> ewsEl.getAnnotation(Configuration.class) != null
              && this.typeUtils.directSupertypes(ewsEl.asType()).contains(this.elementUtils
                .getTypeElement(WebSecurityConfigurerAdapter.class.getCanonicalName()).asType()))
        .collect(Collectors.toList()).size() == 0;
  }

  private TypeMirror getTypeMirror(Runnable error) {

    try {
      error.run();
    } catch (MirroredTypeException e) {
      return e.getTypeMirror();
    }

    return null;
  }

  private void validateHandlers(TypeMirror success, TypeMirror failure) {

    boolean extendsSuccessHandler = false;
    final String authSuccessHandlerName = AuthenticationSuccessHandler.class.getCanonicalName();
    List<? extends TypeMirror> successSupers = this.typeUtils.directSupertypes(success);

    for (TypeMirror successSuper : successSupers) {

      if (authSuccessHandlerName.equals(successSuper.toString()))
        extendsSuccessHandler = true;
    }

    if (!"java.lang.Void".equals(success.toString())) {

      if (!extendsSuccessHandler)
        throw new RuntimeException(
            "A class used as authenticationSuccessHandler for @Autentication must implement "
                + authSuccessHandlerName);

      if (!hasNoArgsConstructor(this.typeUtils.asElement(success)))
        throw new RuntimeException(
            "A class used as authenticationSuccessHandler for @Authentication must have a public constructor with no arguments");
    }
    
    boolean extendsFailureHandler = false;
    final String authFailureHandlerName = AuthenticationFailureHandler.class.getCanonicalName();
    List<? extends TypeMirror> failureSupers = this.typeUtils.directSupertypes(failure);
    
    
    for (TypeMirror failureSuper : failureSupers) {

      if (authFailureHandlerName.equals(failureSuper.toString()))
        extendsFailureHandler = true;
    }

    if (!"java.lang.Void".equals(failure.toString())) {

      if (!extendsFailureHandler)
        throw new RuntimeException(
            "A class used as authenticationFailureHandler for @Autentication must extend "
                + authFailureHandlerName);

      if (!hasNoArgsConstructor(this.typeUtils.asElement(failure)))
        throw new RuntimeException(
            "A class used as authenticationFailureHandler for @Authentication must have a public constructor with no arguments");
    }
  }

  private boolean validateEncoder(TypeMirror type) {

    Element encoderEle = this.typeUtils.asElement(type);

    boolean noArgsConstructor = this.hasNoArgsConstructor(encoderEle);
    boolean haveGetInstance = false;

    for (Element element : encoderEle.getEnclosedElements()) {

      if (element.getKind() == ElementKind.METHOD) {

        String name = element.getSimpleName().toString();
        String erasure = this.typeUtils.erasure(element.asType()).toString();
        if ("getInstance".equals(name)
            && (("()" + type.toString()).equals(erasure)
                || ("()" + "org.springframework.security.crypto.password.PasswordEncoder")
                    .equals(erasure))
            && (element.getModifiers().contains(Modifier.PUBLIC))
            && (element.getModifiers().contains(Modifier.STATIC)))
          haveGetInstance = true;
      }
    }

    if (!noArgsConstructor && !haveGetInstance)
      throw new RuntimeException(
          "A class used as encoder for @Authentication must have a public constructor with no arguments or a public static getInstance method");

    if (!haveGetInstance)
      System.out.println(String.format(
          "----- Consider creating a public static %s getInstance() method at %s -----",
          type.toString(), type.toString()));

    List<? extends TypeMirror> encoderSuperClasses = this.typeUtils.directSupertypes(type);
    String passwordEncoderPath = "org.springframework.security.crypto.password.PasswordEncoder";

    boolean implementsPassEncoder = false;

    for (TypeMirror superClassType : encoderSuperClasses) {

      if (superClassType.toString().equals(passwordEncoderPath))
        implementsPassEncoder = true;
    }

    if (!implementsPassEncoder) {
      throw new RuntimeException(
          "A class used as encoder for @Authentication must implement org.springframework.security.crypto.password.PasswordEncoder");
    }

    return haveGetInstance;

  }

  private boolean hasNoArgsConstructor(Element element) {

    for (Element el : element.getEnclosedElements()) {

      if (el.getKind() == ElementKind.CONSTRUCTOR) {
        if ("()void".equals(this.typeUtils.erasure(el.asType()).toString())
            && el.getModifiers().contains(Modifier.PUBLIC))
          return true;
      }
    }

    return false;
  }

  
  public void processCrud(Element element) throws RuntimeException {

    if (element.getKind() != ElementKind.CLASS) {
      throw new RuntimeException("A element annotated with CRUD must be a class");
    }

    if (element.getAnnotationsByType(Entity.class).length == 0) {
      throw new RuntimeException(
          "A element annotated with CRUD must be annotated with \"javax.persistence.Entity\"");
    }

    this.eleUtils = new ElementUtils(element, elementUtils);

    String prefix = element.getSimpleName().toString();
    String packageName = this.eleUtils.packageOf(element);

    CRUD annCrud = element.getAnnotation(CRUD.class);
    prefix = "".equals(annCrud.name()) ? prefix : annCrud.name();
    String endpoint = "".equals(annCrud.endpoint()) ? prefix.toLowerCase() : annCrud.endpoint();
    
    generateCrudClasses(prefix, endpoint, packageName, annCrud);
  }
  
  private void generateCrudClasses(String prefix, String endpoint, String packageName, CRUD annCrud) {
    
    RepositoryGenerator repGenerator =
        new RepositoryGenerator(this, this.repSuffix, this.classesPrefix);
    
    ServiceGenerator serGenerator =
        new ServiceGenerator(this, this.serSuffix, this.repSuffix, this.classesPrefix);
    
    ControllerGenerator conGenerator =
        new ControllerGenerator(this, this.conSuffix, this.serSuffix, this.classesPrefix);
    
    save(repGenerator.generate(Args.of(prefix, annCrud)), packageName);
    save(serGenerator.generate(Args.of(prefix, annCrud)), packageName);
    save(conGenerator.generate(Args.of(prefix, endpoint, annCrud)), packageName);
  }

  private void save(TypeSpec spec, String packageName) throws RuntimeException {

    JavaFile file = JavaFile.builder(packageName, spec).build();

    String generatedFor =
        this.eleUtils != null ? this.eleUtils.elementTypeStr() : this.generatedFor;

    try {
      if (debug) {
        System.out.println("\n"
            + this.strUtils.center(String.format("Class generated for %s", generatedFor)));

        file.writeTo(System.out);

        System.out.println(this.strUtils.center(""));
      }

      file.writeTo(this.filer);
    } catch (IOException e) {
      throw new RuntimeException("Could not write class to filer: " + e.getMessage());
    }
  }


  public void setDebug(boolean debug) {

    this.debug = debug;
  }

  public void setSuffixes(Suffixes suffixes) {

    this.repSuffix = suffixes.repository();
    this.serSuffix = suffixes.service();
    this.conSuffix = suffixes.controller();
  }

  public void setClassesPrefix(String classesPrefix) {

    this.classesPrefix = classesPrefix;
  }

  private class StringUtils {

    String center(String s) {

      int size = 75;
      char pad = '-';

      if (s == null || size <= s.length())
        return s;

      StringBuilder sb = new StringBuilder(size);
      for (int i = 0; i < (size - s.length()) / 2; i++) {
        sb.append(pad);
      }
      sb.append(s);
      while (sb.length() < size) {
        sb.append(pad);
      }
      return sb.toString();
    }
  }
}
