package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Professor extends Usuario {

    private String titulacao;

    @OneToMany(mappedBy = "professorResponsavel")
    private List<Disciplina> disciplinasResponsaveis;

    public Professor() {
        this.disciplinasResponsaveis = new ArrayList<>();
    }

    public Professor(Long id, String nome, String login, String senha, String email, String titulacao) {
        super(id, nome, login, senha, email);
        this.titulacao = titulacao;
        this.disciplinasResponsaveis = new ArrayList<>();
    }

    public void associarDisciplina(Disciplina disciplina) {
        if (disciplina != null && !disciplinasResponsaveis.contains(disciplina)) {
            disciplinasResponsaveis.add(disciplina);
            disciplina.setProfessorResponsavel(this);
        }
    }

    public List<Aluno> consultarAlunosMatriculados(Disciplina disciplina) {
        List<Aluno> alunos = new ArrayList<>();
        if (disciplina != null) {
            for (Matricula matricula : disciplina.getMatriculas()) {
                if (matricula.isAtiva()) {
                    alunos.add(matricula.getAluno());
                }
            }
        }
        return alunos;
    }

    public boolean ehResponsavelPor(Disciplina disciplina) {
        return disciplina != null && disciplinasResponsaveis.contains(disciplina);
    }

}
