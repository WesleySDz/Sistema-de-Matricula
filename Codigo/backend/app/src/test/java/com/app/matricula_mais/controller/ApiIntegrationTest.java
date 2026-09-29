package com.app.matricula_mais.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.matricula_mais.model.Aluno;
import com.app.matricula_mais.model.Curso;
import com.app.matricula_mais.repository.AlunoRepository;
import com.app.matricula_mais.repository.CursoRepository;
import com.app.matricula_mais.security.UsuarioAutenticado;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    AlunoRepository alunos;
    @Autowired
    CursoRepository cursos;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    ObjectMapper json;

    @Test
    void exigeAutenticacaoPerfilSecretariaECsrf() throws Exception {
        mvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/alunos").with(perfil("ALUNO"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/alunos").with(perfil("SECRETARIA"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("campos").isArray());
    }

    @Test
    void crudDeAlunoProtegeSenhaEValidaDuplicidade() throws Exception {
        String login = "crud-" + UUID.randomUUID();
        Curso curso = novoCurso();
        String corpo = """
                {"nome":"Ana","login":"%s","senha":"senha-segura","email":"ana@teste.com",
                 "cursoId":%d}
                """.formatted(login, curso.getId());
        var resultado = mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("cursoId").value(curso.getId())).andExpect(jsonPath("curso").value(curso.getNome()))
                .andExpect(jsonPath("senha").doesNotExist()).andReturn();
        long id = json.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
        String matricula = json.readTree(resultado.getResponse().getContentAsString()).get("matricula").asString();
        assertThat(matricula).matches("[0-9]{7,}");
        assertThat(alunos.findById(id).orElseThrow().getCurso().getId()).isEqualTo(curso.getId());
        assertThat(encoder.matches("senha-segura", alunos.findById(id).orElseThrow().getSenha())).isTrue();
        mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/alunos/{id}", id).with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo.replace("Ana", "Ana Maria")
                    .replace("\"cursoId\":", "\"matricula\":\"9999999\",\"cursoId\":")))
                .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Ana Maria"))
                .andExpect(jsonPath("matricula").value(matricula));
        mvc.perform(get("/api/alunos/{id}", id).with(perfil("SECRETARIA")))
                .andExpect(status().isOk()).andExpect(jsonPath("senha").doesNotExist());
        mvc.perform(delete("/api/alunos/{id}", id).with(perfil("SECRETARIA")).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/alunos/{id}", id).with(perfil("SECRETARIA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteNaoEscolheMatriculaNemNoCadastro() throws Exception {
        String login = "automatica-" + UUID.randomUUID();
        Curso curso = novoCurso();
        String corpo = """
            {"nome":"Ana","login":"%s","senha":"senha-segura","email":"ana@teste.com",
             "matricula":"ESCOLHIDA-PELO-CLIENTE","cursoId":%d}
            """.formatted(login, curso.getId());
        var resultado = mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andReturn();
        var aluno = json.readTree(resultado.getResponse().getContentAsString());
                assertThat(aluno.get("matricula").asString()).matches("[0-9]{7,}");
        assertThat(alunos.findById(aluno.get("id").asLong()).orElseThrow().getMatricula())
                        .isEqualTo(aluno.get("matricula").asString());
    }

    @Test
    void loginCriaSessaoELogoutEncerraAcesso() throws Exception {
        Aluno aluno = novoAluno();
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("token").isNotEmpty());
        var login = mvc.perform(post("/api/auth/login").with(csrf())
                .param("login", aluno.getLogin()).param("senha", "senha-segura"))
                .andExpect(status().isNoContent()).andReturn();
        MockHttpSession sessao = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(sessao).isNotNull();
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isOk())
                .andExpect(jsonPath("id").value(aluno.getId())).andExpect(jsonPath("perfil").value("ALUNO"))
                .andExpect(jsonPath("senha").doesNotExist());
        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf())).andExpect(status().isNoContent());
        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    void cincoFalhasBloqueiamPorQuinzeMinutosEPersistemMesmoComErro() throws Exception {
        Aluno aluno = novoAluno();
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).param("login", aluno.getLogin()).param("senha", "errada"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).param("login", aluno.getLogin()).param("senha", "errada"))
                .andExpect(status().isLocked());
        aluno = alunos.findById(aluno.getId()).orElseThrow();
        assertThat(aluno.getTentativasInvalidas()).isEqualTo(5);
        assertThat(aluno.getBloqueadoAte()).isBetween(LocalDateTime.now().plusMinutes(14),
                LocalDateTime.now().plusMinutes(16));
        mvc.perform(
                post("/api/auth/login").with(csrf()).param("login", aluno.getLogin()).param("senha", "senha-segura"))
                .andExpect(status().isLocked());
        aluno.setBloqueadoAte(LocalDateTime.now().minusSeconds(1));
        alunos.save(aluno);
        mvc.perform(
                post("/api/auth/login").with(csrf()).param("login", aluno.getLogin()).param("senha", "senha-segura"))
                .andExpect(status().isNoContent());
        assertThat(alunos.findById(aluno.getId()).orElseThrow().getTentativasInvalidas()).isZero();
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void fluxoHttpDoCurriculoAInscricaoEConsultasPorPerfil() throws Exception {
        String identificador = UUID.randomUUID().toString();
        long professorId = criar("/api/professores", """
                {"nome":"Docente","login":"%s","senha":"senha-segura","email":"docente@teste.com","titulacao":"Mestre"}
                """.formatted(identificador));
        long disciplinaId = criar("/api/disciplinas", """
                {"codigo":"%s","nome":"Algoritmos","creditos":4,"professorId":%d}
                """.formatted(identificador, professorId));
        long cursoId = criar("/api/cursos", """
                {"nome":"ADS","quantidadeCreditos":120,"disciplinaIds":[%d]}
                """.formatted(disciplinaId));
        mvc.perform(get("/api/cursos/{id}", cursoId).with(perfil("SECRETARIA")))
                .andExpect(status().isOk()).andExpect(jsonPath("disciplinas[0].id").value(disciplinaId));
        long semestreId = criar("/api/semestres", "{\"nome\":\"" + identificador + "\"}");
        mvc.perform(put("/api/semestres/{id}/curriculo", semestreId).with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaIds\":[" + disciplinaId + "]}"))
                .andExpect(status().isOk());
        Aluno aluno = novoAluno();
        String corpo = "{\"semestreId\":" + semestreId + ",\"obrigatorias\":[" + disciplinaId + "],\"optativas\":[]}";
        var inscricao = mvc.perform(post("/api/matriculas").with(perfil("ALUNO", aluno.getId())).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$[0].alunoId").value(aluno.getId())).andReturn();
        long matriculaId = json.readTree(inscricao.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(get("/api/matriculas/me/disciplinas").param("semestreId", Long.toString(semestreId))
                .with(perfil("ALUNO", aluno.getId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].creditos").value(4))
                .andExpect(jsonPath("$[0].professorNome").value("Docente"));
        mvc.perform(get("/api/disciplinas/{id}/matriculas", disciplinaId).param("semestreId", Long.toString(semestreId))
                .with(perfil("SECRETARIA")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(matriculaId));
        mvc.perform(get("/api/professores/me/disciplinas").with(perfil("PROFESSOR", professorId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(disciplinaId));
        mvc.perform(get("/api/professores/me/disciplinas/{id}/alunos", disciplinaId)
                .param("semestreId", Long.toString(semestreId)).with(perfil("PROFESSOR", professorId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(aluno.getId()));
        mvc.perform(get("/api/professores/me/disciplinas/{id}/alunos", disciplinaId)
                .param("semestreId", Long.toString(semestreId)).with(perfil("PROFESSOR")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/matriculas/{id}", matriculaId).with(perfil("ALUNO")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/matriculas/{id}", matriculaId).with(perfil("ALUNO", aluno.getId())).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/matriculas/me").param("semestreId", Long.toString(semestreId))
                .with(perfil("ALUNO", aluno.getId())))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"curso\":\"Engenharia de Dragões\"", ",\"cursoId\":null",
        ",\"cursoId\":0", ",\"cursoId\":-1", ",\"cursoId\":\"Engenharia de Dragões\""})
    void cadastroExigeIdDeCursoPositivoSemCriarAlunoOuCurso(String campoCurso) throws Exception {
        long totalAlunos = alunos.count();
        long totalCursos = cursos.count();
        String corpo = """
            {"nome":"Ana","login":"%s","senha":"senha-segura","email":"ana@teste.com"%s}
            """.formatted(UUID.randomUUID(), campoCurso);
        mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest());
        assertThat(alunos.count()).isEqualTo(totalAlunos);
        assertThat(cursos.count()).isEqualTo(totalCursos);
    }

    @Test
    void cursoInexistenteNaoCadastraAlunoNemAlteraCadastroExistente() throws Exception {
        Aluno aluno = novoAluno();
        Long cursoId = aluno.getCurso().getId();
        long totalAlunos = alunos.count();
        long totalCursos = cursos.count();
        String corpo = """
            {"nome":"Alterado","login":"%s","senha":"senha-segura","email":"ana@teste.com","cursoId":%d}
            """.formatted(UUID.randomUUID(), Long.MAX_VALUE);
        mvc.perform(post("/api/alunos").with(perfil("SECRETARIA")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isNotFound());
        mvc.perform(put("/api/alunos/{id}", aluno.getId()).with(perfil("SECRETARIA")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isNotFound());
        Aluno preservado = alunos.findById(aluno.getId()).orElseThrow();
        assertThat(preservado.getNome()).isEqualTo(aluno.getNome());
        assertThat(preservado.getLogin()).isEqualTo(aluno.getLogin());
        assertThat(preservado.getCurso().getId()).isEqualTo(cursoId);
        assertThat(alunos.count()).isEqualTo(totalAlunos);
        assertThat(cursos.count()).isEqualTo(totalCursos);
    }

    @Test
    void atualizacaoTrocaVinculoEConsultaRefleteRenomeacaoDoCurso() throws Exception {
        Aluno aluno = novoAluno();
        Curso curso = novoCurso();
        long totalCursos = cursos.count();
        String corpo = """
            {"nome":"Ana","login":"%s","senha":"senha-segura","email":"ana@teste.com","cursoId":%d}
            """.formatted(aluno.getLogin(), curso.getId());
        mvc.perform(put("/api/alunos/{id}", aluno.getId()).with(perfil("SECRETARIA")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk()).andExpect(jsonPath("cursoId").value(curso.getId()))
            .andExpect(jsonPath("matricula").value(aluno.getMatricula()));
        assertThat(alunos.findById(aluno.getId()).orElseThrow().getCurso().getId()).isEqualTo(curso.getId());
        curso.setNome("Sistemas de Informação");
        cursos.saveAndFlush(curso);
        mvc.perform(get("/api/alunos/{id}", aluno.getId()).with(perfil("SECRETARIA")))
            .andExpect(status().isOk()).andExpect(jsonPath("curso").value("Sistemas de Informação"));
        mvc.perform(delete("/api/cursos/{id}", curso.getId()).with(perfil("SECRETARIA")).with(csrf()))
            .andExpect(status().isConflict());
        assertThat(cursos.count()).isEqualTo(totalCursos);
        assertThat(alunos.existsById(aluno.getId())).isTrue();
    }

    private long criar(String rota, String corpo) throws Exception {
        var resultado = mvc.perform(post(rota).with(perfil("SECRETARIA")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private Aluno novoAluno() {
        String login = UUID.randomUUID().toString();
        return alunos
                .save(new Aluno(null, "Ana", login, encoder.encode("senha-segura"), "ana@teste.com", login, novoCurso()));
    }

    private Curso novoCurso() {
        return cursos.save(new Curso(null, "ADS", 120));
    }

    private RequestPostProcessor perfil(String perfil) {
        return perfil(perfil, -1L);
    }

    private RequestPostProcessor perfil(String perfil, Long id) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                new UsuarioAutenticado(id, "teste", perfil), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + perfil))));
    }
}
