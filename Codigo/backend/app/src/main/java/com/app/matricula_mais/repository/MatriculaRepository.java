package com.app.matricula_mais.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.app.matricula_mais.model.Matricula;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    boolean existsByAlunoId(Long alunoId);
    boolean existsByDisciplinaId(Long disciplinaId);
    List<Matricula> findByAlunoIdAndSemestreAndAtivaTrue(Long alunoId, String semestre);
    List<Matricula> findByDisciplinaIdAndSemestreAndAtivaTrue(Long disciplinaId, String semestre);
    List<Matricula> findBySemestreAndAtivaTrue(String semestre);
    long countByDisciplinaIdAndSemestreAndAtivaTrue(Long disciplinaId, String semestre);

    @Query("select m from Matricula m, Semestre s where m.aluno.id = :alunoId "
        + "and m.semestre = s.nome and s.concluido = true and m.ativa = true")
    List<Matricula> buscarHistorico(@Param("alunoId") Long alunoId);
}

