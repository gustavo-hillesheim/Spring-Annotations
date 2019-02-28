package main.authentication;

import main.TesteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private TesteRepository repository;

	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

		main.User user = this.repository.findByNome(s);

		if (user == null)
			throw new UsernameNotFoundException("Não foi possível encontrar o usuário com Nome " + s);

		return new User(user.getNome(), user.getSenha(), new ArrayList<>());
	}
}
