package com.app.matricula_mais.security;

import java.io.Serializable;
import java.security.Principal;

public record UsuarioAutenticado(Long id, String login, String perfil, String nome, String email)
        implements Principal, Serializable {
    public UsuarioAutenticado(Long id, String login, String perfil) {
        this(id, login, perfil, login, null);
    }

    @Override
    public String getName() {
        return login;
    }
}
