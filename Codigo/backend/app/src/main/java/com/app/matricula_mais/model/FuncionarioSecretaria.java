package com.app.matricula_mais.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FuncionarioSecretaria extends Usuario {

    public FuncionarioSecretaria() {
    }

    public FuncionarioSecretaria(Long id, String nome, String login, String senha, String email) {
        super(id, nome, login, senha, email);
    }

    // As operações da secretaria são executadas pelos controllers e services transacionais.
}
