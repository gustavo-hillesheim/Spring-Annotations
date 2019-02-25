package main;

import annotation.authentication.NoEncodingEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import hava.annotation.spring.annotations.Authentication;
import hava.annotation.spring.annotations.HASConfiguration;

@SpringBootApplication
@HASConfiguration(debug = true)
@Authentication(secret = "GustavoLegal", encoder = NoEncodingEncoder.class)
public class Main {

  public static void main(String[] args) {
  
    SpringApplication.run(Main.class, args);
  }
}