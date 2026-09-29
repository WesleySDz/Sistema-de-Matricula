package com.app.matricula_mais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.ConfigurableApplicationContext;
import com.app.matricula_mais.cli.TerminalApplication;

@SpringBootApplication
@EnableScheduling
public class MatriculaMaisApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext contexto = SpringApplication.run(MatriculaMaisApplication.class, args);
		if (contexto.getEnvironment().getProperty("app.cli.enabled", Boolean.class, false)) {
			try (contexto) {
				contexto.getBean(TerminalApplication.class).executar();
			}
		}
	}

}
