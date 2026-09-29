package com.app.matricula_mais.cli;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.app.matricula_mais.dto.Requisicoes.AlunoRequest;
import com.app.matricula_mais.dto.Requisicoes.CurriculoRequest;
import com.app.matricula_mais.dto.Requisicoes.CursoRequest;
import com.app.matricula_mais.dto.Requisicoes.DisciplinaRequest;
import com.app.matricula_mais.dto.Requisicoes.MatriculaRequest;
import com.app.matricula_mais.dto.Requisicoes.ProfessorRequest;
import com.app.matricula_mais.dto.Requisicoes.PerfilRequest;
import com.app.matricula_mais.dto.Requisicoes.SemestreRequest;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.dto.Respostas.CursoResponse;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.MatriculaResponse;
import com.app.matricula_mais.dto.Respostas.ProfessorResponse;
import com.app.matricula_mais.dto.Respostas.SemestreResponse;
import com.app.matricula_mais.dto.Respostas.SituacaoDisciplinaResponse;
import com.app.matricula_mais.security.UsuarioAutenticado;

/** Menus e formulários. Toda operação de domínio é solicitada à API. */
public class TerminalApplication {
    private final ApiClient api;
    private final TerminalIO io;
    private final ApresentacaoTerminal tela;

    public TerminalApplication(ApiClient api, TerminalIO io) {
        this.api = api;
        this.io = io;
        this.tela = new ApresentacaoTerminal(io);
    }

    public void executar() {
        io.titulo("MATRÍCULA MAIS");
        io.linha("Digite /voltar em qualquer formulário para cancelar a operação.");
        try {
            while (true) {
                try {
                    io.titulo("LOGIN DO SISTEMA");
                    String login = io.texto("Login (0 para encerrar): ");
                    if (login.equals("0"))
                        return;
                    UsuarioAutenticado usuario = api.login(login, io.senha("Senha: "));
                    try {
                        menuPerfil(usuario);
                    } finally {
                        sairDaSessao();
                    }
                } catch (ApiException ex) {
                    io.linha(ex.getMessage());
                } catch (TerminalIO.OperacaoCancelada ex) {
                    io.linha("Operação cancelada.");
                }
            }
        } catch (TerminalIO.FimEntrada ex) {
            io.linha("\nEntrada encerrada.");
        } finally {
            io.linha("Sistema encerrado.");
        }
    }

    private void sairDaSessao() {
        try {
            api.logout();
        } catch (ApiException ex) {
            io.linha(ex.getMessage());
        }
        io.linha("Sessão encerrada.");
    }

    private void menuPerfil(UsuarioAutenticado usuario) {
        while (true) {
            io.titulo("Olá, " + usuario.nome() + "\nPerfil: " + usuario.perfil());
            switch (usuario.perfil()) {
                case "SECRETARIA" -> io.linha("""
                        1 - Gerenciar alunos
                        2 - Gerenciar professores
                        3 - Gerenciar disciplinas
                        4 - Gerenciar cursos
                        5 - Gerenciar semestres e currículos
                        6 - Consultar matrículas por disciplina
                        7 - Consultar situação das disciplinas""");
                case "ALUNO" -> io.linha("""
                        1 - Consultar semestres e disciplinas disponíveis
                        2 - Realizar matrícula
                        3 - Consultar minhas matrículas no semestre
                        4 - Consultar detalhes das minhas disciplinas
                        5 - Cancelar matrícula
                        6 - Consultar meu histórico""");
                case "PROFESSOR" -> io.linha("""
                        1 - Consultar minhas disciplinas
                        2 - Consultar alunos de uma disciplina
                        3 - Consultar semestres e currículos""");
                default -> {
                    io.linha("Perfil sem menu disponível.");
                    return;
                }
            }
            io.linha("9 - Meu perfil\n0 - Sair da conta");
            int opcao = io.opcao();
            if (opcao == 0)
                return;
            if (opcao == 9) {
                acao(this::menuMeuPerfil);
                usuario = api.buscar("/api/auth/me", UsuarioAutenticado.class);
                continue;
            }
            String perfil = usuario.perfil();
            acao(() -> {
                switch (perfil) {
                    case "SECRETARIA" -> secretaria(opcao);
                    case "ALUNO" -> aluno(opcao);
                    case "PROFESSOR" -> professor(opcao);
                    default -> opcaoInvalida();
                }
            });
        }
    }

    private void menuMeuPerfil() {
        while (true) {
            var usuario = api.buscar("/api/auth/me", UsuarioAutenticado.class);
            tela.perfil(usuario);
            io.linha("\n  1 - Alterar meus dados\n  0 - Voltar");
            int opcao = io.opcao();
            if (opcao == 0)
                return;
            acao(() -> {
                if (opcao != 1) {
                    opcaoInvalida();
                    return;
                }
                io.titulo("ALTERAR MEUS DADOS");
                io.linha("Pressione Enter para manter o valor atual ou /voltar para cancelar.");
                String nome = editarTexto("Nome", usuario.nome());
                String login = editarTexto("Login", usuario.login());
                String email = editarTexto("E-mail", usuario.email());
                String senha = io.senha("Nova senha (Enter para manter): ");
                if (!senha.isEmpty() && !senha.equals(io.senha("Confirme a nova senha: "))) {
                    io.linha("As senhas não conferem. Nenhuma alteração foi salva.");
                    return;
                }
                var dados = new PerfilRequest(nome, login, email, senha.isEmpty() ? null : senha);
                if (io.confirmar("Salvar alterações do meu perfil?")) {
                    api.salvar("PUT", "/api/auth/me", dados, UsuarioAutenticado.class);
                    io.linha("Perfil atualizado com sucesso.");
                }
            });
        }
    }

    private String editarTexto(String campo, String atual) {
        String novo = io.texto(campo + " [" + (atual == null ? "Não informado" : atual) + "]: ");
        return novo.isEmpty() ? atual : novo;
    }

    private void secretaria(int opcao) {
        switch (opcao) {
            case 1 -> cadastro("ALUNOS", "/api/alunos", AlunoResponse.class, this::formAluno, tela::aluno);
            case 2 -> cadastro("PROFESSORES", "/api/professores", ProfessorResponse.class, this::formProfessor,
                    tela::professor);
            case 3 -> cadastro("DISCIPLINAS", "/api/disciplinas", DisciplinaResponse.class, this::formDisciplina,
                    tela::disciplina);
            case 4 -> cadastro("CURSOS", "/api/cursos", CursoResponse.class, this::formCurso, tela::curso);
            case 5 -> menuSemestres();
            case 6 -> {
                long semestreId = escolherSemestre();
                long disciplinaId = io.id("ID da disciplina: ");
                tela.lista(api.listar("/api/disciplinas/" + disciplinaId + "/matriculas?semestreId=" + semestreId,
                        MatriculaResponse.class), tela::matricula);
            }
            case 7 -> consultarSituacao();
            default -> opcaoInvalida();
        }
    }

    private void aluno(int opcao) {
        switch (opcao) {
            case 1 -> escolherSemestre();
            case 2 -> {
                long semestreId = escolherSemestre();
                var dados = new MatriculaRequest(semestreId,
                        io.ids("IDs das obrigatórias, separados por vírgula: "),
                        io.ids("IDs das optativas, separados por vírgula (Enter para nenhuma): "));
                if (io.confirmar("Confirmar inscrição nas disciplinas selecionadas?")) {
                    api.executar("POST", "/api/matriculas", dados);
                    io.linha("Inscrição concluída. Suas matrículas atuais:");
                    minhasMatriculas(semestreId);
                }
            }
            case 3 -> minhasMatriculas(escolherSemestre());
            case 4 -> tela.lista(api.listar("/api/matriculas/me/disciplinas?semestreId=" + escolherSemestre(),
                    DisciplinaResponse.class), tela::disciplina);
            case 5 -> {
                minhasMatriculas(escolherSemestre());
                long id = io.id("ID da matrícula a cancelar (não o ID da disciplina): ");
                if (io.confirmar("Cancelar a matrícula " + id + "?")) {
                    api.executar("DELETE", "/api/matriculas/" + id, null);
                    io.linha("Matrícula cancelada.");
                }
            }
            case 6 -> tela.lista(api.listar("/api/matriculas/me/historico", MatriculaResponse.class), tela::matricula);
            default -> opcaoInvalida();
        }
    }

    private void professor(int opcao) {
        switch (opcao) {
            case 1 -> minhasDisciplinas();
            case 2 -> {
                minhasDisciplinas();
                long disciplinaId = io.id("ID da sua disciplina: ");
                long semestreId = escolherSemestre();
                tela.lista(api.listar(
                        "/api/professores/me/disciplinas/" + disciplinaId + "/alunos?semestreId=" + semestreId,
                        AlunoResponse.class), tela::aluno);
            }
            case 3 -> escolherSemestre();
            default -> opcaoInvalida();
        }
    }

    private void minhasDisciplinas() {
        tela.lista(api.listar("/api/professores/me/disciplinas", DisciplinaResponse.class), tela::disciplina);
    }

    private void minhasMatriculas(long semestreId) {
        tela.lista(api.listar("/api/matriculas/me?semestreId=" + semestreId, MatriculaResponse.class), tela::matricula);
    }

    private <T, R> void cadastro(String titulo, String rota, Class<T> tipo, Supplier<R> formulario,
            Consumer<T> apresentar) {
        while (true) {
            io.titulo(titulo);
            io.linha("  1 - Listar\n  2 - Consultar por ID\n  3 - Cadastrar\n  4 - Atualizar\n  5 - Excluir\n\n  0 - Voltar");
            int opcao = io.opcao();
            if (opcao == 0)
                return;
            acao(() -> {
                switch (opcao) {
                    case 1 -> tela.lista(api.listar(rota, tipo), apresentar);
                    case 2 -> apresentar.accept(api.buscar(rota + "/" + io.id("ID: "), tipo));
                    case 3 -> {
                        R dados = formulario.get();
                        if (io.confirmar("Confirmar cadastro?")) {
                            apresentar.accept(api.salvar("POST", rota, dados, tipo));
                            io.linha("Cadastro realizado.");
                        }
                    }
                    case 4 -> {
                        long id = io.id("ID a atualizar: ");
                        apresentar.accept(api.buscar(rota + "/" + id, tipo));
                        io.linha("Preencha todos os novos dados; para usuários, informe também a senha desejada.");
                        R dados = formulario.get();
                        if (io.confirmar("Confirmar atualização?")) {
                            apresentar.accept(api.salvar("PUT", rota + "/" + id, dados, tipo));
                            io.linha("Cadastro atualizado.");
                        }
                    }
                    case 5 -> {
                        long id = io.id("ID a excluir: ");
                        apresentar.accept(api.buscar(rota + "/" + id, tipo));
                        if (io.confirmar("Excluir este cadastro?")) {
                            api.executar("DELETE", rota + "/" + id, null);
                            io.linha("Cadastro excluído.");
                        }
                    }
                    default -> opcaoInvalida();
                }
            });
        }
    }

    private void menuSemestres() {
        while (true) {
            io.titulo("SEMESTRES E CURRÍCULOS");
            io.linha("""
                    1 - Listar semestres
                    2 - Consultar currículo
                    3 - Cadastrar semestre
                    4 - Definir currículo
                    5 - Consultar situação das disciplinas
                    6 - Encerrar período de matrícula
                    7 - Concluir semestre letivo
                    0 - Voltar""");
            int opcao = io.opcao();
            if (opcao == 0)
                return;
            acao(() -> {
                switch (opcao) {
                    case 1 -> listarSemestres();
                    case 2 -> escolherSemestre();
                    case 3 -> {
                        var dados = new SemestreRequest(io.texto("Nome do semestre (ex.: 2026/2): "));
                        if (io.confirmar("Cadastrar semestre?")) {
                            tela.semestre(api.salvar("POST", "/api/semestres", dados, SemestreResponse.class));
                        }
                    }
                    case 4 -> {
                        long id = escolherSemestre();
                        tela.lista(api.listar("/api/disciplinas", DisciplinaResponse.class), tela::disciplina);
                        var dados = new CurriculoRequest(io.ids("IDs de TODAS as disciplinas do novo currículo: "));
                        if (io.confirmar("Substituir o currículo por esta seleção?")) {
                            tela.semestre(api.salvar("PUT", "/api/semestres/" + id + "/curriculo", dados,
                                    SemestreResponse.class));
                        }
                    }
                    case 5 -> consultarSituacao();
                    case 6 -> {
                        long id = escolherSemestre();
                        if (io.confirmar("Encerrar matrículas e cancelar ofertas com menos de três alunos?")) {
                            tela.semestre(api.salvar("POST", "/api/semestres/" + id + "/encerrar-matriculas", null,
                                    SemestreResponse.class));
                        }
                    }
                    case 7 -> {
                        long id = escolherSemestre();
                        if (io.confirmar("Concluir o semestre letivo e disponibilizá-lo no histórico?")) {
                            tela.semestre(api.salvar("POST", "/api/semestres/" + id + "/concluir", null,
                                    SemestreResponse.class));
                        }
                    }
                    default -> opcaoInvalida();
                }
            });
        }
    }

    private void listarSemestres() {
        tela.lista(api.listar("/api/semestres", SemestreResponse.class), tela::resumoSemestre);
    }

    private long escolherSemestre() {
        listarSemestres();
        long id = io.id("ID do semestre (/voltar para cancelar): ");
        tela.semestre(api.buscar("/api/semestres/" + id, SemestreResponse.class));
        return id;
    }

    private void consultarSituacao() {
        long id = escolherSemestre();
        tela.lista(api.listar("/api/semestres/" + id + "/situacao-disciplinas", SituacaoDisciplinaResponse.class),
                tela::situacao);
    }

    private AlunoRequest formAluno() {
        return new AlunoRequest(io.texto("Nome: "), io.texto("Login: "), io.senha("Senha: "),
                io.texto("E-mail: "), escolherCurso());
    }

    private long escolherCurso() {
        io.titulo("SELEÇÃO DE CURSO");
        while (true) {
            var cursos = api.listar("/api/cursos", CursoResponse.class);
            if (cursos.isEmpty()) {
                io.linha("Nenhum curso cadastrado. Cadastre um curso antes de cadastrar o aluno.");
                throw new TerminalIO.OperacaoCancelada();
            }
            io.linha("Cursos disponíveis:");
            tela.lista(cursos, curso -> io.linha("[" + curso.id() + "] " + curso.nome()));
            long id = io.id("Digite o ID do curso desejado: ");
            try {
                return api.buscar("/api/cursos/" + id, CursoResponse.class).id();
            } catch (ApiException ex) {
                if (ex.status() != 404)
                    throw ex;
                io.linha("Curso inválido.\nEscolha um dos cursos disponíveis.");
            }
        }
    }

    private ProfessorRequest formProfessor() {
        return new ProfessorRequest(io.texto("Nome: "), io.texto("Login: "), io.senha("Senha: "),
                io.texto("E-mail: "), io.texto("Titulação: "));
    }

    private DisciplinaRequest formDisciplina() {
        tela.lista(api.listar("/api/professores", ProfessorResponse.class), tela::professor);
        return new DisciplinaRequest(io.texto("Código: "), io.texto("Nome: "), io.inteiro("Créditos: "),
                io.id("ID do professor responsável: "));
    }

    private CursoRequest formCurso() {
        tela.lista(api.listar("/api/disciplinas", DisciplinaResponse.class), tela::disciplina);
        return new CursoRequest(io.texto("Nome: "), io.inteiro("Quantidade de créditos: "),
                io.ids("IDs das disciplinas, separados por vírgula (Enter para nenhuma): "));
    }

    private void acao(Runnable operacao) {
        try {
            operacao.run();
        } catch (ApiException ex) {
            if (ex.status() == 401)
                throw ex;
            io.linha(ex.getMessage());
        } catch (TerminalIO.OperacaoCancelada ex) {
            io.linha("Operação cancelada.");
        }
    }

    private void opcaoInvalida() {
        io.linha("Opção inválida. Escolha uma das opções exibidas.");
    }
}
