package com.app.matricula_mais.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.DisciplinaRequest;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Professor;
import com.app.matricula_mais.repository.CursoRepository;
import com.app.matricula_mais.repository.DisciplinaRepository;
import com.app.matricula_mais.repository.MatriculaRepository;
import com.app.matricula_mais.repository.ProfessorRepository;
import com.app.matricula_mais.repository.SemestreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisciplinaService {
    private final DisciplinaRepository disciplinas;
    private final ProfessorRepository professores;
    private final MatriculaRepository matriculas;
    private final CursoRepository cursos;
    private final SemestreRepository semestres;

    public List<DisciplinaResponse> listar() {
        return disciplinas.findAll().stream().map(DisciplinaResponse::de).toList();
    }

    public DisciplinaResponse buscar(Long id) {
        return DisciplinaResponse.de(buscarEntidade(id));
    }

    public Disciplina buscarEntidade(Long id) {
        return disciplinas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina"));
    }

    public List<Disciplina> buscarTodas(List<Long> ids) {
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new RegraNegocioException("A lista contém disciplinas repetidas.");
        }
        List<Disciplina> encontradas = disciplinas.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            throw new RecursoNaoEncontradoException("Uma ou mais disciplinas");
        }
        return encontradas;
    }

    @Transactional
    public DisciplinaResponse cadastrar(DisciplinaRequest dados) {
        return salvar(new Disciplina(), dados);
    }

    @Transactional
    public DisciplinaResponse atualizar(Long id, DisciplinaRequest dados) {
        return salvar(buscarEntidade(id), dados);
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = buscarEntidade(id);
        if (matriculas.existsByDisciplinaId(id) || cursos.existsByDisciplinasId(id)
                || semestres.existsByDisciplinasOfertadasId(id)) {
            throw new RegraNegocioException("Disciplina vinculada a curso, semestre ou matrícula.");
        }
        disciplinas.delete(disciplina);
    }

    private DisciplinaResponse salvar(Disciplina disciplina, DisciplinaRequest dados) {
        Long id = disciplina.getId();
        if (id == null) {
            id = -1L;
        }
        if (disciplinas.existsByCodigoAndIdNot(dados.codigo(), id)) {
            throw new RegraNegocioException("Código de disciplina já cadastrado.");
        }
        Professor professor = professores.findById(dados.professorId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Professor"));
        disciplina.setCodigo(dados.codigo());
        disciplina.setNome(dados.nome());
        disciplina.setCreditos(dados.creditos());
        disciplina.setProfessorResponsavel(professor);
        return DisciplinaResponse.de(disciplinas.save(disciplina));
    }
}
