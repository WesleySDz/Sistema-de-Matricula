package com.app.matricula_mais.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Apenas leitura, conversão de entrada e apresentação de texto. */
public class TerminalIO {
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private final Console console;

    public TerminalIO(Reader entrada, PrintWriter saida) {
        this(entrada, saida, null);
    }

    private TerminalIO(Reader entrada, PrintWriter saida, Console console) {
        this.entrada = new BufferedReader(entrada);
        this.saida = saida;
        this.console = console;
    }

    public static TerminalIO sistema() {
        Console console = System.console();
        return console == null
                ? new TerminalIO(new InputStreamReader(System.in, StandardCharsets.UTF_8),
                        new PrintWriter(System.out, true))
                : new TerminalIO(console.reader(), console.writer(), console);
    }

    public void linha(String mensagem) {
        // Não interpreta sequências de controle recebidas dos cadastros.
        saida.println(mensagem.replaceAll("[\\p{Cc}&&[^\\n\\t]]", ""));
        saida.flush();
    }

    public void titulo(String texto) {
        linha("\n=====================================");
        linha(texto);
        linha("=====================================");
    }

    public String texto(String pergunta) {
        return ler(pergunta, false).trim();
    }

    public String senha(String pergunta) {
        if (console == null) {
            linha("(Neste console a senha ficará visível; em um terminal interativo ela é ocultada.)");
        }
        return ler(pergunta, true);
    }

    private String ler(String pergunta, boolean segredo) {
        saida.print(pergunta);
        saida.flush();
        try {
            String valor;
            if (segredo && console != null) {
                char[] caracteres = console.readPassword();
                if (caracteres == null)
                    throw new FimEntrada();
                valor = new String(caracteres);
                Arrays.fill(caracteres, '\0');
            } else {
                valor = entrada.readLine();
            }
            if (valor == null)
                throw new FimEntrada();
            if (valor.equalsIgnoreCase("/voltar"))
                throw new OperacaoCancelada();
            return valor;
        } catch (IOException ex) {
            throw new FimEntrada();
        }
    }

    public int opcao() {
        while (true) {
            try {
                return Integer.parseInt(texto("Escolha uma opção: "));
            } catch (NumberFormatException ex) {
                linha("Digite o número de uma opção.");
            }
        }
    }

    public long id(String pergunta) {
        while (true) {
            try {
                long valor = Long.parseLong(texto(pergunta));
                if (valor > 0)
                    return valor;
            } catch (NumberFormatException ignored) {
            }
            linha("Informe um ID numérico positivo.");
        }
    }

    public int inteiro(String pergunta) {
        while (true) {
            try {
                return Integer.parseInt(texto(pergunta));
            } catch (NumberFormatException ex) {
                linha("Informe um número inteiro.");
            }
        }
    }

    public List<Long> ids(String pergunta) {
        while (true) {
            String valor = texto(pergunta);
            if (valor.isBlank())
                return List.of();
            try {
                List<Long> ids = Arrays.stream(valor.split(",", -1)).map(String::trim).map(Long::valueOf).toList();
                if (ids.stream().allMatch(id -> id > 0))
                    return ids;
            } catch (NumberFormatException ignored) {
            }
            linha("Informe IDs positivos separados por vírgula, ou deixe em branco para nenhum.");
        }
    }

    public boolean confirmar(String mensagem) {
        return texto(mensagem + " [s/N]: ").equalsIgnoreCase("s");
    }

    public static class FimEntrada extends RuntimeException {
    }

    public static class OperacaoCancelada extends RuntimeException {
    }
}
