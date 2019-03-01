package main.generators.crud;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import main.annotations.CRUD;
import main.annotations.Endpoint;
import main.annotations.Filter;
import main.exceptions.EntityNotFoundException;
import main.generators.CodeGenerator;
import main.generators.Generator;
import main.generators.args.FourArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;

public class ControllerGenerator extends Generator<FourArgs<String, String, CRUD, List<? extends Element>>> {

	private String suffix;
	private String serSuffix;
	private String classesPrefix;

	private boolean pagination;

	public ControllerGenerator(CodeGenerator codeGenerator, String suffix, String serSuffix, String classesPrefix) {

		super(codeGenerator);
		
		this.suffix = suffix;
		this.serSuffix = serSuffix;
		this.classesPrefix = classesPrefix;
	}

	public TypeSpec generate(FourArgs<String, String, CRUD, List<? extends Element>> args) {

	    String name = args.one();
	    String endpoint = args.two();
	    CRUD crud = args.three();
	    List<? extends Element> endpoints = args.four();
	  
		this.pagination = crud.pagination();

		MethodSpec one = MethodSpec.methodBuilder("one")
			.addException(EntityNotFoundException.class)
			.addAnnotation(this.annBuilder.getMapping("{id}"))
			.addParameter(this.eleUtils.elementIdPathParam())
			.addStatement("return this.service.one(id)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec all = crud.filter().fields().length > 0 ? filterableAll(crud.filter()) : simpleAll();

		MethodSpec save = MethodSpec.methodBuilder("save")
			.addAnnotation(this.annBuilder.postMapping(""))
			.addParameter(this.eleUtils.elementReqBodyParam())
			.addStatement("return this.service.save(entity)")
			.returns(ResponseEntity.class)
			.build();

		MethodSpec delete = MethodSpec.methodBuilder("delete")
			.addAnnotation(this.annBuilder.deleteMapping("{id}"))
			.addParameter(this.eleUtils.elementIdPathParam())
			.addStatement("return this.service.delete(id)")
			.returns(ResponseEntity.class)
			.build();
		
		String serviceClassName = this.classesPrefix + name + serSuffix;
		
		TypeSpec.Builder builder = TypeSpec.classBuilder(this.classesPrefix + name + this.suffix)
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(RestController.class)
			.addAnnotation(this.annBuilder.requestMapping("/" + endpoint))
			.addField(FieldSpec.builder(
			    ClassName.get("", serviceClassName), "service")
			        .addAnnotation(Autowired.class)
			        .build())
			.addMethod(all)
			.addMethod(one)
			.addMethod(save)
			.addMethod(delete);

		endpoints.forEach(el -> builder.addMethod(createEndpoint(el)));

		return builder.build();
	}

	private MethodSpec simpleAll() {

		MethodSpec.Builder builder = MethodSpec.methodBuilder("all")
			.addAnnotation(this.annBuilder.getMapping(""));

		if (!this.pagination)
			builder.addStatement("return $T.ok(this.service.all())", ResponseEntity.class);
		else {
			addPageability(builder);
			builder.addStatement("return $T.ok(this.service.all(page, pageSize))", ResponseEntity.class);
		}

		return builder
			.returns(ResponseEntity.class)
			.build();
	}

	private MethodSpec filterableAll(Filter filter) {

		String[] fields = filter.fields();

		if (fields.length == 1 && "*".equals(fields[0])) {

			fields = this.eleUtils
				.getNonTransientFieldsNames()
				.toArray(new String[]{});
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder("allByFilter")
			.addAnnotation(this.annBuilder.getMapping(""));

		if (this.pagination)
			addPageability(builder);

		return this.addArguments(builder, fields)
			.addStatement(getReturn(fields), ResponseEntity.class)
			.returns(ResponseEntity.class)
			.build();
	}

	private MethodSpec createEndpoint(Element element) {

		String elementName = element.getSimpleName().toString();
		String endpointValue = element.getAnnotation(Endpoint.class).value();
		String path = "{id}/" + ("".equals(endpointValue) ? elementName : endpointValue);

		return MethodSpec.methodBuilder(elementName)
			.addAnnotation(this.annBuilder.getMapping(path))
			.addParameter(this.eleUtils.elementIdPathParam())
			.addException(EntityNotFoundException.class)
			.addStatement("return this.service." + elementName + "(id)")
			.returns(ResponseEntity.class)
			.build();
	}

	private String getReturn(String[] fields) {

		StringBuilder builder = new StringBuilder("return this.service.allByFilter(");

		for (int i = 0; i < fields.length; i++) {

			String field = fields[i];
			builder.append(field);

			if (i < fields.length - 1)
				builder.append(", ");
		}

		if (this.pagination)
			builder.append(", page, pageSize");

		builder.append(")");
		return builder.toString();
	}

	private void addPageability(MethodSpec.Builder builder) {

		builder
			.addParameter(
			    this.parBuilder.name("page")
			        .type(Integer.class)
			        .annotation(this.annBuilder.requestParam(false))
			        .build())
			.addParameter(
			    this.parBuilder.name("pageSize")
			        .type(Integer.class)
			        .annotation(this.annBuilder.requestParam(false))
			        .build());
	}

	private MethodSpec.Builder addArguments(MethodSpec.Builder builder, String[] fields) {

		Arrays.stream(fields).forEach(field -> {

			TypeMirror fieldType = this.eleUtils.getEnclosedElement(field).asType();

			builder.addParameter(
			    this.parBuilder.name(field)
			        .type(fieldType)
			        .annotation(this.annBuilder.requestParam(false))
			        .build());
		});

		return builder;
	}
}
