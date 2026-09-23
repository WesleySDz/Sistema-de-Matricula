package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {

    private String matricula;
    private String curso;
    private List<Matricula> matriculas;
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
