package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "curso")
@Getter
@Setter
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private int quantidadeCreditos;

    @ManyToMany
    @JoinTable(
        name = "curso_disciplina",
        joinColumns = @JoinColumn(name = "curso_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
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

}
