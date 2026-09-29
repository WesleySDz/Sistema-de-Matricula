package com.app.matricula_mais.cli;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.app.matricula_mais.dto.Requisicoes.*;
import com.app.matricula_mais.dto.Respostas.*;
import com.app.matricula_mais.security.UsuarioAutenticado;
import tools.jackson.databind.ObjectMapper;

/** Usa HTTP real: terminal -> filtros de segurança -> controllers -> banco. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:terminal-tests;DB_CLOSE_DELAY=-1",
    "server.address=127.0.0.1",
    "app.secretaria.login=secretaria-terminal", "app.secretaria.senha=senha-secretaria",
    "app.cobranca.url="
})
class TerminalIntegrationTest {
    @LocalServerPort int porta;
    @Autowired ObjectMapper json;
    private ApiClient api;
    private AlunoResponse aluno;
    private ProfessorResponse professor;
    private DisciplinaResponse disciplina;
    private SemestreResponse semestre;
    private String sufixo;

    @BeforeEach
    void prepararPelaApi() {
        api = cliente();
        api.login("secretaria-terminal", "senha-secretaria");
        sufixo = UUID.randomUUID().toString();
        aluno = api.salvar("POST", "/api/alunos", new AlunoRequest("Ana " + sufixo, "ana-" + sufixo,
            "senha-aluno", "ana@teste.com", "ADS"), AlunoResponse.class);
        professor = api.salvar("POST", "/api/professores", new ProfessorRequest("Docente " + sufixo,
            "docente-" + sufixo, "senha-professor", "docente@teste.com", "Mestre"), ProfessorResponse.class);
        disciplina = api.salvar("POST", "/api/disciplinas", new DisciplinaRequest("DISC-" + sufixo,
            "Algoritmos " + sufixo, 4, professor.id()), DisciplinaResponse.class);
        semestre = api.salvar("POST", "/api/semestres", new SemestreRequest("semestre-" + sufixo), SemestreResponse.class);
        api.salvar("PUT", "/api/semestres/" + semestre.id() + "/curriculo",
            new CurriculoRequest(List.of(disciplina.id())), SemestreResponse.class);
        api.logout();
    }

    @AfterEach
    void fechar() {
        api.close();
    }

    @Test
    void umLoginTrocaOsTresMenusEEncerraAsSessoes() {
        String saida = terminal("secretaria-terminal", "senha-secretaria", "9", "0",
            aluno.login(), "senha-aluno", "9", "0", professor.login(), "senha-professor", "9", "0", "0");
        assertThat(saida).contains("Olá, Secretaria", "Perfil: SECRETARIA", "Gerenciar alunos",
            "Olá, " + aluno.nome(), "Perfil: ALUNO", "Realizar matrícula",
            "Olá, " + professor.nome(), "Perfil: PROFESSOR", "Consultar minhas disciplinas", "Sistema encerrado.");
        String menuAluno = saida.substring(saida.indexOf("Olá, " + aluno.nome()), saida.indexOf("Olá, " + professor.nome()));
        assertThat(menuAluno).doesNotContain("Gerenciar alunos", "Gerenciar cursos", "Gerenciar professores");
        String menuProfessor = saida.substring(saida.indexOf("Olá, " + professor.nome()));
        assertThat(menuProfessor).doesNotContain("Realizar matrícula", "Gerenciar alunos");
        assertThat(saida).doesNotContain("senha-secretaria", "senha-aluno", "senha-professor");
    }

    @Test
    void secretariaCadastraAlunoPeloTerminalEOAlunoEntraNoMesmoLogin() {
        String login = "novo-" + sufixo;
        String saida = terminal("secretaria-terminal", "senha-secretaria", "1", "3",
            "Novo Aluno", login, "senha-nova", "novo@teste.com", "ADS", "s", "0", "0",
            login, "senha-nova", "9", "0", "0");
        assertThat(saida).contains("Cadastro realizado.", "Olá, Novo Aluno", "Perfil: ALUNO", "E-mail: novo@teste.com");
        assertThat(saida).doesNotContain("Número de matrícula:").containsPattern("Matrícula: [0-9]{7,}");
    }

    @Test
    void alunoMatriculaConsultaECancelaPeloTerminal() {
        String saida = terminal(aluno.login(), "senha-aluno", "2", semestre.id().toString(), disciplina.id().toString(), "", "s",
            "3", semestre.id().toString(), "4", semestre.id().toString(), "6", "0", "0");
        assertThat(saida).contains("Inscrição concluída.", "Créditos: 4", professor.nome(), "OBRIGATÓRIA");
        api.login(aluno.login(), "senha-aluno");
        var matriculas = api.listar("/api/matriculas/me?semestreId=" + semestre.id(), MatriculaResponse.class);
        assertThat(matriculas).hasSize(1).allMatch(m -> m.alunoId().equals(aluno.id()));
        String cancelamento = terminal(aluno.login(), "senha-aluno", "5", semestre.id().toString(),
            matriculas.getFirst().id().toString(), "s", "0", "0");
        assertThat(cancelamento).contains("Matrícula cancelada.");
        assertThat(api.listar("/api/matriculas/me?semestreId=" + semestre.id(), MatriculaResponse.class)).isEmpty();
    }

    @Test
    void permissoesSaoAplicadasMesmoAoChamarOperacoesForaDoMenu() {
        api.login(aluno.login(), "senha-aluno");
        assertThatThrownBy(() -> api.listar("/api/alunos", AlunoResponse.class))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(403));
        assertThatThrownBy(() -> api.salvar("POST", "/api/semestres", new SemestreRequest("proibido"), SemestreResponse.class))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(403));
        api.executar("POST", "/api/matriculas", new MatriculaRequest(semestre.id(), List.of(disciplina.id()), List.of()));
        Long matriculaId = api.listar("/api/matriculas/me?semestreId=" + semestre.id(), MatriculaResponse.class).getFirst().id();
        api.logout();

        api.login(professor.login(), "senha-professor");
        assertThatThrownBy(() -> api.executar("DELETE", "/api/matriculas/" + matriculaId, null))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(403));
        api.logout();

        api.login("secretaria-terminal", "senha-secretaria");
        AlunoResponse outro = api.salvar("POST", "/api/alunos", new AlunoRequest("Outro", "outro-" + sufixo,
            "senha-outro", "outro@teste.com", "ADS"), AlunoResponse.class);
        ProfessorResponse outroProfessor = api.salvar("POST", "/api/professores", new ProfessorRequest("Outro professor",
            "outro-prof-" + sufixo, "senha-outro", "outro@teste.com", "Doutor"), ProfessorResponse.class);
        api.logout();
        api.login(outro.login(), "senha-outro");
        assertThatThrownBy(() -> api.executar("DELETE", "/api/matriculas/" + matriculaId, null))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(403));
        api.logout();
        api.login(outroProfessor.login(), "senha-outro");
        assertThatThrownBy(() -> api.listar("/api/professores/me/disciplinas/" + disciplina.id()
            + "/alunos?semestreId=" + semestre.id(), AlunoResponse.class))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(403));
        api.logout();
        assertThatThrownBy(() -> api.buscar("/api/auth/me", UsuarioAutenticado.class))
            .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.status()).isEqualTo(401));
    }

    @Test
    void professorConsultaAlunosESemestreConcluidoEntraNoHistorico() {
        api.login("secretaria-terminal", "senha-secretaria");
        AlunoResponse segundo = api.salvar("POST", "/api/alunos", new AlunoRequest("Segundo", "segundo-" + sufixo,
            "senha-aluno", "segundo@teste.com", "ADS"), AlunoResponse.class);
        AlunoResponse terceiro = api.salvar("POST", "/api/alunos", new AlunoRequest("Terceiro", "terceiro-" + sufixo,
            "senha-aluno", "terceiro@teste.com", "ADS"), AlunoResponse.class);
        api.logout();
        for (var inscrito : List.of(aluno, segundo, terceiro)) {
            api.login(inscrito.login(), "senha-aluno");
            api.executar("POST", "/api/matriculas", new MatriculaRequest(semestre.id(), List.of(disciplina.id()), List.of()));
            api.logout();
        }
        String consulta = terminal(professor.login(), "senha-professor", "2", disciplina.id().toString(),
            semestre.id().toString(), "0", "0");
        assertThat(consulta).contains(aluno.nome(), "Segundo", "Terceiro");
        String encerramento = terminal("secretaria-terminal", "senha-secretaria", "5", "5", semestre.id().toString(),
            "6", semestre.id().toString(), "s", "7", semestre.id().toString(), "s", "0", "0", "0");
        assertThat(encerramento).contains("Alunos matriculados: 3", "CONCLUÍDO");
        String historico = terminal(aluno.login(), "senha-aluno", "6", "0", "0");
        assertThat(historico).contains(disciplina.nome(), semestre.nome());
    }

    @Test
    void entradaInvalidaCancelamentoDeFormularioEFimDeEntradaNaoDerrubamOFluxo() {
        String saida = terminal("secretaria-terminal", "errada", "secretaria-terminal", "senha-secretaria",
            "abc", "999", "1", "3", "/voltar", "1");
        assertThat(saida).contains("Login ou senha inválidos.", "Digite o número", "Opção inválida",
            "Operação cancelada.", "Sessão encerrada.", "Sistema encerrado.");
    }

    @Test
    void erroDeValidacaoDoServidorEExibidoNoFormulario() {
        String saida = terminal("secretaria-terminal", "senha-secretaria", "1", "3",
            "Inválido", "invalido-" + sufixo, "curta", "email-invalido", "ADS", "s", "0", "0", "0");
        assertThat(saida).contains("Dados inválidos.", "senha:", "email:").doesNotContain("Cadastro realizado.");
    }

    private ApiClient cliente() {
        return new ApiClient(URI.create("http://127.0.0.1:" + porta), json);
    }

    private String terminal(String... entradas) {
        StringWriter saida = new StringWriter();
        try (ApiClient cliente = cliente()) {
            new TerminalApplication(cliente, new TerminalIO(new StringReader(String.join("\n", entradas) + "\n"),
                new PrintWriter(saida, true))).executar();
        }
        return saida.toString();
    }
}
