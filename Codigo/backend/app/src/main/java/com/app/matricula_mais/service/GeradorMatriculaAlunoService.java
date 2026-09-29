package com.app.matricula_mais.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.app.matricula_mais.model.NumeroMatriculaAluno;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.NumeroMatriculaAlunoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeradorMatriculaAlunoService {
    private final NumeroMatriculaAlunoRepository numeros;
    private final AlunoRepository alunos;

    @Transactional(propagation = Propagation.MANDATORY)
    public String gerar() {
        String matricula;
        do {
            // O banco coordena a emissão entre threads e instâncias da aplicação.
            Long sequencial = numeros.saveAndFlush(new NumeroMatriculaAluno()).getId();
            matricula = Long.toString(Math.addExact(1_000_000L, sequencial));
            // Preserva números cadastrados manualmente nas versões anteriores.
        } while (alunos.existsByMatricula(matricula));
        return matricula;
    }
}
