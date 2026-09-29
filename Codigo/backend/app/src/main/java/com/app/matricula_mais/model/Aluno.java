package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Aluno extends Usuario {

    @Column(unique = true)
    private String matricula;
    private String curso;

    @OneToMany(mappedBy = "aluno")
    private List<Matricula> matriculas;

    @ManyToMany
    @JoinTable(
        name = "aluno_historico_disciplina",
        joinColumns = @JoinColumn(name = "aluno_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    private List<Disciplina> historicoDisciplinas;

    public Aluno() {
        this.matriculas = new ArrayList<>();
        this.historicoDisciplinas = new ArrayList<>();
    }

    public Aluno(Long id, String nome, String login, String senha, String email,
                 String matricula, String curso) {
        super(id, nome, login, senha, email);
        this.matricula = matricula;
        this.curso = curso;
        this.matriculas = new ArrayList<>();
        this.historicoDisciplinas = new ArrayList<>();
    }

    public void realizarMatricula(Matricula novaMatricula) {
        if (novaMatricula != null) {
            this.matriculas.add(novaMatricula);
        }
    }

    public void cancelarMatricula(Matricula matriculaParaCancelar) {
        if (matriculaParaCancelar != null) {
            matriculaParaCancelar.cancelar();
            this.matriculas.remove(matriculaParaCancelar);
        }
    }

    public List<Disciplina> consultarMatriculaAtual() {
        List<Disciplina> disciplinasAtivas = new ArrayList<>();
        for (Matricula matricula : matriculas) {
            if (matricula.isAtiva()) {
                disciplinasAtivas.add(matricula.getDisciplina());
            }
        }
        return disciplinasAtivas;
    }

    public List<Disciplina> consultarDisciplinasMatriculadas() {
        return consultarMatriculaAtual();
    }

    public List<Disciplina> consultarHistoricoDisciplinas() {
        return historicoDisciplinas;
    }

}
