package com.app.matricula_mais.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.model.Matricula;
import com.app.matricula_mais.model.NotificacaoCobranca;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.NotificacaoCobrancaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CobrancaService {
    private final NotificacaoCobrancaRepository notificacoes;

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(Long alunoId, Semestre semestre, List<Matricula> matriculas) {
        NotificacaoCobranca notificacao = new NotificacaoCobranca();
        notificacao.setAlunoId(alunoId);
        notificacao.setSemestreId(semestre.getId());
        notificacao.setSemestre(semestre.getNome());
        notificacao.setDisciplinaIds(matriculas.stream().filter(Matricula::isAtiva)
                .map(matricula -> matricula.getDisciplina().getId()).toList());
        notificacoes.save(notificacao);
    }
}
