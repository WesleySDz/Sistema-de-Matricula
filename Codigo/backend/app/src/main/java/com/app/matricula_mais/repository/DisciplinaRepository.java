package com.app.matricula_mais.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.app.matricula_mais.model.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    boolean existsByProfessorResponsavelId(Long professorId);
    List<Disciplina> findByProfessorResponsavelId(Long professorId);
}

