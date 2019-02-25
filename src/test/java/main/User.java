package main;

import main.annotations.CRUD;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
  private String senha;

  public User() {}
  
  public String getNome() { return this.nome; }
  public String getSenha() { return this.senha; }
}