package com.app.matricula_mais.repository;

import org.springframework.data.repository.Repository;
import com.app.matricula_mais.model.NumeroMatriculaAluno;

// A aplicação pode emitir números, mas não apagar ou reiniciar o registro.
public interface NumeroMatriculaAlunoRepository extends Repository<NumeroMatriculaAluno, Long> {
    NumeroMatriculaAluno saveAndFlush(NumeroMatriculaAluno numero);
}
