package com.app.matricula_mais.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NotificacaoCobranca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long alunoId;
    private Long semestreId;
    private String semestre;
    @ElementCollection
    private List<Long> disciplinaIds = new ArrayList<>();
    private LocalDateTime criadaEm = LocalDateTime.now();
    private LocalDateTime enviadaEm;
    private int tentativas;
}
