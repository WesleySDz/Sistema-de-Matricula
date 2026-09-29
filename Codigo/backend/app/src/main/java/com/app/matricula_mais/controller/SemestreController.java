package com.app.matricula_mais.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.app.matricula_mais.dto.Requisicoes.*;
import com.app.matricula_mais.dto.Respostas.*;
import com.app.matricula_mais.service.SemestreService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/semestres")
@RequiredArgsConstructor
public class SemestreController {
    private final SemestreService service;

    @GetMapping
    public List<SemestreResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SemestreResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<SemestreResponse> cadastrar(@Valid @RequestBody SemestreRequest dados) {
        SemestreResponse criado = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/semestres/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}/curriculo")
    @PreAuthorize("hasRole('SECRETARIA')")
    public SemestreResponse gerarCurriculo(@PathVariable Long id, @Valid @RequestBody CurriculoRequest dados) {
        return service.gerarCurriculo(id, dados);
    }

    @GetMapping("/{id}/situacao-disciplinas")
    @PreAuthorize("hasRole('SECRETARIA')")
    public List<SituacaoDisciplinaResponse> consultarSituacao(@PathVariable Long id) {
        return service.consultarSituacao(id);
    }

    @PostMapping("/{id}/encerrar-matriculas")
    @PreAuthorize("hasRole('SECRETARIA')")
    public SemestreResponse encerrarMatriculas(@PathVariable Long id) {
        return service.encerrarMatriculas(id);
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize("hasRole('SECRETARIA')")
    public SemestreResponse concluir(@PathVariable Long id) {
        return service.concluir(id);
    }
}
