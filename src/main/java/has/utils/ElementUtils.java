package has.utils;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import has.builders.AnnotationBuilder;
import has.builders.ParameterBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ElementUtils {


	private Element element;
	private TypeName elementType;
	private TypeName elementIdType = null;
	private HashMap<String, Element> enclosedElement = new HashMap<>();

	private ParameterBuilder parBuilder = new ParameterBuilder();
	private AnnotationBuilder annUtils = new AnnotationBuilder();
	private Elements utils;

	public ElementUtils(Element element, Elements utils) {

		MiscUtils miscUtils = new MiscUtils();

		this.element = element;
		this.utils = utils;

		this.elementType = miscUtils.getTypeName(element.asType());

		for (Element el : this.element.getEnclosedElements()) {

			if (el.getAnnotation(Id.class) != null) {
				this.elementIdType = miscUtils.getTypeName(el.asType());
			}
		}

		for (Element el : element.getEnclosedElements()) {

			this.enclosedElement.put(el.getSimpleName().toString(), el);
		}

		if (this.elementIdType == null)
			throw new RuntimeException("A Entity must have a field annotated with javax.persistence.Id");
	}

	public String packageOf(Element element) {

		return this.utils.getPackageOf(element).getQualifiedName().toString();
	}

	public Element getEnclosedElement(String name) {

		if (name == null)
			return null;

		return this.enclosedElement.get(name);
	}

	public String getEnclosedTypeStr(String elementName) {

		return this.getEnclosedElement(elementName).asType().toString();
	}

	public List<? extends Element> getEnclosedElementsAnnotatedWith(Class<? extends Annotation> annotation) {

		return this.getEnclosedElements().stream()
			.filter(el -> ((Element) el).getAnnotation(annotation) != null)
			.collect(Collectors.toList());
	}

	public List<? extends Element> getEnclosedElements() {

		return this.element.getEnclosedElements();
	}

	public List<? extends Element> getNonTransientFields() {

		return getEnclosedElements().stream()
			.filter(el -> el.getAnnotation(Transient.class) == null
				&& el.getKind() == ElementKind.FIELD
			)
			.collect(Collectors.toList());
	}

	public List<String> getNonTransientFieldsNames() {

		return this.getNonTransientFields()
			.stream().map(f -> f.getSimpleName().toString())
			.collect(Collectors.toList());
	}

	public String getParameters(ExecutableElement element,
	                            RequestMethod method, String bodyVariableName) {

		StringBuilder methodParameters = new StringBuilder("(");

		for (int i = 0; i < element.getParameters().size(); i++) {

			VariableElement param = element.getParameters().get(i);
			String paramName = param.getSimpleName().toString();

			if (method == RequestMethod.POST)
				methodParameters.append("(" + param.asType().toString() + ") "
					+ bodyVariableName + ".get(\"" + paramName + "\")");
			else
				methodParameters.append(paramName);

			if (i < element.getParameters().size() - 1)
				methodParameters.append(" ,");
		}
		methodParameters.append(")");

		return methodParameters.toString();
	}

	public String getParameters(ExecutableElement element) {

		return this.getParameters(element, RequestMethod.GET, "");
	}

	public void addParameter(MethodSpec.Builder builder, ExecutableElement element) {

		for (int i = 0; i < element.getParameters().size(); i++) {

			VariableElement param = element.getParameters().get(i);
			String paramName = param.getSimpleName().toString();

			builder.addParameter(this.parBuilder
				.type(param.asType())
				.name(paramName)
				.build());
		}
	}

	public void addRequestParameter(MethodSpec.Builder builder, ExecutableElement element) {

		for (int i = 0; i < element.getParameters().size(); i++) {

			VariableElement param = element.getParameters().get(i);
			String paramName = param.getSimpleName().toString();

			builder.addParameter(this.parBuilder
				.type(param.asType())
				.name(paramName)
				.annotation(this.annUtils.requestParam(true))
				.build());
		}
	}

	public String elementSimpleName() {

		return this.element.getSimpleName().toString();
	}

	public String elementTypeStr() {

		return this.elementType.toString();
	}

	public String elementIdTypeStr() {

		return this.elementIdType.toString();
	}

	public ParameterSpec elementParam() {

		return this.parBuilder.name("entity").type(this.elementType).build();
	}

	public ParameterSpec elementReqBodyParam() {

		return this.parBuilder.name("entity").type(this.elementType).annotation(RequestBody.class).build();
	}

	public ParameterSpec elementIdParam() {

		return this.parBuilder.name("id").type(this.elementIdType).build();
	}

	public ParameterSpec elementIdPathParam() {

		return this.parBuilder.name("id").type(this.elementIdType).annotation(this.annUtils.build(PathVariable.class, "id")).build();
	}
}
