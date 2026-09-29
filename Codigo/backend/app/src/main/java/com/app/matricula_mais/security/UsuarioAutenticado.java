package com.app.matricula_mais.security;

import java.io.Serializable;
import java.security.Principal;

public record UsuarioAutenticado(Long id, String login, String perfil) implements Principal, Serializable {
    @Override
    public String getName() {
        return login;
    }
}
