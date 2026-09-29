package com.app.matricula_mais.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.app.matricula_mais.dto.Requisicoes.PerfilRequest;
import com.app.matricula_mais.model.*;
import com.app.matricula_mais.repository.CursoRepository;
import com.app.matricula_mais.repository.UsuarioRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class PerfilIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CursoRepository cursos;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;

    @ParameterizedTest
    @ValueSource(strings = {"ALUNO", "PROFESSOR", "SECRETARIA"})
    void cadaPerfilEditaSomenteDadosPessoaisDaPropriaConta(String perfil) throws Exception {
        Usuario usuario = novoUsuario(perfil);
        Usuario outro = novoUsuario("ALUNO");
        MockHttpSession sessao = login(usuario.getLogin(), "senha-segura");
        String novoLogin = UUID.randomUUID().toString();
        String corpo = """
            {"nome":"Nome atualizado","login":"%s","email":"novo@teste.com",
             "id":%d,"perfil":"SECRETARIA","matricula":"ALTERADA","cursoId":999999,
             "titulacao":"ALTERADA","tentativasInvalidas":99}
            """.formatted(novoLogin, outro.getId());
        mvc.perform(put("/api/auth/me").session(sessao).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk()).andExpect(jsonPath("id").value(usuario.getId()))
            .andExpect(jsonPath("perfil").value(perfil)).andExpect(jsonPath("senha").doesNotExist());
        mvc.perform(get("/api/auth/me").session(sessao))
            .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Nome atualizado"))
            .andExpect(jsonPath("login").value(novoLogin)).andExpect(jsonPath("email").value("novo@teste.com"));
        Usuario atualizado = usuarios.findById(usuario.getId()).orElseThrow();
        assertThat(atualizado.getSenha()).isEqualTo(usuario.getSenha());
        assertThat(atualizado.getTentativasInvalidas()).isZero();
        assertThat(atualizado).isInstanceOf(usuario.getClass());
        if (atualizado instanceof Aluno aluno) {
            assertThat(aluno.getMatricula()).isEqualTo(((Aluno) usuario).getMatricula());
            assertThat(aluno.getCurso().getId()).isEqualTo(((Aluno) usuario).getCurso().getId());
        }
        if (atualizado instanceof Professor professor) {
            assertThat(professor.getTitulacao()).isEqualTo("Mestre");
        }
        assertThat(usuarios.findById(outro.getId()).orElseThrow().getNome()).isEqualTo(outro.getNome());
        login(novoLogin, "senha-segura");
    }

    @Test
    void alteraSenhaComHashERejeitaSenhaAnterior() throws Exception {
        Usuario usuario = novoUsuario("ALUNO");
        MockHttpSession sessao = login(usuario.getLogin(), "senha-segura");
        mvc.perform(put("/api/auth/me").session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(new PerfilRequest(usuario.getNome(), usuario.getLogin(),
                usuario.getEmail(), "senha-atualizada"))))
            .andExpect(status().isOk()).andExpect(jsonPath("senha").doesNotExist());
        String hash = usuarios.findById(usuario.getId()).orElseThrow().getSenha();
        assertThat(hash).isNotEqualTo("senha-atualizada");
        assertThat(encoder.matches("senha-atualizada", hash)).isTrue();
        mvc.perform(post("/api/auth/login").with(csrf()).param("login", usuario.getLogin()).param("senha", "senha-segura"))
            .andExpect(status().isUnauthorized());
        login(usuario.getLogin(), "senha-atualizada");
    }

    @Test
    void validaDadosDuplicidadeESenhaSemSalvarAlteracoesParciais() throws Exception {
        Usuario usuario = novoUsuario("ALUNO");
        Usuario outro = novoUsuario("PROFESSOR");
        MockHttpSession sessao = login(usuario.getLogin(), "senha-segura");
        var invalidos = List.of(
            new PerfilRequest("", usuario.getLogin(), usuario.getEmail(), null),
            new PerfilRequest("Alterado", "", usuario.getEmail(), null),
            new PerfilRequest("Alterado", usuario.getLogin(), "invalido", null),
            new PerfilRequest("Alterado", outro.getLogin(), usuario.getEmail(), null),
            new PerfilRequest("Alterado", usuario.getLogin(), usuario.getEmail(), "curta"),
            new PerfilRequest("Alterado", usuario.getLogin(), usuario.getEmail(), "        "),
            new PerfilRequest("Alterado", usuario.getLogin(), usuario.getEmail(), "á".repeat(40)));
        for (var dados : invalidos) {
            mvc.perform(put("/api/auth/me").session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dados))).andExpect(status().is4xxClientError());
            Usuario preservado = usuarios.findById(usuario.getId()).orElseThrow();
            assertThat(preservado.getNome()).isEqualTo(usuario.getNome());
            assertThat(preservado.getLogin()).isEqualTo(usuario.getLogin());
            assertThat(preservado.getEmail()).isEqualTo(usuario.getEmail());
            assertThat(preservado.getSenha()).isEqualTo(usuario.getSenha());
        }
    }

    @Test
    void permiteEditarSecretariaInicialSemEmailEPreservaEmailExistenteQuandoOmitido() throws Exception {
        Usuario usuario = novoUsuario("SECRETARIA");
        usuario.setEmail(null);
        usuarios.save(usuario);
        MockHttpSession sessao = login(usuario.getLogin(), "senha-segura");
        for (String emailAtual : new String[] {null, "secretaria@teste.com"}) {
            usuario.setEmail(emailAtual);
            usuarios.save(usuario);
            mvc.perform(put("/api/auth/me").session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new PerfilRequest("Secretaria Editada", usuario.getLogin(), null, null))))
                .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Secretaria Editada"));
            assertThat(usuarios.findById(usuario.getId()).orElseThrow().getEmail()).isEqualTo(emailAtual);
        }
    }

    @Test
    void exigeSessaoECsrf() throws Exception {
        mvc.perform(put("/api/auth/me").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
        Usuario usuario = novoUsuario("ALUNO");
        mvc.perform(put("/api/auth/me").session(login(usuario.getLogin(), "senha-segura"))
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    private Usuario novoUsuario(String perfil) {
        String login = UUID.randomUUID().toString();
        String hash = encoder.encode("senha-segura");
        return usuarios.save(switch (perfil) {
            case "ALUNO" -> new Aluno(null, "Ana", login, hash, "ana@teste.com", login,
                cursos.save(new Curso(null, "ADS", 120)));
            case "PROFESSOR" -> new Professor(null, "Ana", login, hash, "ana@teste.com", "Mestre");
            default -> new FuncionarioSecretaria(null, "Ana", login, hash, "ana@teste.com");
        });
    }

    private MockHttpSession login(String login, String senha) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
            .param("login", login).param("senha", senha))
            .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
    }
}
