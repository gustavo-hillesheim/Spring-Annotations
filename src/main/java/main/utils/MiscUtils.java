package main.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public class MiscUtils {


	public TypeName getTypeName(TypeMirror type) {

		return TypeName.get(type);
	}

	public TypeName getTypeName(Class<?> typeClass) {

		return ClassName.get(typeClass);
	}
}
