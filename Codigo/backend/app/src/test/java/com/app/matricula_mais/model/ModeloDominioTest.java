package com.app.matricula_mais.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ModeloDominioTest {

    @Test
    void deveCriarModeloBasicoDeDominio() {
        Aluno aluno = new Aluno(1L, "Ana", "ana", "123", "ana@email.com", "2024-001", "ADS");
        Professor professor = new Professor(2L, "Bruno", "bruno", "456", "bruno@email.com", "Mestre");
        Disciplina disciplina = new Disciplina(10L, "MAT101", "Matemática", 4);
        professor.associarDisciplina(disciplina);

        Matricula matricula = new Matricula(20L, aluno, disciplina, "2024/2");
        aluno.realizarMatricula(matricula);
        disciplina.adicionarMatricula(matricula);

        assertEquals("Ana", aluno.getNome());
        assertEquals("MAT101", disciplina.getCodigo());
        assertTrue(matricula.isAtiva());
        assertEquals(professor, disciplina.getProfessorResponsavel());
        assertEquals(1, aluno.consultarDisciplinasMatriculadas().size());

        matricula.cancelar();
        assertFalse(matricula.isAtiva());
    }
}
