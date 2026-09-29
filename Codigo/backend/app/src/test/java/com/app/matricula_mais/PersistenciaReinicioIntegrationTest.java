package com.app.matricula_mais;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.app.matricula_mais.cli.ApiClient;
import com.app.matricula_mais.cli.ApiException;
import com.app.matricula_mais.dto.Requisicoes.*;
import com.app.matricula_mais.dto.Respostas.*;
import com.app.matricula_mais.security.UsuarioAutenticado;

import tools.jackson.databind.ObjectMapper;

/** HTTP e banco em arquivo reais, com todos os contextos e conexões fechados entre as etapas. */
class PersistenciaReinicioIntegrationTest {
    @TempDir Path diretorio;

    @Test
    void crudRelacionamentosEHistoricoSobrevivemAReinicializacoes() throws Exception {
        Dados dados;
        try (var contexto = iniciar(); var api = cliente(contexto)) {
            dados = cadastrarEEditar(api);
        }
        assertThat(diretorio.resolve("matricula-mais.mv.db")).exists();
        verificarBancoFechado(dados, true);

        try (var contexto = iniciar(); var api = cliente(contexto)) {
            consultarECancelar(api, dados);
        }
        verificarBancoFechado(dados, false);

        try (var contexto = iniciar(); var api = cliente(contexto)) {
            api.login("aluno-editado", "senha-editada");
            var perfil = api.buscar("/api/auth/me", UsuarioAutenticado.class);
            assertThat(perfil.nome()).isEqualTo("Aluno Editado");
            assertThat(perfil.email()).isEqualTo("editado@teste.com");
            var historico = api.listar("/api/matriculas/me/historico", MatriculaResponse.class);
            assertThat(historico).singleElement().satisfies(m -> {
                assertThat(m.alunoId()).isEqualTo(dados.aluno().id());
                assertThat(m.disciplina().id()).isEqualTo(dados.obrigatoria().id());
                assertThat(m.ativa()).isTrue();
            });
            var semestre = api.buscar("/api/semestres/" + dados.semestre().id(), SemestreResponse.class);
            assertThat(semestre.concluido()).isTrue();
            assertThat(semestre.periodoMatriculaAberto()).isFalse();
            assertThat(semestre.disciplinasCanceladas()).containsExactly(dados.optativa().id());
            assertThatThrownBy(() -> api.executar("POST", "/api/matriculas",
                new MatriculaRequest(semestre.id(), List.of(dados.optativa().id()), List.of())))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(409));
        }
    }

    private Dados cadastrarEEditar(ApiClient api) {
        api.login("secretaria-persistencia", "senha-secretaria");
        var professor = api.salvar("POST", "/api/professores",
            new ProfessorRequest("Docente", "docente", "senha-docente", "docente@teste.com", "Mestre"), ProfessorResponse.class);
        professor = api.salvar("PUT", "/api/professores/" + professor.id(),
            new ProfessorRequest("Docente Editado", "docente", "senha-docente", "docente@teste.com", "Doutor"), ProfessorResponse.class);
        var obrigatoria = api.salvar("POST", "/api/disciplinas",
            new DisciplinaRequest("ALG", "Algoritmos", 4, professor.id()), DisciplinaResponse.class);
        obrigatoria = api.salvar("PUT", "/api/disciplinas/" + obrigatoria.id(),
            new DisciplinaRequest("ALG", "Algoritmos Editados", 6, professor.id()), DisciplinaResponse.class);
        var optativa = api.salvar("POST", "/api/disciplinas",
            new DisciplinaRequest("OPT", "Optativa", 2, professor.id()), DisciplinaResponse.class);
        var curso = api.salvar("POST", "/api/cursos",
            new CursoRequest("Curso", 120, List.of(obrigatoria.id())), CursoResponse.class);
        curso = api.salvar("PUT", "/api/cursos/" + curso.id(),
            new CursoRequest("Curso Editado", 160, List.of(obrigatoria.id(), optativa.id())), CursoResponse.class);
        var aluno = cadastrarAluno(api, "aluno", curso.id());
        aluno = api.salvar("PUT", "/api/alunos/" + aluno.id(),
            new AlunoRequest("Aluno Atualizado", "aluno", "senha-aluno", "aluno@teste.com", curso.id()), AlunoResponse.class);
        cadastrarAluno(api, "segundo", curso.id());
        cadastrarAluno(api, "terceiro", curso.id());
        var semestre = api.salvar("POST", "/api/semestres", new SemestreRequest("2026/2"), SemestreResponse.class);
        api.salvar("PUT", "/api/semestres/" + semestre.id() + "/curriculo",
            new CurriculoRequest(List.of(obrigatoria.id())), SemestreResponse.class);
        semestre = api.salvar("PUT", "/api/semestres/" + semestre.id() + "/curriculo",
            new CurriculoRequest(List.of(obrigatoria.id(), optativa.id())), SemestreResponse.class);

        // Exclusões reais de todos os cadastros que possuem DELETE, sem vínculos impeditivos.
        var removido = cadastrarAluno(api, "removido", curso.id());
        api.executar("DELETE", "/api/alunos/" + removido.id(), null);
        var cursoRemovido = api.salvar("POST", "/api/cursos",
            new CursoRequest("Removido", 10, List.of()), CursoResponse.class);
        api.executar("DELETE", "/api/cursos/" + cursoRemovido.id(), null);
        var disciplinaRemovida = api.salvar("POST", "/api/disciplinas",
            new DisciplinaRequest("REM", "Removida", 2, professor.id()), DisciplinaResponse.class);
        api.executar("DELETE", "/api/disciplinas/" + disciplinaRemovida.id(), null);
        var professorRemovido = api.salvar("POST", "/api/professores",
            new ProfessorRequest("Removido", "removido", "senha-docente", "removido@teste.com", "Mestre"), ProfessorResponse.class);
        api.executar("DELETE", "/api/professores/" + professorRemovido.id(), null);

        api.login("aluno", "senha-aluno");
        api.salvar("PUT", "/api/auth/me",
            new PerfilRequest("Aluno Editado", "aluno-editado", "editado@teste.com", "senha-editada"), UsuarioAutenticado.class);
        api.executar("POST", "/api/matriculas", new MatriculaRequest(semestre.id(), List.of(obrigatoria.id()), List.of(optativa.id())));
        var inscricoes = api.listar("/api/matriculas/me?semestreId=" + semestre.id(), MatriculaResponse.class);
        long cancelavel = inscricoes.stream().filter(MatriculaResponse::optativa).findFirst().orElseThrow().id();
        for (String login : List.of("segundo", "terceiro")) {
            api.login(login, "senha-aluno");
            api.executar("POST", "/api/matriculas", new MatriculaRequest(semestre.id(), List.of(obrigatoria.id()), List.of()));
        }
        return new Dados(aluno, professor, curso, obrigatoria, optativa, semestre, cancelavel, removido,
            cursoRemovido.id(), disciplinaRemovida.id(), professorRemovido.id());
    }

    private void consultarECancelar(ApiClient api, Dados dados) {
        api.login("secretaria-persistencia", "senha-secretaria");
        var alunos = api.listar("/api/alunos", AlunoResponse.class);
        assertThat(alunos).hasSize(3).filteredOn(a -> a.id().equals(dados.aluno().id())).singleElement().satisfies(a -> {
            assertThat(a.matricula()).isEqualTo(dados.aluno().matricula());
            assertThat(a.cursoId()).isEqualTo(dados.curso().id());
            assertThat(a.curso()).isEqualTo("Curso Editado");
            assertThat(a.nome()).isEqualTo("Aluno Editado");
        });
        assertThat(api.listar("/api/professores", ProfessorResponse.class)).containsExactly(dados.professor());
        assertThat(api.listar("/api/disciplinas", DisciplinaResponse.class))
            .containsExactlyInAnyOrder(dados.obrigatoria(), dados.optativa());
        assertThat(api.listar("/api/cursos", CursoResponse.class)).singleElement().satisfies(c -> {
            assertThat(c.id()).isEqualTo(dados.curso().id());
            assertThat(c.nome()).isEqualTo("Curso Editado");
            assertThat(c.quantidadeCreditos()).isEqualTo(160);
            assertThat(c.disciplinas()).containsExactlyInAnyOrder(dados.obrigatoria(), dados.optativa());
        });
        assertThat(api.listar("/api/semestres", SemestreResponse.class)).singleElement().satisfies(s ->
            assertThat(s.disciplinasOfertadas()).containsExactlyInAnyOrder(dados.obrigatoria(), dados.optativa()));
        for (String rota : List.of("/api/alunos/" + dados.removido().id(), "/api/cursos/" + dados.cursoRemovido(),
                "/api/disciplinas/" + dados.disciplinaRemovida(), "/api/professores/" + dados.professorRemovido())) {
            assertThatThrownBy(() -> api.buscar(rota, Object.class))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(404));
        }
        for (String rota : List.of("/api/alunos/" + dados.aluno().id(), "/api/cursos/" + dados.curso().id(),
                "/api/disciplinas/" + dados.obrigatoria().id(), "/api/professores/" + dados.professor().id())) {
            assertThatThrownBy(() -> api.executar("DELETE", rota, null))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(409));
        }
        var novo = cadastrarAluno(api, "apos-reinicio", dados.curso().id());
        assertThat(Long.parseLong(novo.matricula())).isGreaterThan(Long.parseLong(dados.removido().matricula()));

        api.login("docente", "senha-docente");
        assertThat(api.listar("/api/professores/me/disciplinas/" + dados.obrigatoria().id()
            + "/alunos?semestreId=" + dados.semestre().id(), AlunoResponse.class)).hasSize(3);
        api.login("aluno-editado", "senha-editada");
        assertThat(api.listar("/api/matriculas/me?semestreId=" + dados.semestre().id(), MatriculaResponse.class)).hasSize(2);
        api.executar("DELETE", "/api/matriculas/" + dados.cancelavel(), null);
        api.login("secretaria-persistencia", "senha-secretaria");
        api.executar("POST", "/api/semestres/" + dados.semestre().id() + "/encerrar-matriculas", null);
        api.executar("POST", "/api/semestres/" + dados.semestre().id() + "/concluir", null);
    }

    private void verificarBancoFechado(Dados dados, boolean optativaAtiva) throws Exception {
        // JDBC independente, depois que Spring/JPA e o pool já foram encerrados.
        try (var conexao = DriverManager.getConnection("jdbc:h2:file:" + caminhoBanco() + ";IFEXISTS=TRUE", "sa", "");
             var consulta = conexao.prepareStatement("""
                 select u.nome, u.matricula, u.curso_id, c.nome, d.professor_responsavel_id, m.ativa
                 from usuario u join curso c on c.id = u.curso_id
                 join matricula m on m.aluno_id = u.id join disciplina d on d.id = m.disciplina_id
                 where m.id = ?
                 """)) {
            consulta.setLong(1, dados.cancelavel());
            try (var resultado = consulta.executeQuery()) {
                assertThat(resultado.next()).isTrue();
                assertThat(resultado.getString(1)).isEqualTo("Aluno Editado");
                assertThat(resultado.getString(2)).isEqualTo(dados.aluno().matricula());
                assertThat(resultado.getLong(3)).isEqualTo(dados.curso().id());
                assertThat(resultado.getString(4)).isEqualTo("Curso Editado");
                assertThat(resultado.getLong(5)).isEqualTo(dados.professor().id());
                assertThat(resultado.getBoolean(6)).isEqualTo(optativaAtiva);
            }
            try (var sql = conexao.createStatement(); var resultado = sql.executeQuery("select count(*) from notificacao_cobranca")) {
                assertThat(resultado.next()).isTrue();
                assertThat(resultado.getInt(1)).isEqualTo(3);
            }
        }
    }

    private AlunoResponse cadastrarAluno(ApiClient api, String login, Long cursoId) {
        return api.salvar("POST", "/api/alunos",
            new AlunoRequest("Aluno", login, "senha-aluno", login + "@teste.com", cursoId), AlunoResponse.class);
    }

    private String caminhoBanco() {
        return diretorio.resolve("matricula-mais").toAbsolutePath().toString().replace('\\', '/');
    }

    private ConfigurableApplicationContext iniciar() {
        // Carrega a configuração de produção, sem herdar o banco em memória de src/test/resources.
        return new SpringApplicationBuilder(MatriculaMaisApplication.class).run(
            "--spring.config.location=" + Path.of("src/main/resources/application.properties").toAbsolutePath().toUri(),
            "--app.data-dir=" + diretorio.toAbsolutePath().toString().replace('\\', '/'),
            "--app.cli.enabled=false", "--server.port=0", "--server.address=127.0.0.1",
            "--app.secretaria.login=secretaria-persistencia", "--app.secretaria.senha=senha-secretaria",
            "--app.cobranca.url=", "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }

    private ApiClient cliente(ConfigurableApplicationContext contexto) {
        int porta = ((WebServerApplicationContext) contexto).getWebServer().getPort();
        return new ApiClient(URI.create("http://127.0.0.1:" + porta), contexto.getBean(ObjectMapper.class));
    }

    private record Dados(AlunoResponse aluno, ProfessorResponse professor, CursoResponse curso,
            DisciplinaResponse obrigatoria, DisciplinaResponse optativa, SemestreResponse semestre,
            long cancelavel, AlunoResponse removido, long cursoRemovido, long disciplinaRemovida, long professorRemovido) { }
}
