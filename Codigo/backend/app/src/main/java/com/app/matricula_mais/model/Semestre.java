package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

public class Semestre {

    private Long id;
    private String nome;
    private boolean periodoMatriculaAberto;
    private List<Disciplina> disciplinasOfertadas;

    public Semestre() {
        this.disciplinasOfertadas = new ArrayList<>();
        this.periodoMatriculaAberto = true;
    }

    public Semestre(Long id, String nome) {
        this.id = id;
        this.nome = nome;
        this.disciplinasOfertadas = new ArrayList<>();
        this.periodoMatriculaAberto = true;
    }

    public void gerarCurriculo(List<Disciplina> disciplinas) {
        if (disciplinas != null) {
            this.disciplinasOfertadas = new ArrayList<>(disciplinas);
        }
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        if (disciplina != null && !disciplinasOfertadas.contains(disciplina)) {
            disciplinasOfertadas.add(disciplina);
        }
    }

    public void removerDisciplina(Disciplina disciplina) {
        if (disciplina != null) {
            disciplinasOfertadas.remove(disciplina);
        }
    }

    public boolean aplicarRegraMinimaAlunos() {
        for (Disciplina disciplina : disciplinasOfertadas) {
            if (disciplina.quantidadeMatriculadosAtivos() < 3) {
                disciplina.setAtiva(false);
            }
        }
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isPeriodoMatriculaAberto() {
        return periodoMatriculaAberto;
    }

    public void setPeriodoMatriculaAberto(boolean periodoMatriculaAberto) {
        this.periodoMatriculaAberto = periodoMatriculaAberto;
    }

    public List<Disciplina> getDisciplinasOfertadas() {
        return disciplinasOfertadas;
    }

    public void setDisciplinasOfertadas(List<Disciplina> disciplinasOfertadas) {
        this.disciplinasOfertadas = disciplinasOfertadas;
    }
}
