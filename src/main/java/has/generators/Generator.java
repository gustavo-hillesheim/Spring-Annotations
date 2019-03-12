package has.generators;

import com.squareup.javapoet.TypeSpec;
import has.generators.args.Args;
import has.builders.AnnotationBuilder;
import has.builders.ParameterBuilder;
import has.utils.ElementUtils;

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