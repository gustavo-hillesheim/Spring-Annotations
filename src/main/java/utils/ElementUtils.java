package hava.annotation.spring.utils;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import hava.annotation.spring.builders.AnnotationBuilder;
import hava.annotation.spring.builders.ParameterBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ElementUtils {


	private Element element;
	private TypeName elementType;
	private TypeName elementIdType = null;
	private HashMap<String, Element> enclosedElement = new HashMap<>();

	private ParameterBuilder parUtils = new ParameterBuilder();
	private AnnotationBuilder annUtils = new AnnotationBuilder();
	private MiscUtils miscUtils = new MiscUtils();
	private Elements utils;

	public ElementUtils(Element element, Elements utils) {

		this.element = element;
		this.utils = utils;

		this.elementType = this.miscUtils.getTypeName(element.asType());

		for (Element el : this.element.getEnclosedElements()) {

			if (el.getAnnotation(Id.class) != null) {
				this.elementIdType = this.miscUtils.getTypeName(el.asType());
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

	public String packageName() {

		return this.packageOf(this.element);
	}

	public Element getEnclosedElement(String name) {

		if (name == null)
			return null;

		return this.enclosedElement.get(name);
	}

	public String getEnclosedTypeStr(String elementName) {

		return this.getEnclosedElement(elementName).asType().toString();
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

		return this.parUtils.name("entity").type(this.elementType).build();
	}

	public ParameterSpec elementReqBodyParam() {

		return this.parUtils.name("entity").type(this.elementType).annotation(RequestBody.class).build();
	}

	public ParameterSpec elementIdParam() {

		return this.parUtils.name("id").type(this.elementIdType).build();
	}

	public ParameterSpec elementIdPathParam() {

		return this.parUtils.name("id").type(this.elementIdType).annotation(this.annUtils.build(PathVariable.class, "id")).build();
	}
}
