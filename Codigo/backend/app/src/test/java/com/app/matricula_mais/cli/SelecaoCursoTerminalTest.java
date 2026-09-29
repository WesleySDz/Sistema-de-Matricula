package com.app.matricula_mais.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.app.matricula_mais.dto.Respostas.CursoResponse;
import com.app.matricula_mais.security.UsuarioAutenticado;

class SelecaoCursoTerminalTest {
    private final ApiClient api = mock(ApiClient.class);

    @Test
    void semCursosAvisaECancelaCadastroSemSolicitarId() {
        when(api.listar("/api/cursos", CursoResponse.class)).thenReturn(List.of());

        String saida = cadastrar("0", "0", "0");

        assertThat(saida).contains("Nenhum curso cadastrado. Cadastre um curso antes de cadastrar o aluno.",
            "Operação cancelada.", "Sistema encerrado.")
            .doesNotContain("Digite o ID do curso desejado:", "Confirmar cadastro?", "Cadastro realizado.");
        verify(api, never()).salvar(anyString(), anyString(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 401, 403, 500})
    void falhaDeConsultaNaoEhTratadaComoCursoInvalido(int status) {
        when(api.listar("/api/cursos", CursoResponse.class))
            .thenReturn(List.of(new CursoResponse(42L, "Sistemas de Informação", 120, List.of())));
        when(api.buscar("/api/cursos/42", CursoResponse.class))
            .thenThrow(new ApiException(status, "Falha na consulta do curso."));

        String saida = cadastrar("42", "0", "0", "0");

        assertThat(saida).contains("Falha na consulta do curso.", "Sistema encerrado.")
            .doesNotContain("Curso inválido.", "Confirmar cadastro?", "Cadastro realizado.");
        verify(api, never()).salvar(anyString(), anyString(), any(), any());
    }

    private String cadastrar(String... selecaoESaida) {
        when(api.login("secretaria", "senha-secretaria"))
            .thenReturn(new UsuarioAutenticado(1L, "Secretaria", "SECRETARIA"));
        String entradas = "secretaria\nsenha-secretaria\n1\n3\nAna\nana\nsenha-segura\nana@teste.com\n"
            + String.join("\n", selecaoESaida) + "\n";
        StringWriter saida = new StringWriter();
        new TerminalApplication(api, new TerminalIO(new StringReader(entradas), new PrintWriter(saida, true))).executar();
        return saida.toString();
    }
}
