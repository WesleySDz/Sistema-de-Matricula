package com.app.matricula_mais.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "matricula")
@Getter
@Setter
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;
    private LocalDate dataMatricula;
    private boolean ativa;
    private boolean optativa;
    private String semestre;

    public Matricula() {
        this.ativa = true;
        this.dataMatricula = LocalDate.now();
    }

    public Matricula(Long id, Aluno aluno, Disciplina disciplina, String semestre) {
        this.id = id;
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.semestre = semestre;
        this.dataMatricula = LocalDate.now();
        this.ativa = true;
    }

    public void cancelar() {
        this.ativa = false;
    }

    public void ativar() {
        this.ativa = true;
    }

}
