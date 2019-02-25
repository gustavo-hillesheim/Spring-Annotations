package main;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TesteRepository extends JpaRepository<User, Long> {

	User findByNome(String nome);
}
