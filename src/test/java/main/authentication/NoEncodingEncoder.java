package annotation.authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

public class NoEncodingEncoder implements PasswordEncoder {
	public String encode(CharSequence charSequence) {
		return charSequence.toString();
	}

	public boolean matches(CharSequence charSequence, String s) {

		return charSequence.toString().equals(s);
	}
}
