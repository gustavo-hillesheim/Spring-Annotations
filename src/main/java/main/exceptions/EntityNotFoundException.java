package main.exceptions;

public class EntityNotFoundException extends Exception {

	private String entityClassName;
	private Long id;

	public EntityNotFoundException(String entityClassName, Long id) {

		super("Could not find entity of type " + entityClassName + " with id " + id);

		this.entityClassName = entityClassName;
		this.id = id;
	}

	public String getEntityClassName() {

		return this.entityClassName;
	}

	public Long getId() {

		return this.id;
	}
}
