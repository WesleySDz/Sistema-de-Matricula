package com.app.matricula_mais.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.app.matricula_mais.model.NotificacaoCobranca;
import com.app.matricula_mais.repository.NotificacaoCobrancaRepository;

@Service
public class EnvioCobrancaService {
    private static final Logger log = LoggerFactory.getLogger(EnvioCobrancaService.class);
    private final NotificacaoCobrancaRepository notificacoes;
    private final String url;
    private final RestClient cliente;

    public EnvioCobrancaService(NotificacaoCobrancaRepository notificacoes,
            @Value("${app.cobranca.url:}") String url) {
        this.notificacoes = notificacoes;
        this.url = url;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.cliente = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${app.cobranca.intervalo-ms:30000}")
    @Transactional
    public void enviarPendentes() {
        if (url.isBlank()) {
            return;
        }
        for (NotificacaoCobranca notificacao : notificacoes.findTop50ByEnviadaEmIsNullOrderByIdAsc()) {
            notificacao.setTentativas(notificacao.getTentativas() + 1);
            try {
                cliente.post().uri(url).header("Idempotency-Key", "matricula-" + notificacao.getId())
                        .body(new SolicitacaoCobranca(notificacao.getId(), notificacao.getAlunoId(),
                                notificacao.getSemestreId(), notificacao.getSemestre(), notificacao.getDisciplinaIds()))
                        .retrieve().toBodilessEntity();
                notificacao.setEnviadaEm(LocalDateTime.now());
            } catch (RestClientException ex) {
                log.warn("Notificação de cobrança {} permanece pendente ({})", notificacao.getId(),
                        ex.getClass().getSimpleName());
                // Mantém a ordem das atualizações de inscrição durante novas tentativas.
                break;
            }
        }
    }

    public record SolicitacaoCobranca(Long notificacaoId, Long alunoId, Long semestreId,
            String semestre, List<Long> disciplinaIds) {
    }
}
