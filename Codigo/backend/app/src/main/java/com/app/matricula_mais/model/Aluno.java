package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Setter;

@Entity
@Setter
public class Aluno extends Usuario {

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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }

    public List<Disciplina> getHistoricoDisciplinas() {
        return historicoDisciplinas;
    }

    public void setHistoricoDisciplinas(List<Disciplina> historicoDisciplinas) {
        this.historicoDisciplinas = historicoDisciplinas;
    }
}
