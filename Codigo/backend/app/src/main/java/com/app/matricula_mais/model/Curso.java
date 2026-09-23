package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

public class Curso {

    private Long id;
    private String nome;
    private int quantidadeCreditos;
    private List<Disciplina> disciplinas;

    public Curso() {
        this.disciplinas = new ArrayList<>();
    }

    public Curso(Long id, String nome, int quantidadeCreditos) {
        this.id = id;
        this.nome = nome;
        this.quantidadeCreditos = quantidadeCreditos;
        this.disciplinas = new ArrayList<>();
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        if (disciplina != null && !disciplinas.contains(disciplina)) {
            disciplinas.add(disciplina);
        }
    }

    public void removerDisciplina(Disciplina disciplina) {
        if (disciplina != null) {
            disciplinas.remove(disciplina);
        }
    }

    public boolean validarCreditos() {
        return quantidadeCreditos > 0;
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

    public int getQuantidadeCreditos() {
        return quantidadeCreditos;
    }

    public void setQuantidadeCreditos(int quantidadeCreditos) {
        this.quantidadeCreditos = quantidadeCreditos;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(List<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }
}
