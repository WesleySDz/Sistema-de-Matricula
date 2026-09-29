package com.app.matricula_mais.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.security.UsuarioAutenticado;
import com.app.matricula_mais.dto.Requisicoes.PerfilRequest;
import com.app.matricula_mais.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {
    private final PerfilService perfis;
    // Login e logout são processados pelos filtros configurados em SecurityConfig.
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    public UsuarioAutenticado consultarUsuario(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfis.consultar(usuario);
    }

    @PutMapping("/me")
    public UsuarioAutenticado atualizarUsuario(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody PerfilRequest dados) {
        return perfis.atualizar(usuario, dados);
    }
}
