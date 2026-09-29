package com.app.matricula_mais.cli;

/** Falha apresentada pelo servidor ou pelo transporte, sem regras de domínio. */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
