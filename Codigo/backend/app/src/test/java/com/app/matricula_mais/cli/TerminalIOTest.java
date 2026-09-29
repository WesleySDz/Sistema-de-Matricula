package com.app.matricula_mais.cli;

import static org.assertj.core.api.Assertions.*;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class TerminalIOTest {
    @Test
    void repetePerguntasParaIdsInvalidosSemPerderAProximaEntrada() {
        var io = new TerminalIO(new StringReader("abc\n-1\n7\n1,,2\n1, 2\n\n"), new PrintWriter(new StringWriter()));
        assertThat(io.id("ID: ")).isEqualTo(7);
        assertThat(io.ids("IDs: ")).containsExactly(1L, 2L);
        assertThat(io.ids("IDs: ")).isEmpty();
    }

    @Test
    void eofECancelamentoSaoDistintosESenhaPreservaEspacos() {
        var io = new TerminalIO(new StringReader(" senha \n/voltar\n"), new PrintWriter(new StringWriter()));
        assertThat(io.senha("Senha: ")).isEqualTo(" senha ");
        assertThatThrownBy(() -> io.texto("Nome: ")).isInstanceOf(TerminalIO.OperacaoCancelada.class);
        assertThatThrownBy(() -> io.texto("Nome: ")).isInstanceOf(TerminalIO.FimEntrada.class);
    }
}
