package hava.annotation.spring.configurators;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface AuthenticationConfigurator {

	void configure(HttpSecurity http, AuthenticationManager authManager) throws Exception;
	void configure(AuthenticationManagerBuilder auth) throws Exception;
}
