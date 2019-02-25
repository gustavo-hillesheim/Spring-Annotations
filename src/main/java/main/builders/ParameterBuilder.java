package main.builders;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import main.utils.MiscUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class ParameterBuilder {


	private MiscUtils miscUtils = new MiscUtils();
	
	private String name;
	private TypeName type;
	private List<AnnotationSpec> annotations = new ArrayList<>();
	private List<Modifier> modifiers = new ArrayList<>();

	
	public ParameterBuilder name(String name) {
	  
	  this.name = name;
	  return this;
	}
	
	public ParameterBuilder type(TypeName type) {
	  
	  this.type = type;
	  return this;
	}
	
	public ParameterBuilder type(Class<?> type) {
	  
	  this.type = this.miscUtils.getTypeName(type);
	  return this;
	}
	
	public ParameterBuilder type(TypeMirror type) {
	  
	  this.type = this.miscUtils.getTypeName(type);
	  return this;
	}
	
	public ParameterBuilder annotation(AnnotationSpec annotation) {
	  
	  this.annotations.add(annotation);
	  return this;
	}
	
	public ParameterBuilder annotation(Class<? extends Annotation> annotation) {
	  
	  this.annotations.add(AnnotationSpec.builder(annotation).build());
	  return this;
	}
	
	public ParameterBuilder annotations(Class<? extends Annotation>... annotations) {
	  
	  Arrays.stream(annotations).forEach(ann -> this.annotations.add(AnnotationSpec.builder(ann).build()));
	  return this;
	}
	
	public ParameterBuilder modifier(Modifier modifier) {
	  
	  this.modifiers.add(modifier);
	  return this;
	}
	
	public ParameterBuilder modifiers(Modifier... modifiers) {

		this.modifiers.addAll(Arrays.asList(modifiers));
	  return this;
	}
	
	public ParameterSpec build() {
	  
	  ParameterSpec.Builder builder = ParameterSpec.builder(type, name, modifiers.toArray(new Modifier[] {}));
	  
	  this.annotations.forEach(builder::addAnnotation);
	  
	  this.type = null;
	  this.name = null;
	  this.modifiers.clear();
	  this.annotations.clear();
	  
	  return builder.build();
	}
}
