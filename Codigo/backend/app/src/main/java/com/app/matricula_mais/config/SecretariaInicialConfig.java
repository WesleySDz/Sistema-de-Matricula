package com.app.matricula_mais.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.app.matricula_mais.model.FuncionarioSecretaria;
import com.app.matricula_mais.repository.UsuarioRepository;
import com.app.matricula_mais.service.CadastroUsuarioService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecretariaInicialConfig implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final CadastroUsuarioService cadastro;
    @Value("${app.secretaria.login:}")
    private String login;
    @Value("${app.secretaria.senha:}")
    private String senha;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (login.isBlank() && senha.isBlank()) {
            return;
        }
        if (login.isBlank() || senha.length() < 8) {
            throw new IllegalArgumentException("Informe o login e uma senha com ao menos oito caracteres para a secretaria inicial.");
        }
        if (usuarios.findByLogin(login).isEmpty()) {
            FuncionarioSecretaria secretaria = new FuncionarioSecretaria();
            cadastro.preencher(secretaria, "Secretaria", login, senha, null);
            usuarios.save(secretaria);
        }
    }
}
