package com.app.matricula_mais.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.app.matricula_mais.dto.Requisicoes.CursoRequest;
import com.app.matricula_mais.dto.Respostas.CursoResponse;
import com.app.matricula_mais.exception.RecursoNaoEncontradoException;
import com.app.matricula_mais.model.Curso;
import com.app.matricula_mais.repository.CursoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CursoService {
    private final CursoRepository cursos;
    private final DisciplinaService disciplinas;

    public List<CursoResponse> listar() {
        return cursos.findAll().stream().map(CursoResponse::de).toList();
    }

    public CursoResponse buscar(Long id) {
        return CursoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CursoResponse cadastrar(CursoRequest dados) {
        return salvar(new Curso(), dados);
    }

    @Transactional
    public CursoResponse atualizar(Long id, CursoRequest dados) {
        return salvar(buscarEntidade(id), dados);
    }

    @Transactional
    public void excluir(Long id) {
        cursos.delete(buscarEntidade(id));
    }

    private CursoResponse salvar(Curso curso, CursoRequest dados) {
        curso.setNome(dados.nome());
        curso.setQuantidadeCreditos(dados.quantidadeCreditos());
        curso.setDisciplinas(new ArrayList<>(disciplinas.buscarTodas(dados.disciplinaIds())));
        return CursoResponse.de(cursos.save(curso));
    }

    private Curso buscarEntidade(Long id) {
        return cursos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Curso"));
    }
}
