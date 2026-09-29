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
@Table(name = "semestre")
@Getter
@Setter
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private boolean periodoMatriculaAberto;

    @ManyToMany
    @JoinTable(
        name = "semestre_disciplina",
        joinColumns = @JoinColumn(name = "semestre_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
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

}
