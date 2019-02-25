package hava.annotation.spring.generators;

import com.squareup.javapoet.TypeSpec;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.builders.ParameterBuilder;
import hava.annotation.spring.generators.args.Args;
import hava.annotation.spring.utils.ElementUtils;

public abstract class Generator<Arg extends Args> {

  protected ParameterBuilder parBuilder;
  protected AnnotationBuilder annBuilder;
  protected ElementUtils eleUtils;
  
  public Generator(CodeGenerator codeGenerator) {
    
    this.parBuilder = codeGenerator.parBuilder;
    this.annBuilder = codeGenerator.annBuilder;
    this.eleUtils = codeGenerator.eleUtils;
  }
  
  public abstract TypeSpec generate(Arg args);
}