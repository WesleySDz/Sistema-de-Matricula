package com.app.matricula_mais.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/** Registro permanente de emissão. Não é excluído junto com o aluno. */
@Entity
@Getter
public class NumeroMatriculaAluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
