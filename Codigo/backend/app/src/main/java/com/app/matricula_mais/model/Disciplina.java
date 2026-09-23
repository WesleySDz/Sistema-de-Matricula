package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {

    private Long id;
    private String codigo;
    private String nome;
    private int creditos;
    private Professor professorResponsavel;
    private List<Matricula> matriculas;
    private boolean ativa;

    public Disciplina() {
        this.matriculas = new ArrayList<>();
        this.ativa = true;
    }

    public Disciplina(Long id, String codigo, String nome, int creditos) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.creditos = creditos;
        this.matriculas = new ArrayList<>();
        this.ativa = true;
    }

    public void adicionarMatricula(Matricula matricula) {
        if (matricula != null && !matriculas.contains(matricula)) {
            matriculas.add(matricula);
        }
    }

    public void removerMatricula(Matricula matricula) {
        if (matricula != null) {
            matriculas.remove(matricula);
        }
    }

    public int quantidadeMatriculadosAtivos() {
        int total = 0;
        for (Matricula matricula : matriculas) {
            if (matricula.isAtiva()) {
                total++;
            }
        }
        return total;
    }

    public boolean podeReceberMatricula() {
        return ativa && quantidadeMatriculadosAtivos() < 60;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public Professor getProfessorResponsavel() {
        return professorResponsavel;
    }

    public void setProfessorResponsavel(Professor professorResponsavel) {
        this.professorResponsavel = professorResponsavel;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
