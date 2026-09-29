package com.app.matricula_mais.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.model.FuncionarioSecretaria;
import com.app.matricula_mais.model.Professor;
import com.app.matricula_mais.model.Usuario;
import com.app.matricula_mais.repository.UsuarioRepository;
import com.app.matricula_mais.security.UsuarioAutenticado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutenticacaoService implements AuthenticationProvider {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(noRollbackFor = { BadCredentialsException.class, LockedException.class })
    public Authentication authenticate(Authentication autenticacao) {
        Usuario usuario = usuarios.buscarParaAutenticar(autenticacao.getName())
                .orElseThrow(() -> new BadCredentialsException("Login ou senha inválidos."));
        LocalDateTime agora = LocalDateTime.now();
        if (usuario.getBloqueadoAte() != null) {
            if (usuario.getBloqueadoAte().isAfter(agora)) {
                throw new LockedException("Conta bloqueada temporariamente por 15 minutos.");
            }
            usuario.setBloqueadoAte(null);
            usuario.setTentativasInvalidas(0);
        }
        String senha = autenticacao.getCredentials() == null ? "" : autenticacao.getCredentials().toString();
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            usuario.setTentativasInvalidas(usuario.getTentativasInvalidas() + 1);
            if (usuario.getTentativasInvalidas() >= 5) {
                usuario.setBloqueadoAte(agora.plusMinutes(15));
                throw new LockedException("Conta bloqueada temporariamente por 15 minutos.");
            }
            throw new BadCredentialsException("Login ou senha inválidos.");
        }
        String perfil;
        if (usuario instanceof Aluno) {
            perfil = "ALUNO";
        } else if (usuario instanceof Professor) {
            perfil = "PROFESSOR";
        } else if (usuario instanceof FuncionarioSecretaria) {
            perfil = "SECRETARIA";
        } else {
            throw new BadCredentialsException("Usuário sem perfil de acesso.");
        }
        usuario.setTentativasInvalidas(0);
        usuario.registrarAcesso();
        return UsernamePasswordAuthenticationToken.authenticated(
                new UsuarioAutenticado(usuario.getId(), usuario.getLogin(), perfil), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
    }

    @Override
    public boolean supports(Class<?> tipo) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipo);
    }
}
