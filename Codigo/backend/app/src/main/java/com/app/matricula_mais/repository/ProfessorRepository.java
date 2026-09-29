package com.app.matricula_mais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.matricula_mais.model.Professor;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}

