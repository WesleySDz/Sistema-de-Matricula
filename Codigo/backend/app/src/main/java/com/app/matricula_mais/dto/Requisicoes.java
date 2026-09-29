package com.app.matricula_mais.dto;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class Requisicoes {
    private Requisicoes() { }

    public record PerfilRequest(@NotBlank String nome, @NotBlank String login,
            @Email @Size(min = 1) String email, @Size(min = 8, max = 72) String senha) {
        @Override
        public String toString() {
            return "PerfilRequest[credenciais protegidas]";
        }
    }

    public record AlunoRequest(@NotBlank String nome, @NotBlank String login,
            @NotBlank @Size(min = 8, max = 72) String senha, @NotBlank @Email String email,
            @NotNull @Positive Long cursoId) {
        @Override
        public String toString() {
            return "AlunoRequest[credenciais protegidas]";
        }
    }

    public record ProfessorRequest(@NotBlank String nome, @NotBlank String login,
            @NotBlank @Size(min = 8, max = 72) String senha, @NotBlank @Email String email,
            @NotBlank String titulacao) {
        @Override
        public String toString() {
            return "ProfessorRequest[credenciais protegidas]";
        }
    }

    public record CursoRequest(@NotBlank String nome, @Positive int quantidadeCreditos,
            @NotNull List<@NotNull @Positive Long> disciplinaIds) { }

    public record DisciplinaRequest(@NotBlank String codigo, @NotBlank String nome,
            @Positive int creditos, @NotNull @Positive Long professorId) { }

    public record SemestreRequest(@NotBlank String nome) { }

    public record CurriculoRequest(@NotNull List<@NotNull @Positive Long> disciplinaIds) { }

    public record MatriculaRequest(@NotNull @Positive Long semestreId,
            @NotNull @Size(max = 4) List<@NotNull @Positive Long> obrigatorias,
            @NotNull @Size(max = 2) List<@NotNull @Positive Long> optativas) { }
}
