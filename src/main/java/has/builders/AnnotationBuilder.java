package has.builders;

import com.squareup.javapoet.AnnotationSpec;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;

public class AnnotationBuilder {


	public AnnotationSpec build(Class<? extends Annotation> annotationClass) {

		return AnnotationSpec.builder(annotationClass).build();
	}

	public AnnotationSpec build(Class<? extends Annotation> annotationClass, Object value) {

		return this.build(annotationClass, "value", value);
	}

	public AnnotationSpec build(Class<? extends Annotation> annotationClass, String member, Object value) {

		String memberValue = "java.lang.String".equals(value.getClass().getName()) ? "\"" + value + "\"" : value.toString();

		return AnnotationSpec.builder(annotationClass).addMember(member, memberValue).build();
	}

	public AnnotationSpec postMapping(String value) {

		return this.build(PostMapping.class, value);
	}

	public AnnotationSpec getMapping(String value) {

		return this.build(GetMapping.class, value);
	}

	public AnnotationSpec deleteMapping(String value) {

		return this.build(DeleteMapping.class, value);
	}

	public AnnotationSpec requestMapping(String value) {

		return this.build(RequestMapping.class, value);
	}

	public AnnotationSpec requestParam(boolean required) {

		return this.build(RequestParam.class,
			"required", required);
	}
}
