package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import main.annotations.HASConfiguration;

@SpringBootApplication
@HASConfiguration
//@Authentication(secret = "GustavoLegal", encoder = NoEncodingEncoder.class)
public class Main {

	public static void main(String[] args) {

		SpringApplication.run(Main.class, args);
	}
}