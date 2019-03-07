package main;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import main.annotations.Authentication;
import main.annotations.CRUD;
import main.annotations.HASConfiguration;
import main.generators.CodeGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {


  private CodeGenerator codeGenerator;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);    this.codeGenerator = new CodeGenerator(processingEnv.getElementUtils(), processingEnv.getFiler(), processingEnv.getTypeUtils());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {

    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();

    annotations.add(CRUD.class.getCanonicalName());
    annotations.add(HASConfiguration.class.getCanonicalName());

    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    configureCodeGenerator(roundEnv);
    generateAuthentication(roundEnv);
    generateCrud(roundEnv);
    
    return true;
  }
  
  private void configureCodeGenerator(RoundEnvironment roundEnv) {
    
    for (Element el : roundEnv.getElementsAnnotatedWith(HASConfiguration.class)) {

      HASConfiguration config = el.getAnnotation(HASConfiguration.class);

      this.codeGenerator.setSaving(config.save());
      this.codeGenerator.setSavingOutput(config.savingOutput());
      this.codeGenerator.setSuffixes(config.suffixes());
      this.codeGenerator.setClassesPrefix(config.classesPrefix());
    }
  }
  
  private void generateAuthentication(RoundEnvironment roundEnv) {
    
    for (Element el : roundEnv.getElementsAnnotatedWith(Authentication.class)) {

      Authentication auth = el.getAnnotation(Authentication.class);

      this.codeGenerator.processAuthentication(auth, el, roundEnv);
    }
  }
  
  private void generateCrud(RoundEnvironment roundEnv) {
    
    for (Element element : roundEnv.getElementsAnnotatedWith(CRUD.class)) {

      try {
        this.codeGenerator.processCrud(element);
      } catch (RuntimeException e) {

        throw new RuntimeException("An exception occurred while generating code: " + e.getMessage());
      }
    }
  }
}
