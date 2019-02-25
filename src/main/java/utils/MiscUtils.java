package hava.annotation.spring.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.type.TypeMirror;

public class MiscUtils {


	public FieldSpec autowire(String fieldName, String typeName) {

		String[] names = this.splitQualifiedName(typeName);

		String packageName = names[0];
		String objectName = names[1];

		TypeName type = ClassName.get(packageName, objectName);

		return FieldSpec.builder(type, fieldName)
			.addAnnotation(Autowired.class).build();
	}

	public String[] splitQualifiedName(String qualifiedName) {

	    if (!qualifiedName.contains("."))
	      return new String[] {"", qualifiedName};
	  
		String objectName = qualifiedName.split("\\.")[qualifiedName.split("\\.").length -1];
		
		String packageName = qualifiedName.substring(0, qualifiedName.length() - objectName.length() - 1);

		return new String[]{packageName, objectName};
	}

	public TypeName getTypeName(TypeMirror type) {

		return TypeName.get(type);
	}

	public TypeName getTypeName(Class<?> typeClass) {

		return ClassName.get(typeClass);
	}
}
