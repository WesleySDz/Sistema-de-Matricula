package com.app.matricula_mais.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.dto.Requisicoes.DisciplinaRequest;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.MatriculaResponse;
import com.app.matricula_mais.service.DisciplinaService;
import com.app.matricula_mais.service.MatriculaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/disciplinas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SECRETARIA')")
public class DisciplinaController {
    private final DisciplinaService service;
    private final MatriculaService matriculas;

    @GetMapping
    public List<DisciplinaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public DisciplinaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<DisciplinaResponse> cadastrar(@Valid @RequestBody DisciplinaRequest dados) {
        DisciplinaResponse criado = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/disciplinas/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public DisciplinaResponse atualizar(@PathVariable Long id, @Valid @RequestBody DisciplinaRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/matriculas")
    public List<MatriculaResponse> consultarMatriculas(
            @PathVariable Long id, @RequestParam Long semestreId) {
        return matriculas.consultarPorDisciplina(id, semestreId);
    }
}
