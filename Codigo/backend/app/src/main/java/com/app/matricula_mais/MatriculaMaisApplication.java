package com.app.matricula_mais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MatriculaMaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatriculaMaisApplication.class, args);
	}

}
