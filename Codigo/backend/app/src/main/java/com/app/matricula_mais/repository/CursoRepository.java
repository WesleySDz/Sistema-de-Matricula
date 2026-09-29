package com.app.matricula_mais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.matricula_mais.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    boolean existsByDisciplinasId(Long disciplinaId);
}

