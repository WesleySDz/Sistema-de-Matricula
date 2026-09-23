package com.app.matricula_mais.model;

import java.time.LocalDateTime;

public class Usuario {

    private Long id;
    private String nome;
    private String login;
    private String senha;
    private String email;
    private LocalDateTime ultimoAcesso;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
