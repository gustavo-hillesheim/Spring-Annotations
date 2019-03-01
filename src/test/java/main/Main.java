package main;

import main.authentication.NoEncodingEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import main.annotations.Authentication;
import main.annotations.HASConfiguration;

@SpringBootApplication
@HASConfiguration(save = true, savingOutput = "src/test/java")
@Authentication(secret = "GustavoLegal", encoder = NoEncodingEncoder.class)
public class Main {

  public static void main(String[] args) {
  
    SpringApplication.run(Main.class, args);
  }
}