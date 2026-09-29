package com.app.matricula_mais.dto;

import java.time.LocalDate;
import java.util.List;
import com.app.matricula_mais.model.*;

public final class Respostas {
    private Respostas() { }

    public record AlunoResponse(Long id, String nome, String login, String email, String matricula, String curso) {
        public static AlunoResponse de(Aluno aluno) {
            return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getLogin(), aluno.getEmail(),
                aluno.getMatricula(), aluno.getCurso());
        }
    }

    public record ProfessorResponse(Long id, String nome, String login, String email, String titulacao) {
        public static ProfessorResponse de(Professor professor) {
            return new ProfessorResponse(professor.getId(), professor.getNome(), professor.getLogin(),
                professor.getEmail(), professor.getTitulacao());
        }
    }

    public record DisciplinaResponse(Long id, String codigo, String nome, int creditos,
            Long professorId, String professorNome) {
        public static DisciplinaResponse de(Disciplina disciplina) {
            Professor professor = disciplina.getProfessorResponsavel();
            return new DisciplinaResponse(disciplina.getId(), disciplina.getCodigo(), disciplina.getNome(),
                disciplina.getCreditos(), professor == null ? null : professor.getId(),
                professor == null ? null : professor.getNome());
        }
    }

    public record CursoResponse(Long id, String nome, int quantidadeCreditos, List<DisciplinaResponse> disciplinas) {
        public static CursoResponse de(Curso curso) {
            return new CursoResponse(curso.getId(), curso.getNome(), curso.getQuantidadeCreditos(),
                curso.getDisciplinas().stream().map(DisciplinaResponse::de).toList());
        }
    }

    public record SemestreResponse(Long id, String nome, boolean periodoMatriculaAberto, boolean concluido,
            List<DisciplinaResponse> disciplinasOfertadas, List<Long> disciplinasCanceladas) {
        public static SemestreResponse de(Semestre semestre) {
            return new SemestreResponse(semestre.getId(), semestre.getNome(), semestre.isPeriodoMatriculaAberto(),
                semestre.isConcluido(), semestre.getDisciplinasOfertadas().stream().map(DisciplinaResponse::de).toList(),
                semestre.getDisciplinasCanceladas().stream().map(Disciplina::getId).toList());
        }
    }

    public record MatriculaResponse(Long id, Long alunoId, String alunoNome, String semestre,
            DisciplinaResponse disciplina, boolean optativa, boolean ativa, LocalDate dataMatricula) {
        public static MatriculaResponse de(Matricula matricula) {
            return new MatriculaResponse(matricula.getId(), matricula.getAluno().getId(), matricula.getAluno().getNome(),
                matricula.getSemestre(), DisciplinaResponse.de(matricula.getDisciplina()), matricula.isOptativa(),
                matricula.isAtiva(), matricula.getDataMatricula());
        }
    }

    public record SituacaoDisciplinaResponse(DisciplinaResponse disciplina, String situacao, long matriculados) { }
}
