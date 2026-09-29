package com.app.matricula_mais.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.dto.Requisicoes.MatriculaRequest;
import com.app.matricula_mais.dto.Respostas.*;
import com.app.matricula_mais.security.UsuarioAutenticado;
import com.app.matricula_mais.service.MatriculaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUNO')")
public class MatriculaController {
    private final MatriculaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatriculaResponse> realizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody MatriculaRequest dados) {
        return service.realizar(usuario.id(), dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        service.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public List<MatriculaResponse> consultarAtual(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Long semestreId) {
        return service.consultarAtual(usuario.id(), semestreId);
    }

    @GetMapping("/me/disciplinas")
    public List<DisciplinaResponse> consultarDisciplinas(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Long semestreId) {
        return service.consultarDisciplinas(usuario.id(), semestreId);
    }

    @GetMapping("/me/historico")
    public List<MatriculaResponse> consultarHistorico(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.consultarHistorico(usuario.id());
    }
}
