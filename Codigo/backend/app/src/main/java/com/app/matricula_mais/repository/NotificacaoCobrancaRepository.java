package com.app.matricula_mais.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.app.matricula_mais.model.NotificacaoCobranca;

public interface NotificacaoCobrancaRepository extends JpaRepository<NotificacaoCobranca, Long> {
    List<NotificacaoCobranca> findTop50ByEnviadaEmIsNullOrderByIdAsc();
}
