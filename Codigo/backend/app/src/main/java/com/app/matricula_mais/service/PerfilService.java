package com.app.matricula_mais.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.PerfilRequest;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.model.Usuario;
import com.app.matricula_mais.repository.UsuarioRepository;
import com.app.matricula_mais.security.UsuarioAutenticado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerfilService {
    private final UsuarioRepository usuarios;
    private final CadastroUsuarioService cadastroUsuarios;

    public UsuarioAutenticado consultar(UsuarioAutenticado sessao) {
        return resposta(buscar(sessao.id()), sessao.perfil());
    }

    @Transactional
    public UsuarioAutenticado atualizar(UsuarioAutenticado sessao, PerfilRequest dados) {
        Usuario usuario = buscar(sessao.id());
        cadastroUsuarios.preencherDadosPessoais(usuario, dados.nome(), dados.login(),
            dados.email() == null ? usuario.getEmail() : dados.email());
        if (dados.senha() != null) {
            cadastroUsuarios.alterarSenha(usuario, dados.senha());
        }
        return resposta(usuarios.save(usuario), sessao.perfil());
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário"));
    }

    private UsuarioAutenticado resposta(Usuario usuario, String perfil) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getLogin(), perfil, usuario.getNome(), usuario.getEmail());
    }
}
