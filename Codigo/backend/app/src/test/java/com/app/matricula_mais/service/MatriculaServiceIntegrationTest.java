package com.app.matricula_mais.service;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.app.matricula_mais.dto.Requisicoes.*;
import com.app.matricula_mais.dto.Requisicoes.CurriculoRequest;
import com.app.matricula_mais.dto.Requisicoes.MatriculaRequest;
import com.app.matricula_mais.exception.RegraNegocioException;
import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.model.Disciplina;
import com.app.matricula_mais.model.Matricula;
import com.app.matricula_mais.model.Professor;
import com.app.matricula_mais.model.Semestre;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.DisciplinaRepository;
import com.app.matricula_mais.repository.MatriculaRepository;
import com.app.matricula_mais.repository.NotificacaoCobrancaRepository;
import com.app.matricula_mais.repository.ProfessorRepository;
import com.app.matricula_mais.repository.SemestreRepository;

@SpringBootTest
@Transactional
class MatriculaServiceIntegrationTest {
    @Autowired
    MatriculaService service;
    @Autowired
    SemestreService semestreService;
    @Autowired
    ProfessorService professorService;
    @Autowired
    AlunoRepository alunos;
    @Autowired
    ProfessorRepository professores;
    @Autowired
    DisciplinaRepository disciplinas;
    @Autowired
    SemestreRepository semestres;
    @Autowired
    MatriculaRepository matriculas;
    @Autowired
    NotificacaoCobrancaRepository notificacoes;

    private Aluno aluno;
    private Professor professor;
    private Semestre semestre;
    private List<Disciplina> oferta;

    @BeforeEach
    void preparar() {
        aluno = novoAluno("principal");
        professor = professores
                .save(new Professor(null, "Docente", "docente-teste", "hash", "docente@teste.com", "Mestre"));
        oferta = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Disciplina disciplina = new Disciplina(null, "DISC-" + i, "Disciplina " + i, 4);
            disciplina.setProfessorResponsavel(professor);
            oferta.add(disciplinas.save(disciplina));
        }
        semestre = new Semestre(null, "teste/1");
        semestre.gerarCurriculo(oferta);
        semestre = semestres.save(semestre);
    }

    @Test
    void inscreveQuatroObrigatoriasEDuasOptativasENotificaTodas() {
        var resultado = service.realizar(aluno.getId(), pedido(ids(0, 4), ids(4, 6)));
        assertThat(resultado).hasSize(6);
        assertThat(resultado).filteredOn(m -> m.optativa()).hasSize(2);
        assertThat(service.consultarDisciplinas(aluno.getId(), semestre.getId())).hasSize(6);
        var notificacao = notificacoes.findAll().getFirst();
        assertThat(notificacao.getAlunoId()).isEqualTo(aluno.getId());
        assertThat(notificacao.getDisciplinaIds()).containsExactlyInAnyOrderElementsOf(ids(0, 6));
        assertThat(notificacao.getEnviadaEm()).isNull();
    }

    @Test
    void limitesConsideramInscricoesAnterioresENaoGeramNotificacaoEmFalha() {
        service.realizar(aluno.getId(), pedido(ids(0, 4), ids(4, 6)));
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(ids(6, 7), List.of())))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(List.of(), ids(6, 7))))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(matriculas.findByAlunoIdAndSemestreAndAtivaTrue(aluno.getId(), semestre.getNome())).hasSize(6);
        assertThat(notificacoes.count()).isEqualTo(1);
    }

    @Test
    void rejeitaDisciplinaRepetidaMesmoEntreTiposEInscricaoVazia() {
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(ids(0, 1), ids(0, 1))))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(List.of(), List.of())))
                .isInstanceOf(RegraNegocioException.class);
        service.realizar(aluno.getId(), pedido(ids(0, 1), List.of()));
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(ids(0, 1), List.of())))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaDisciplinaForaDoCurriculoSemSalvarInscricaoParcial() {
        Disciplina fora = disciplinas.save(new Disciplina(null, "FORA", "Fora da oferta", 4));
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(
                List.of(oferta.getFirst().getId(), fora.getId()), List.of())))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(matriculas.count()).isZero();
        assertThat(notificacoes.count()).isZero();
    }

    @Test
    void capacidadeDeSessentaEContadaPorSemestre() {
        Disciplina disciplina = oferta.getFirst();
        for (int i = 0; i < 60; i++) {
            matriculas.save(new Matricula(null, novoAluno("capacidade-" + i), disciplina, semestre.getNome()));
        }
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(ids(0, 1), List.of())))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("60");
        Semestre outro = new Semestre(null, "teste/2");
        outro.gerarCurriculo(List.of(disciplina));
        semestres.save(outro);
        assertThat(service.realizar(aluno.getId(), new MatriculaRequest(outro.getId(), ids(0, 1), List.of())))
                .hasSize(1);
    }

    @Test
    void cancelamentoLiberaEscolhaESoPodeSerFeitoPeloDonoNoPeriodoAberto() {
        var inscricao = service.realizar(aluno.getId(), pedido(ids(0, 4), List.of()));
        Long id = inscricao.getFirst().id();
        assertThatThrownBy(() -> service.cancelar(novoAluno("outro").getId(), id))
                .isInstanceOf(AccessDeniedException.class);
        service.cancelar(aluno.getId(), id);
        assertThat(service.consultarAtual(aluno.getId(), semestre.getId())).hasSize(3);
        assertThat(service.realizar(aluno.getId(), pedido(ids(4, 5), List.of()))).hasSize(1);
        semestre.setPeriodoMatriculaAberto(false);
        assertThatThrownBy(() -> service.cancelar(aluno.getId(), inscricao.get(1).id()))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.realizar(aluno.getId(), pedido(List.of(), ids(5, 6))))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void encerramentoCancelaSomenteOfertasComMenosDeTresNoSemestreEscolhido() {
        for (int i = 0; i < 3; i++) {
            Aluno inscrito = i == 0 ? aluno : novoAluno("minimo-" + i);
            matriculas.save(new Matricula(null, inscrito, oferta.get(0), semestre.getNome()));
            if (i < 2) {
                matriculas.save(new Matricula(null, inscrito, oferta.get(1), semestre.getNome()));
            }
        }
        Semestre outro = new Semestre(null, "teste/2");
        outro.gerarCurriculo(oferta);
        semestres.save(outro);
        Matricula emOutroSemestre = matriculas.save(new Matricula(null, aluno, oferta.get(1), outro.getNome()));
        semestreService.encerrarMatriculas(semestre.getId());
        assertThat(semestre.isPeriodoMatriculaAberto()).isFalse();
        assertThat(semestre.getDisciplinasCanceladas()).contains(oferta.get(1)).doesNotContain(oferta.get(0));
        assertThat(service.consultarAtual(aluno.getId(), semestre.getId())).hasSize(1);
        assertThat(emOutroSemestre.isAtiva()).isTrue();
        assertThat(oferta.get(1).isAtiva()).isTrue();
        assertThat(semestreService.consultarSituacao(semestre.getId()).get(1).situacao()).isEqualTo("CANCELADA");
        assertThat(service.consultarHistorico(aluno.getId())).isEmpty();
        semestreService.concluir(semestre.getId());
        assertThat(service.consultarHistorico(aluno.getId())).hasSize(1)
                .allMatch(m -> m.semestre().equals(semestre.getNome()));
    }

    @Test
    void curriculoNaoPodeRemoverOfertaComMatriculas() {
        service.realizar(aluno.getId(), pedido(ids(0, 1), List.of()));
        assertThatThrownBy(() -> semestreService.gerarCurriculo(semestre.getId(), new CurriculoRequest(ids(1, 3))))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void professorConsultaSomenteAlunosDeSuasDisciplinas() {
        service.realizar(aluno.getId(), pedido(ids(0, 1), List.of()));
        assertThat(professorService.consultarAlunos(professor.getId(), oferta.getFirst().getId(), semestre.getId()))
                .hasSize(1).allMatch(a -> a.id().equals(aluno.getId()));
        assertThatThrownBy(() -> professorService.consultarAlunos(-1L, oferta.getFirst().getId(), semestre.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Aluno novoAluno(String sufixo) {
        return alunos
                .save(new Aluno(null, "Aluno", "aluno-" + sufixo, "hash", "aluno@teste.com", "MAT-" + sufixo, "ADS"));
    }

    private List<Long> ids(int inicio, int fim) {
        return oferta.subList(inicio, fim).stream().map(Disciplina::getId).toList();
    }

    private MatriculaRequest pedido(List<Long> obrigatorias, List<Long> optativas) {
        return new MatriculaRequest(semestre.getId(), obrigatorias, optativas);
    }
}
