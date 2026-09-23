package com.app.matricula_mais.model;

import java.util.List;

public class FuncionarioSecretaria extends Usuario {

    public FuncionarioSecretaria() {
    }

    public FuncionarioSecretaria(Long id, String nome, String login, String senha, String email) {
        super(id, nome, login, senha, email);
    }

    public void cadastrarAluno(Aluno aluno) {
        if (aluno != null) {
            // stub para serviço de persistência
        }
    }

    public void atualizarAluno(Aluno aluno) {
        if (aluno != null) {
            // stub para serviço de persistência
        }
    }

    public void excluirAluno(Long idAluno) {
        if (idAluno != null) {
            // stub para serviço de persistência
        }
    }

    public void gerarCurriculoSemestre(Semestre semestre, List<Disciplina> disciplinas) {
        if (semestre != null && disciplinas != null) {
            semestre.gerarCurriculo(disciplinas);
        }
    }

    public void consultarMatriculasPorDisciplina(Disciplina disciplina) {
        if (disciplina != null) {
            disciplina.getMatriculas();
        }
    }
}
