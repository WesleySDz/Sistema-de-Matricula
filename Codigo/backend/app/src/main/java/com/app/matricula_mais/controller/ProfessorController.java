package com.app.matricula_mais.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.dto.Requisicoes.ProfessorRequest;
import com.app.matricula_mais.dto.Respostas.ProfessorResponse;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.security.UsuarioAutenticado;
import com.app.matricula_mais.service.ProfessorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SECRETARIA')")
public class ProfessorController {
    private final ProfessorService service;

    @GetMapping
    public List<ProfessorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProfessorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> cadastrar(@Valid @RequestBody ProfessorRequest dados) {
        ProfessorResponse criado = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/professores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ProfessorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfessorRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/disciplinas")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<DisciplinaResponse> minhasDisciplinas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.consultarDisciplinas(usuario.id());
    }

    @GetMapping("/me/disciplinas/{disciplinaId}/alunos")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<AlunoResponse> alunosMatriculados(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long disciplinaId, @RequestParam Long semestreId) {
        return service.consultarAlunos(usuario.id(), disciplinaId, semestreId);
    }
}
