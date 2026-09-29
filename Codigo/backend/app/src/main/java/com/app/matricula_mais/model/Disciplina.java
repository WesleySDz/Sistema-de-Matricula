package com.app.matricula_mais.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "disciplina")
@Getter
@Setter
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nome;
    private int creditos;

    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id")
    private Professor professorResponsavel;

    @OneToMany(mappedBy = "disciplina")
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

}
