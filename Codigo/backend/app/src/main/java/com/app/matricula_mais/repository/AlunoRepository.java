package com.app.matricula_mais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.matricula_mais.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}

