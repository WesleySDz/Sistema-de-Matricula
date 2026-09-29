package com.app.matricula_mais.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.app.matricula_mais.dto.Requisicoes.MatriculaRequest;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Matricula;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.DisciplinaRepository;
import com.app.matricula_mais.repository.MatriculaRepository;
import com.app.matricula_mais.repository.SemestreRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:concorrencia-tests;DB_CLOSE_DELAY=-1")
class ConcorrenciaMatriculaIntegrationTest {
    @Autowired
    MatriculaService service;
    @Autowired
    AlunoRepository alunos;
    @Autowired
    DisciplinaRepository disciplinas;
    @Autowired
    SemestreRepository semestres;
    @Autowired
    MatriculaRepository matriculas;
    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void duasInscricoesConcorrentesDisputamUmaUnicaVaga() throws Exception {
        Long[] ids = new TransactionTemplate(transactionManager).execute(status -> {
            Disciplina disciplina = disciplinas.save(new Disciplina(null, "CONCORRENTE", "Disciplina", 4));
            Semestre semestre = new Semestre(null, "concorrencia/1");
            semestre.gerarCurriculo(List.of(disciplina));
            semestres.save(semestre);
            for (int i = 0; i < 59; i++) {
                matriculas.save(new Matricula(null, novoAluno("inscrito-" + i), disciplina, semestre.getNome()));
            }
            return new Long[] { semestre.getId(), disciplina.getId(), novoAluno("candidato-a").getId(),
                    novoAluno("candidato-b").getId() };
        });
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primeira = executor.submit(inscricao(ids[2], ids[0], ids[1], inicio));
            var segunda = executor.submit(inscricao(ids[3], ids[0], ids[1], inicio));
            inicio.countDown();
            assertThat(List.of(primeira.get(10, TimeUnit.SECONDS), segunda.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(matriculas.countByDisciplinaIdAndSemestreAndAtivaTrue(ids[1], "concorrencia/1")).isEqualTo(60);
    }

    private Callable<Boolean> inscricao(Long alunoId, Long semestreId, Long disciplinaId, CountDownLatch inicio) {
        return () -> {
            inicio.await(5, TimeUnit.SECONDS);
            try {
                service.realizar(alunoId, new MatriculaRequest(semestreId, List.of(disciplinaId), List.of()));
                return true;
            } catch (RegraNegocioException ex) {
                assertThat(ex.getMessage()).contains("60");
                return false;
            }
        };
    }

    private Aluno novoAluno(String login) {
        return alunos.save(new Aluno(null, "Aluno", login, "hash", "aluno@teste.com", login, "ADS"));
    }
}
