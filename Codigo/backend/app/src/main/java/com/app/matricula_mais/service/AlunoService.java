package com.app.matricula_mais.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.app.matricula_mais.dto.Requisicoes.AlunoRequest;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.exception.*;
import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlunoService {
    private final AlunoRepository alunos;
    private final MatriculaRepository matriculas;
    private final CadastroUsuarioService cadastroUsuarios;

    public List<AlunoResponse> listar() {
        return alunos.findAll().stream().map(AlunoResponse::de).toList();
    }

    public AlunoResponse buscar(Long id) {
        return AlunoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public AlunoResponse cadastrar(AlunoRequest dados) {
        return salvar(new Aluno(), dados);
    }

    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest dados) {
        return salvar(buscarEntidade(id), dados);
    }

    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarEntidade(id);
        if (matriculas.existsByAlunoId(id)) {
            throw new RegraNegocioException("Aluno possui matrículas; seu histórico deve ser preservado.");
        }
        alunos.delete(aluno);
    }

    private AlunoResponse salvar(Aluno aluno, AlunoRequest dados) {
        if (alunos.existsByMatriculaAndIdNot(dados.matricula(), aluno.getId() == null ? -1L : aluno.getId())) {
            throw new RegraNegocioException("Número de matrícula já cadastrado.");
        }
        cadastroUsuarios.preencher(aluno, dados.nome(), dados.login(), dados.senha(), dados.email());
        aluno.setMatricula(dados.matricula());
        aluno.setCurso(dados.curso());
        return AlunoResponse.de(alunos.save(aluno));
    }

    private Aluno buscarEntidade(Long id) {
        return alunos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno"));
    }
}
