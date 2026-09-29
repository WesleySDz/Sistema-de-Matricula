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
        Long id = usuario.getId() == null ? -1L : usuario.getId();
        if (usuarios.existsByLoginAndIdNot(login, id)) {
            throw new RegraNegocioException("Login já cadastrado.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve possuir no máximo 72 bytes em UTF-8.");
        }
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setEmail(email);
    }
}
