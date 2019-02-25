package main.generators;

import com.squareup.javapoet.TypeSpec;
import main.builders.AnnotationBuilder;
import main.builders.ParameterBuilder;
import main.generators.args.Args;
import main.utils.ElementUtils;

public abstract class Generator<Arg extends Args> {

  protected ParameterBuilder parBuilder;
  protected AnnotationBuilder annBuilder;
  protected ElementUtils eleUtils;
  
  public Generator(main.generators.CodeGenerator codeGenerator) {
    
    this.parBuilder = codeGenerator.parBuilder;
    this.annBuilder = codeGenerator.annBuilder;
    this.eleUtils = codeGenerator.eleUtils;
  }
  
  public abstract TypeSpec generate(Arg args);
}