package com.app.matricula_mais.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import com.app.matricula_mais.cli.ApiClient;
import com.app.matricula_mais.cli.TerminalApplication;
import com.app.matricula_mais.cli.TerminalIO;
import tools.jackson.databind.ObjectMapper;

@Configuration
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class TerminalConfig {
    // Criados após a inicialização do servidor, inclusive quando sua porta é dinâmica.
    @Bean
    @Lazy
    public ApiClient terminalApiClient(WebServerApplicationContext contexto, ObjectMapper json) {
        return new ApiClient(URI.create("http://127.0.0.1:" + contexto.getWebServer().getPort()), json);
    }

    @Bean
    @Lazy
    public TerminalApplication terminalApplication(ApiClient cliente) {
        return new TerminalApplication(cliente, TerminalIO.sistema());
    }
}
