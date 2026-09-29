package com.app.matricula_mais.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.security.UsuarioAutenticado;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {
    // Login e logout são processados pelos filtros configurados em SecurityConfig.
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    public UsuarioAutenticado consultarUsuario(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }
}
