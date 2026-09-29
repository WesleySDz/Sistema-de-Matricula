package com.app.matricula_mais.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(unique = true)
    private String login;
    private String senha;
    private String email;
    private LocalDateTime ultimoAcesso;
    private int tentativasInvalidas;
    private LocalDateTime bloqueadoAte;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String login, String senha, String email) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.email = email;
    }

    public boolean autenticar(String loginInformado, String senhaInformada) {
        return login != null && login.equals(loginInformado)
                && senha != null && senha.equals(senhaInformada);
    }

    public void registrarAcesso() {
        this.ultimoAcesso = LocalDateTime.now();
    }

}
