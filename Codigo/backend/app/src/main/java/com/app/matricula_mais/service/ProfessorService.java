package com.app.matricula_mais.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.ProfessorRequest;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.ProfessorResponse;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Professor;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.DisciplinaRepository;
import com.app.matricula_mais.repository.MatriculaRepository;
import com.app.matricula_mais.repository.ProfessorRepository;
import com.app.matricula_mais.repository.SemestreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorService {
    private final ProfessorRepository professores;
    private final DisciplinaRepository disciplinas;
    private final MatriculaRepository matriculas;
    private final SemestreRepository semestres;
    private final CadastroUsuarioService cadastroUsuarios;

    public List<ProfessorResponse> listar() {
        return professores.findAll().stream().map(ProfessorResponse::de).toList();
    }

    public ProfessorResponse buscar(Long id) {
        return ProfessorResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ProfessorResponse cadastrar(ProfessorRequest dados) {
        return salvar(new Professor(), dados);
    }

    @Transactional
    public ProfessorResponse atualizar(Long id, ProfessorRequest dados) {
        return salvar(buscarEntidade(id), dados);
    }

    @Transactional
    public void excluir(Long id) {
        Professor professor = buscarEntidade(id);
        if (disciplinas.existsByProfessorResponsavelId(id)) {
            throw new RegraNegocioException("Professor ainda é responsável por disciplinas.");
        }
        professores.delete(professor);
    }

    public List<DisciplinaResponse> consultarDisciplinas(Long professorId) {
        buscarEntidade(professorId);
        return disciplinas.findByProfessorResponsavelId(professorId).stream().map(DisciplinaResponse::de).toList();
    }

    public List<AlunoResponse> consultarAlunos(Long professorId, Long disciplinaId, Long semestreId) {
        Disciplina disciplina = disciplinas.findById(disciplinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina"));
        if (disciplina.getProfessorResponsavel() == null
                || !professorId.equals(disciplina.getProfessorResponsavel().getId())) {
            throw new AccessDeniedException("A disciplina não pertence ao professor autenticado.");
        }
        Semestre semestre = semestres.findById(semestreId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Semestre"));
        return matriculas.findByDisciplinaIdAndSemestreAndAtivaTrue(disciplinaId, semestre.getNome()).stream()
                .map(matricula -> AlunoResponse.de(matricula.getAluno())).toList();
    }

    private ProfessorResponse salvar(Professor professor, ProfessorRequest dados) {
        cadastroUsuarios.preencher(professor, dados.nome(), dados.login(), dados.senha(), dados.email());
        professor.setTitulacao(dados.titulacao());
        return ProfessorResponse.de(professores.save(professor));
    }

    private Professor buscarEntidade(Long id) {
        return professores.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Professor"));
    }
}
