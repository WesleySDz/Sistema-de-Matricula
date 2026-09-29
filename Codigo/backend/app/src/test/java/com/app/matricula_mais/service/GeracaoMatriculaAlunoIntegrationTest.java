package com.app.matricula_mais.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.app.matricula_mais.dto.Requisicoes.AlunoRequest;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.UsuarioRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:geracao-matriculas-tests;DB_CLOSE_DELAY=-1")
class GeracaoMatriculaAlunoIntegrationTest {
    @Autowired AlunoService service;
    @Autowired AlunoRepository alunos;
    @Autowired UsuarioRepository usuarios;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void emiteNumerosConsecutivosPreservaNaEdicaoENaoReutilizaAposExcluir() {
        AlunoResponse primeiro = service.cadastrar(dados());
        AlunoResponse segundo = service.cadastrar(dados());
        assertThat(Long.parseLong(primeiro.matricula())).isGreaterThanOrEqualTo(1_000_001L);
        assertThat(Long.parseLong(segundo.matricula())).isEqualTo(Long.parseLong(primeiro.matricula()) + 1);
        assertThat(service.atualizar(primeiro.id(), dados()).matricula()).isEqualTo(primeiro.matricula());
        assertThat(alunos.findById(primeiro.id()).orElseThrow().getMatricula()).isEqualTo(primeiro.matricula());
        assertThatThrownBy(() -> alunos.findById(primeiro.id()).orElseThrow().atribuirMatricula("9999999"))
            .isInstanceOf(IllegalStateException.class);
        service.excluir(segundo.id());
        AlunoResponse terceiro = service.cadastrar(dados());
        assertThat(Long.parseLong(terceiro.matricula())).isGreaterThan(Long.parseLong(segundo.matricula()));
    }

    @Test
    void pulaNumeroJaUsadoPorCadastroLegadoSemAlteraLo() {
        AlunoResponse base = service.cadastrar(dados());
        String numeroLegado = Long.toString(Long.parseLong(base.matricula()) + 1);
        Aluno legado = alunos.saveAndFlush(new Aluno(null, "Legado", UUID.randomUUID().toString(), "hash",
            "legado@teste.com", numeroLegado, "ADS"));
        AlunoResponse novo = service.cadastrar(dados());
        assertThat(novo.matricula()).isEqualTo(Long.toString(Long.parseLong(numeroLegado) + 1));
        assertThat(alunos.findById(legado.getId()).orElseThrow().getMatricula()).isEqualTo(numeroLegado);
    }

    @Test
    void cadastrosSimultaneosRecebemNumerosDistintos() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<AlunoResponse>> tarefas = new ArrayList<>();
        List<String> numeros = new ArrayList<>();
        try (var executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 8; i++) {
                tarefas.add(executor.submit(() -> {
                    inicio.await(5, TimeUnit.SECONDS);
                    return service.cadastrar(dados());
                }));
            }
            inicio.countDown();
            for (var tarefa : tarefas) {
                AlunoResponse aluno = tarefa.get(15, TimeUnit.SECONDS);
                numeros.add(aluno.matricula());
                assertThat(alunos.findById(aluno.id()).orElseThrow().getMatricula()).isEqualTo(aluno.matricula());
            }
        }
        assertThat(numeros).hasSize(8).doesNotHaveDuplicates().allMatch(numero -> numero.matches("[0-9]{7,}"));
    }

    @Test
    void falhaNaTransacaoNaoDeixaCadastroParcial() {
        AlunoRequest dados = dados();
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).execute(status -> {
            service.cadastrar(dados);
            throw new IllegalStateException("Falha simulada antes do commit");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(usuarios.findByLogin(dados.login())).isEmpty();
        AlunoResponse concluido = service.cadastrar(dados);
        assertThat(alunos.findById(concluido.id()).orElseThrow().getMatricula()).isEqualTo(concluido.matricula());
    }

    private AlunoRequest dados() {
        return new AlunoRequest("Aluno", UUID.randomUUID().toString(), "senha-segura", "aluno@teste.com", "ADS");
    }
}
