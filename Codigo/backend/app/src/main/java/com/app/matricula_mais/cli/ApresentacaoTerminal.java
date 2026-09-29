package com.app.matricula_mais.cli;

import java.util.List;
import java.util.function.Consumer;

import com.app.matricula_mais.dto.Respostas.*;
import com.app.matricula_mais.dto.Respostas.AlunoResponse;
import com.app.matricula_mais.dto.Respostas.CursoResponse;
import com.app.matricula_mais.dto.Respostas.DisciplinaResponse;
import com.app.matricula_mais.dto.Respostas.MatriculaResponse;
import com.app.matricula_mais.dto.Respostas.ProfessorResponse;
import com.app.matricula_mais.dto.Respostas.SemestreResponse;
import com.app.matricula_mais.dto.Respostas.SituacaoDisciplinaResponse;
import com.app.matricula_mais.security.UsuarioAutenticado;

public class ApresentacaoTerminal {
    private final TerminalIO io;

    public ApresentacaoTerminal(TerminalIO io) {
        this.io = io;
    }

    public <T> void lista(List<T> registros, Consumer<T> apresentar) {
        if (registros.isEmpty())
            io.linha("Nenhum registro encontrado.");
        registros.forEach(apresentar);
    }

    public void aluno(AlunoResponse aluno) {
        io.linha("ID: " + aluno.id() + " | " + aluno.nome() + " | Matrícula: " + aluno.matricula()
                + " | Curso: " + aluno.curso() + " | Login: " + aluno.login() + " | E-mail: " + aluno.email());
    }

    public void professor(ProfessorResponse professor) {
        io.linha("ID: " + professor.id() + " | " + professor.nome() + " | Titulação: " + professor.titulacao()
                + " | Login: " + professor.login() + " | E-mail: " + professor.email());
    }

    public void disciplina(DisciplinaResponse disciplina) {
        io.linha("ID: " + disciplina.id() + " | " + disciplina.codigo() + " - " + disciplina.nome()
                + " | Créditos: " + disciplina.creditos() + " | Professor: " + valor(disciplina.professorNome())
                + " (ID " + valor(disciplina.professorId()) + ")");
    }

    public void curso(CursoResponse curso) {
        io.linha("ID: " + curso.id() + " | Curso: " + curso.nome() + " | Créditos: " + curso.quantidadeCreditos());
        io.linha("Disciplinas do curso:");
        lista(curso.disciplinas(), this::disciplina);
    }

    public void resumoSemestre(SemestreResponse semestre) {
        io.linha("ID: " + semestre.id() + " | Semestre: " + semestre.nome()
                + " | Matrículas: " + (semestre.periodoMatriculaAberto() ? "ABERTAS" : "ENCERRADAS")
                + " | Período letivo: " + (semestre.concluido() ? "CONCLUÍDO" : "EM ANDAMENTO"));
    }

    public void semestre(SemestreResponse semestre) {
        resumoSemestre(semestre);
        io.linha("Currículo:");
        lista(semestre.disciplinasOfertadas(), disciplina -> {
            disciplina(disciplina);
            io.linha("  Oferta: "
                    + (semestre.disciplinasCanceladas().contains(disciplina.id()) ? "CANCELADA" : "ATIVA"));
        });
    }

    public void matricula(MatriculaResponse matricula) {
        io.linha("Matrícula ID: " + matricula.id() + " | Aluno: " + matricula.alunoNome()
                + " (ID " + matricula.alunoId() + ") | Semestre: " + matricula.semestre()
                + " | " + (matricula.optativa() ? "OPTATIVA" : "OBRIGATÓRIA")
                + " | " + (matricula.ativa() ? "ATIVA" : "CANCELADA") + " | Data: " + matricula.dataMatricula());
        disciplina(matricula.disciplina());
    }

    public void situacao(SituacaoDisciplinaResponse situacao) {
        disciplina(situacao.disciplina());
        io.linha("  Situação: " + situacao.situacao() + " | Alunos matriculados: " + situacao.matriculados());
    }

    public void perfil(UsuarioAutenticado usuario) {
        io.titulo("MEU PERFIL");
        io.linha("Nome: " + usuario.nome());
        io.linha("ID: " + usuario.id());
        io.linha("Login: " + usuario.login());
        io.linha("E-mail: " + valor(usuario.email()));
        io.linha("Perfil: " + usuario.perfil());
    }

    private String valor(Object valor) {
        return valor == null ? "Não informado" : valor.toString();
    }
}
