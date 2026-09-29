package com.app.matricula_mais.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.MatriculaRequest;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.MatriculaResponse;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Matricula;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.MatriculaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatriculaService {
    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final SemestreService semestres;
    private final CobrancaService cobrancas;

    @Transactional
    public List<MatriculaResponse> realizar(Long alunoId, MatriculaRequest dados) {
        Semestre semestre = semestres.bloquear(dados.semestreId());
        semestres.validarPeriodoAberto(semestre);
        Aluno aluno = alunos.findById(alunoId).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno"));
        List<Long> selecionadas = new ArrayList<>(dados.obrigatorias());
        selecionadas.addAll(dados.optativas());
        if (selecionadas.isEmpty() || new HashSet<>(selecionadas).size() != selecionadas.size()) {
            throw new RegraNegocioException("Selecione ao menos uma disciplina, sem repetições.");
        }
        List<Matricula> atuais = matriculas.findByAlunoIdAndSemestreAndAtivaTrue(alunoId, semestre.getNome());
        long obrigatorias = atuais.stream().filter(matricula -> !matricula.isOptativa()).count();
        long optativas = atuais.stream().filter(Matricula::isOptativa).count();
        if (obrigatorias + dados.obrigatorias().size() > 4 || optativas + dados.optativas().size() > 2) {
            throw new RegraNegocioException(
                    "O limite por semestre é de quatro disciplinas obrigatórias e duas optativas.");
        }
        Set<Long> inscritas = new HashSet<>(atuais.stream().map(m -> m.getDisciplina().getId()).toList());
        List<Matricula> novas = new ArrayList<>();
        for (Long disciplinaId : selecionadas) {
            Disciplina disciplina = semestre.getDisciplinasOfertadas().stream()
                    .filter(oferta -> oferta.getId().equals(disciplinaId)).findFirst()
                    .orElseThrow(
                            () -> new RegraNegocioException("Disciplina não ofertada no semestre: " + disciplinaId));
            if (semestre.getDisciplinasCanceladas().contains(disciplina)) {
                throw new RegraNegocioException("A oferta da disciplina está cancelada.");
            }
            if (inscritas.contains(disciplinaId)) {
                throw new RegraNegocioException("O aluno já está matriculado na disciplina: " + disciplinaId);
            }
            if (matriculas.countByDisciplinaIdAndSemestreAndAtivaTrue(disciplinaId, semestre.getNome()) >= 60) {
                throw new RegraNegocioException("A disciplina atingiu o limite de 60 alunos: " + disciplinaId);
            }
            Matricula matricula = new Matricula(null, aluno, disciplina, semestre.getNome());
            matricula.setOptativa(dados.optativas().contains(disciplinaId));
            novas.add(matricula);
        }
        matriculas.saveAll(novas);
        atuais.addAll(novas);
        // A notificação só se torna visível ao integrador depois do commit de toda a
        // inscrição.
        cobrancas.registrar(alunoId, semestre, atuais);
        return novas.stream().map(MatriculaResponse::de).toList();
    }

    @Transactional
    public void cancelar(Long alunoId, Long matriculaId) {
        Matricula referencia = buscarEntidade(matriculaId);
        if (!referencia.getAluno().getId().equals(alunoId)) {
            throw new AccessDeniedException("A matrícula não pertence ao aluno autenticado.");
        }
        Semestre semestre = semestres.bloquearPorNome(referencia.getSemestre());
        semestres.validarPeriodoAberto(semestre);
        // Consulta após o bloqueio para acompanhar alterações concorrentes na mesma
        // oferta.
        matriculas.findByAlunoIdAndSemestreAndAtivaTrue(alunoId, semestre.getNome()).stream()
                .filter(matricula -> matricula.getId().equals(matriculaId)).forEach(Matricula::cancelar);
    }

    public List<MatriculaResponse> consultarAtual(Long alunoId, Long semestreId) {
        Semestre semestre = semestres.buscarEntidade(semestreId);
        return matriculas.findByAlunoIdAndSemestreAndAtivaTrue(alunoId, semestre.getNome()).stream()
                .map(MatriculaResponse::de).toList();
    }

    public List<DisciplinaResponse> consultarDisciplinas(Long alunoId, Long semestreId) {
        return consultarAtual(alunoId, semestreId).stream().map(MatriculaResponse::disciplina).toList();
    }

    public List<MatriculaResponse> consultarHistorico(Long alunoId) {
        return matriculas.buscarHistorico(alunoId).stream().map(MatriculaResponse::de).toList();
    }

    public List<MatriculaResponse> consultarPorDisciplina(Long disciplinaId, Long semestreId) {
        Semestre semestre = semestres.buscarEntidade(semestreId);
        if (semestre.getDisciplinasOfertadas().stream().noneMatch(d -> d.getId().equals(disciplinaId))) {
            throw new RecursoNaoEncontradoException("Oferta da disciplina no semestre");
        }
        return matriculas.findByDisciplinaIdAndSemestreAndAtivaTrue(disciplinaId, semestre.getNome()).stream()
                .map(MatriculaResponse::de).toList();
    }

    private Matricula buscarEntidade(Long id) {
        return matriculas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula"));
    }
}
