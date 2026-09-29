package com.app.matricula_mais.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.app.matricula_mais.model.Semestre;

public interface SemestreRepository extends JpaRepository<Semestre, Long> {
    Optional<Semestre> findByNome(String nome);
    boolean existsByNome(String nome);
    boolean existsByDisciplinasOfertadasId(Long disciplinaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Semestre s where s.id = :id")
    Optional<Semestre> buscarComBloqueio(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Semestre s where s.nome = :nome")
    Optional<Semestre> buscarComBloqueioPorNome(@Param("nome") String nome);
}
