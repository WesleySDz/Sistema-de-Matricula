package com.app.matricula_mais.cli;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import com.app.matricula_mais.security.UsuarioAutenticado;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Cliente da API existente. Não acessa services, entidades ou repositories. */
public class ApiClient implements AutoCloseable {
    private final URI base;
    private final ObjectMapper json;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    private final HttpClient http;
    private Csrf csrf;

    public ApiClient(URI base, ObjectMapper json) {
        this.base = base;
        this.json = json;
        this.http = HttpClient.newBuilder().cookieHandler(cookies)
            .connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
    }

    public UsuarioAutenticado login(String login, String senha) {
        limparSessao();
        try {
            renovarCsrf();
            enviar("POST", "/api/auth/login", "login=" + codificar(login) + "&senha=" + codificar(senha),
                "application/x-www-form-urlencoded");
            renovarCsrf();
            return buscar("/api/auth/me", UsuarioAutenticado.class);
        } catch (ApiException ex) {
            limparSessao();
            if (ex.status() == 401) {
                throw new ApiException(401, "Login ou senha inválidos.");
            }
            throw ex;
        }
    }

    public void logout() {
        try {
            if (csrf != null) {
                enviar("POST", "/api/auth/logout", "", "application/x-www-form-urlencoded");
            }
        } finally {
            limparSessao();
        }
    }

    public <T> T buscar(String rota, Class<T> tipo) {
        return ler(enviar("GET", rota, null, null), tipo);
    }

    public <T> List<T> listar(String rota, Class<T> tipo) {
        String corpo = enviar("GET", rota, null, null);
        try {
            return json.readValue(corpo, json.getTypeFactory().constructCollectionType(List.class, tipo));
        } catch (JacksonException ex) {
            throw respostaInvalida();
        }
    }

    public <T> T salvar(String metodo, String rota, Object dados, Class<T> tipo) {
        return ler(enviar(metodo, rota, dados == null ? "" : json.writeValueAsString(dados), "application/json"), tipo);
    }

    public void executar(String metodo, String rota, Object dados) {
        enviar(metodo, rota, dados == null ? "" : json.writeValueAsString(dados), "application/json");
    }

    private void renovarCsrf() {
        csrf = buscar("/api/auth/csrf", Csrf.class);
    }

    private String enviar(String metodo, String rota, String corpo, String contentType) {
        HttpRequest.Builder request = HttpRequest.newBuilder(base.resolve(rota)).timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json");
        if (contentType != null) {
            request.header("Content-Type", contentType);
        }
        if (!metodo.equals("GET") && csrf != null) {
            request.header(csrf.headerName(), csrf.token());
        }
        request.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            if (response.statusCode() == 403 && !metodo.equals("GET")) {
                // CSRF pode falhar antes da autorização quando a sessão expirou.
                // Verifica a sessão, sem repetir uma operação de escrita.
                enviar("GET", "/api/auth/me", null, null);
            }
            throw new ApiException(response.statusCode(), mensagemErro(response.statusCode(), response.body()));
        } catch (IOException ex) {
            throw new ApiException(0, "Não foi possível comunicar com o sistema. Verifique se a aplicação está disponível.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Operação interrompida.");
        }
    }

    private String mensagemErro(int status, String corpo) {
        if (status == 401) return "Sessão expirada ou não autenticada. Entre novamente.";
        if (status == 403) return "Seu perfil não tem permissão para essa operação.";
        if (status == 423) return "Conta bloqueada temporariamente. Tente novamente após 15 minutos.";
        if (status >= 500) return "O sistema não conseguiu concluir a operação. Tente novamente.";
        try {
            JsonNode erro = json.readTree(corpo);
            if (erro != null && erro.hasNonNull("detail")) {
                StringBuilder mensagem = new StringBuilder(erro.get("detail").asText());
                if (erro.has("campos") && erro.get("campos").isArray()) {
                    erro.get("campos").forEach(campo -> mensagem.append("\n- ").append(campo.asText()));
                }
                return mensagem.toString();
            }
        } catch (JacksonException ignored) {
            // Uma resposta sem JSON ainda deve produzir uma mensagem utilizável.
        }
        return "Não foi possível concluir a operação (HTTP " + status + ").";
    }

    private <T> T ler(String corpo, Class<T> tipo) {
        try {
            return json.readValue(corpo, tipo);
        } catch (JacksonException ex) {
            throw respostaInvalida();
        }
    }

    private ApiException respostaInvalida() {
        return new ApiException(0, "O sistema retornou uma resposta em formato inesperado.");
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private void limparSessao() {
        cookies.getCookieStore().removeAll();
        csrf = null;
    }

    @Override
    public void close() {
        limparSessao();
        http.close();
    }

    public record Csrf(String headerName, String parameterName, String token) { }
}
