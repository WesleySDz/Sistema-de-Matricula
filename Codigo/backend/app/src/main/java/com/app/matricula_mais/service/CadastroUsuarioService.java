package com.app.matricula_mais.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Usuario;
import com.app.matricula_mais.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CadastroUsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public void preencher(Usuario usuario, String nome, String login, String senha, String email) {
        preencherDadosPessoais(usuario, nome, login, email);
        alterarSenha(usuario, senha);
    }

    public void preencherDadosPessoais(Usuario usuario, String nome, String login, String email) {
        Long id = usuario.getId();
        if (id == null) {
            id = -1L;
        }
        if (usuarios.existsByLoginAndIdNot(login, id)) {
            throw new RegraNegocioException("Login já cadastrado.");
        }
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setEmail(email);
    }

    public void alterarSenha(Usuario usuario, String senha) {
        if (senha.isBlank() || senha.length() < 8) {
            throw new RegraNegocioException("A senha deve possuir pelo menos 8 caracteres e não pode ser vazia.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve possuir no máximo 72 bytes em UTF-8.");
        }
        usuario.setSenha(passwordEncoder.encode(senha));
    }
}
