package main;

import main.annotations.CRUD;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import main.annotations.Endpoint;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@CRUD(endpoint = "test")
public class User {

	@Id
	@GeneratedValue
	private Long id;
	private String nome;

	@Endpoint
	private String senha;

	public User() {}
}