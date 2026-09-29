package com.app.matricula_mais.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.CurriculoRequest;
import com.app.matricula_mais.dto.Requisicoes.SemestreRequest;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.SemestreResponse;
import com.app.matricula_mais.dto.Respostas.SituacaoDisciplinaResponse;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Matricula;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.MatriculaRepository;
import com.app.matricula_mais.repository.SemestreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemestreService {
    private final SemestreRepository semestres;
    private final MatriculaRepository matriculas;
    private final DisciplinaService disciplinas;

    public List<SemestreResponse> listar() {
        return semestres.findAll().stream().map(SemestreResponse::de).toList();
    }

    public SemestreResponse buscar(Long id) {
        return SemestreResponse.de(buscarEntidade(id));
    }

    public Semestre buscarEntidade(Long id) {
        return semestres.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Semestre"));
    }

    // Todas as alterações na oferta e nas matrículas usam o mesmo bloqueio no
    // banco.
    @Transactional
    public Semestre bloquear(Long id) {
        return semestres.buscarComBloqueio(id).orElseThrow(() -> new RecursoNaoEncontradoException("Semestre"));
    }

    @Transactional
    public Semestre bloquearPorNome(String nome) {
        return semestres.buscarComBloqueioPorNome(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Semestre"));
    }

    public void validarPeriodoAberto(Semestre semestre) {
        if (!semestre.isPeriodoMatriculaAberto() || semestre.isConcluido()) {
            throw new RegraNegocioException("O período de matrícula está encerrado.");
        }
    }

    @Transactional
    public SemestreResponse cadastrar(SemestreRequest dados) {
        if (semestres.existsByNome(dados.nome())) {
            throw new RegraNegocioException("Semestre já cadastrado.");
        }
        return SemestreResponse.de(semestres.save(new Semestre(null, dados.nome())));
    }

    @Transactional
    public SemestreResponse gerarCurriculo(Long id, CurriculoRequest dados) {
        Semestre semestre = bloquear(id);
        validarPeriodoAberto(semestre);
        List<Disciplina> oferta = disciplinas.buscarTodas(dados.disciplinaIds());
        Set<Long> ids = new HashSet<>(dados.disciplinaIds());
        boolean removeMatriculada = matriculas.findBySemestreAndAtivaTrue(semestre.getNome()).stream()
                .anyMatch(matricula -> !ids.contains(matricula.getDisciplina().getId()));
        if (removeMatriculada) {
            throw new RegraNegocioException(
                    "Não é possível remover do currículo uma disciplina com matrículas ativas.");
        }
        semestre.gerarCurriculo(oferta);
        return SemestreResponse.de(semestre);
    }

    public List<SituacaoDisciplinaResponse> consultarSituacao(Long id) {
        Semestre semestre = buscarEntidade(id);
        Set<Long> canceladas = new HashSet<>(
                semestre.getDisciplinasCanceladas().stream().map(Disciplina::getId).toList());
        return semestre.getDisciplinasOfertadas().stream().map(disciplina -> new SituacaoDisciplinaResponse(
                DisciplinaResponse.de(disciplina), canceladas.contains(disciplina.getId()) ? "CANCELADA" : "ATIVA",
                matriculas.countByDisciplinaIdAndSemestreAndAtivaTrue(disciplina.getId(), semestre.getNome())))
                .toList();
    }

    @Transactional
    public SemestreResponse encerrarMatriculas(Long id) {
        Semestre semestre = bloquear(id);
        if (!semestre.isPeriodoMatriculaAberto()) {
            return SemestreResponse.de(semestre);
        }
        for (Disciplina disciplina : semestre.getDisciplinasOfertadas()) {
            List<Matricula> inscritas = matriculas.findByDisciplinaIdAndSemestreAndAtivaTrue(
                    disciplina.getId(), semestre.getNome());
            if (inscritas.size() < 3) {
                semestre.getDisciplinasCanceladas().add(disciplina);
                inscritas.forEach(Matricula::cancelar);
            }
        }
        semestre.setPeriodoMatriculaAberto(false);
        return SemestreResponse.de(semestre);
    }

    @Transactional
    public SemestreResponse concluir(Long id) {
        Semestre semestre = bloquear(id);
        if (semestre.isPeriodoMatriculaAberto()) {
            throw new RegraNegocioException("Encerre as matrículas antes de concluir o semestre letivo.");
        }
        semestre.setConcluido(true);
        return SemestreResponse.de(semestre);
    }
}
