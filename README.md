# Sistema de Matrícula

Sistema para apoiar a secretaria na gestão acadêmica, os alunos na realização e no acompanhamento de matrículas e os professores na consulta de suas disciplinas e dos alunos matriculados.

## Documentos de referência

- [Requisitos funcionais, requisitos não funcionais e regras de negócio](Artefatos/Requisitos/requisitos_e_regras.pdf).
- [Diagrama de casos de uso](Artefatos/Diagramas/diagrama_de_caso_de_uso.pdf).

## Histórias de usuário

As histórias seguem o formato **Como [perfil], quero [funcionalidade], para [benefício]**. Cada história está associada ao requisito funcional e ao caso de uso correspondente ou proposto (HU-001 a HU-018).
### Perfis e relações entre os casos de uso

| Perfil ou ator | Responsabilidades |
| --- | --- |
| Usuário | Autenticar-se no sistema. É o perfil comum a alunos, professores e funcionários da secretaria. |
| Secretaria | Gerenciar os cadastros acadêmicos, definir o currículo do semestre e consultar matrículas e a situação das disciplinas. |
| Aluno | Realizar e cancelar matrículas, selecionar disciplinas e consultar suas matrículas, seu histórico e os dados das disciplinas. |
| Professor | Consultar as disciplinas sob sua responsabilidade e os respectivos alunos matriculados. |
| Sistema de cobranças | Ator externo que recebe a notificação da inscrição do aluno no semestre para realizar a cobrança das disciplinas. |

As histórias HU-002 a HU-018 pressupõem que o usuário esteja autenticado no perfil indicado. Os requisitos não funcionais apresentados ao final complementam os critérios de aceitação das histórias às quais se aplicam.

### Usuário

#### HU-001 — Autenticar-se no sistema

**Como** aluno, professor ou funcionário da secretaria, **quero** acessar o sistema com login e senha, **para** utilizar as funcionalidades do meu perfil.

**Requisito:** RF-001 · **Caso de uso:** Autenticar · **Prioridade:** Alta.

**Critérios de aceitação:**

1. O sistema deve permitir a autenticação dos três perfis mediante login e senha válidos.
2. Credenciais inválidas devem impedir a autenticação.
3. Após cinco tentativas consecutivas de autenticação inválida, o acesso à conta deve ser bloqueado por 15 minutos, conforme RNF-007.
4. A sessão deve ser encerrada após 30 minutos consecutivos sem interação do usuário, conforme RNF-003.

### Secretaria

#### HU-002 — Gerenciar alunos

**Como** funcionário da secretaria, **quero** cadastrar, consultar, atualizar e excluir alunos, **para** manter os registros dos estudantes atualizados.

**Requisito:** RF-002 · **Caso de uso:** Gerenciar alunos · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir cadastrar um aluno no sistema.
2. A secretaria deve conseguir consultar as informações de alunos cadastrados.
3. A secretaria deve conseguir atualizar as informações de um aluno e consultar os dados atualizados.
4. A secretaria deve conseguir excluir o cadastro de um aluno.

#### HU-003 — Gerenciar professores

**Como** funcionário da secretaria, **quero** cadastrar, consultar, atualizar e excluir professores, **para** manter os registros do corpo docente atualizados.

**Requisito:** RF-003 · **Caso de uso:** Gerenciar professores · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir cadastrar um professor no sistema.
2. A secretaria deve conseguir consultar as informações de professores cadastrados.
3. A secretaria deve conseguir atualizar as informações de um professor e consultar os dados atualizados.
4. A secretaria deve conseguir excluir o cadastro de um professor.

#### HU-004 — Gerenciar disciplinas

**Como** funcionário da secretaria, **quero** cadastrar, consultar, atualizar e excluir disciplinas, **para** manter o cadastro das disciplinas da instituição atualizado.

**Requisito:** RF-004 · **Caso de uso:** Gerenciar disciplinas · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir cadastrar uma disciplina no sistema.
2. A secretaria deve conseguir consultar as informações de disciplinas cadastradas.
3. A secretaria deve conseguir atualizar as informações de uma disciplina e consultar os dados atualizados.
4. A secretaria deve conseguir excluir o cadastro de uma disciplina.

#### HU-005 — Gerenciar cursos

**Como** funcionário da secretaria, **quero** cadastrar, atualizar e excluir cursos, incluindo seu nome e sua quantidade de créditos, **para** manter as informações dos cursos da instituição atualizadas.

**Requisito:** RF-005 · **Caso de uso:** Gerenciar cursos · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir cadastrar um curso com nome e quantidade de créditos.
2. A secretaria deve conseguir editar e salvar alterações nas informações de um curso, incluindo nome e quantidade de créditos.
3. A secretaria deve conseguir excluir o cadastro de um curso.

#### HU-006 — Consultar matrículas por disciplina

**Como** funcionário da secretaria, **quero** consultar as matrículas realizadas pelos alunos em cada disciplina, **para** acompanhar a ocupação das disciplinas.

**Requisito:** RF-006 · **Caso de uso:** Consultar matrículas · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir consultar as matrículas de uma disciplina.
2. A consulta deve permitir identificar os alunos matriculados na disciplina consultada.
3. Quando a disciplina não possuir matrículas, a consulta deve indicar essa situação.

#### HU-007 — Consultar a situação das disciplinas

**Como** funcionário da secretaria, **quero** consultar quais disciplinas estão ativas ou canceladas para o semestre, **para** acompanhar quais ofertas serão mantidas.

**Requisito:** RF-007 · **Caso de uso:** Consultar situação das disciplinas · **Prioridade:** Média.

**Critérios de aceitação:**

1. A consulta deve apresentar a situação das disciplinas, identificando as ativas e as canceladas para o semestre.
2. Ao término do período de matrícula, uma disciplina deve possuir pelo menos três alunos matriculados para atender ao mínimo necessário à oferta no semestre seguinte, conforme RN-005.
3. Uma disciplina com menos de três alunos matriculados ao término desse período deve constar como cancelada e não deve ocorrer no semestre seguinte, conforme RN-006.

#### HU-008 — Gerar o currículo do semestre

**Como** funcionário da secretaria, **quero** definir as disciplinas que serão ofertadas em cada semestre, **para** organizar a oferta acadêmica disponível para matrícula.

**Requisito:** RF-008 · **Caso de uso:** Gerar o currículo do semestre · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A secretaria deve conseguir definir as disciplinas que compõem a oferta de um semestre.
2. As disciplinas definidas devem compor o currículo do semestre correspondente.
3. Ao término do período de matrícula, a manutenção das ofertas para o semestre seguinte deve respeitar o mínimo de três alunos e o cancelamento das disciplinas abaixo desse mínimo, conforme RN-005 e RN-006.

### Aluno

#### HU-009 — Realizar matrícula em disciplinas

**Como** aluno, **quero** me matricular nas disciplinas disponíveis durante o período de matrícula, **para** cursá-las no semestre.

**Requisito:** RF-009 · **Caso de uso:** Realizar matrícula · **Prioridade:** Alta.

**Critérios de aceitação:**

1. O aluno deve conseguir realizar matrícula em disciplinas disponíveis enquanto o período de matrícula estiver aberto; fora desse período, novas matrículas devem ser impedidas, conforme RN-003.
2. O processo deve incluir a seleção de disciplinas obrigatórias descrita na HU-011 e permitir a seleção opcional de disciplinas optativas descrita na HU-012, respeitando RN-001 e RN-002.
3. A quantidade de alunos matriculados em uma disciplina não pode ultrapassar 60. Uma nova matrícula em uma disciplina que já tenha 60 alunos deve ser impedida, conforme RN-007.
4. Uma matrícula realizada com sucesso deve aparecer na consulta de matrículas do aluno, descrita na HU-013.
5. Após a conclusão da inscrição do aluno no semestre, o sistema de matrículas deve notificar o sistema de cobranças, conforme a HU-018.

#### HU-010 — Cancelar matrícula

**Como** aluno, **quero** cancelar uma matrícula realizada anteriormente durante o período permitido, **para** ajustar as disciplinas que pretendo cursar.

**Requisito:** RF-010 · **Caso de uso:** Cancelar matrícula · **Prioridade:** Alta.

**Critérios de aceitação:**

1. O aluno deve conseguir cancelar uma de suas matrículas enquanto o período de matrícula estiver aberto, conforme RN-004.
2. O sistema deve impedir o cancelamento fora do período de matrícula.
3. Após o cancelamento, a disciplina deve deixar de constar entre aquelas em que o aluno está matriculado no semestre.
4. O aluno cuja matrícula foi cancelada deve deixar de ser contabilizado entre os matriculados naquela disciplina.

#### HU-011 — Selecionar disciplinas obrigatórias

**Como** aluno, **quero** selecionar até quatro disciplinas obrigatórias como primeira opção no semestre, **para** definir minhas escolhas principais de matrícula.

**Requisito:** RF-011 · **Caso de uso:** Selecionar disciplinas obrigatórias · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A seleção de disciplinas obrigatórias deve integrar o processo de realização de matrícula, conforme a relação `<<include>>` do diagrama.
2. O aluno deve conseguir selecionar até quatro disciplinas como primeira opção para o semestre, conforme RN-001.
3. O sistema deve impedir a seleção de uma quinta disciplina como primeira opção.
4. A matrícula nas disciplinas selecionadas deve respeitar o período permitido e a capacidade máxima da disciplina, conforme RN-003 e RN-007.

#### HU-012 — Selecionar disciplinas optativas

**Como** aluno, **quero** selecionar até duas disciplinas alternativas ou optativas, **para** complementar minhas escolhas de disciplinas para o semestre.

**Requisito:** RF-012 · **Caso de uso:** Selecionar disciplinas optativas · **Prioridade:** Alta.

**Critérios de aceitação:**

1. A seleção de disciplinas optativas deve ser opcional durante a realização da matrícula, conforme a relação `<<extend>>` do diagrama.
2. O aluno deve conseguir selecionar até duas disciplinas alternativas ou optativas, além das disciplinas obrigatórias, conforme RN-002.
3. O sistema deve impedir a seleção de uma terceira disciplina alternativa ou optativa.
4. A ausência de disciplinas optativas selecionadas deve permitir a continuidade do processo de matrícula.

#### HU-013 — Consultar minhas matrículas

**Como** aluno, **quero** consultar as disciplinas nas quais estou matriculado no semestre, **para** acompanhar minhas matrículas atuais.

**Requisito:** RF-013 · **Caso de uso:** Consultar suas matrículas · **Prioridade:** Média.

**Critérios de aceitação:**

1. A consulta deve apresentar as disciplinas em que o aluno autenticado está matriculado no semestre.
2. As informações devem refletir as matrículas realizadas e os cancelamentos efetuados.
3. Quando o aluno não possuir matrículas no semestre, a consulta deve indicar essa situação.

#### HU-014 — Consultar dados das disciplinas

**Como** aluno, **quero** visualizar informações detalhadas das disciplinas, **para** conhecer suas características e apoiar minhas escolhas de matrícula.

**Requisito:** RF-014 · **Caso de uso:** Consultar dados da disciplina · **Prioridade:** Baixa.

**Critérios de aceitação:**

1. O aluno deve conseguir consultar os dados de uma disciplina.
2. A consulta deve apresentar o número de créditos e o professor responsável pela disciplina.
3. As informações exibidas devem corresponder aos dados cadastrados da disciplina consultada.

#### HU-015 — Consultar histórico de matrículas

**Como** aluno, **quero** consultar as disciplinas em que estive matriculado em semestres anteriores, **para** acompanhar meu histórico de matrículas.

**Requisito:** RF-015 · **Caso de uso:** Consultar histórico de matrículas · **Prioridade:** Baixa.

**Critérios de aceitação:**

1. O aluno deve conseguir consultar suas matrículas de semestres anteriores.
2. A consulta deve permitir identificar as disciplinas e os respectivos semestres das matrículas.
3. Quando o aluno não possuir matrículas anteriores, a consulta deve indicar essa situação.

### Professor

#### HU-016 — Consultar minhas disciplinas

**Como** professor, **quero** consultar as disciplinas pelas quais sou responsável, **para** acompanhar minhas atribuições de ensino.

**Requisito:** RF-016 · **Caso de uso:** Consultar suas disciplinas · **Prioridade:** Média.

**Critérios de aceitação:**

1. A consulta deve apresentar as disciplinas sob responsabilidade do professor autenticado.
2. A listagem deve permitir identificar cada disciplina atribuída ao professor.
3. Quando o professor não possuir disciplinas sob sua responsabilidade, a consulta deve indicar essa situação.

#### HU-017 — Visualizar alunos matriculados

**Como** professor, **quero** visualizar os alunos matriculados em cada disciplina sob minha responsabilidade, **para** conhecer os estudantes que participarão das minhas aulas.

**Requisito:** RF-017 · **Caso de uso:** Visualizar alunos matriculados · **Prioridade:** Alta.

**Critérios de aceitação:**

1. O professor deve conseguir consultar os alunos matriculados em uma de suas disciplinas.
2. A consulta deve apresentar os alunos matriculados na disciplina consultada, refletindo as matrículas realizadas e os cancelamentos efetuados.
3. Quando a disciplina não possuir alunos matriculados, a consulta deve indicar essa situação.

### Integração com o sistema de cobranças

#### HU-018 — Solicitar geração de cobrança

**Como** aluno, **quero** que minha inscrição no semestre seja comunicada automaticamente ao sistema de cobranças, **para** que eu possa ser cobrado pelas disciplinas em que me matriculei naquele semestre.

**Requisito:** RF-018 · **Caso de uso proposto:** Solicitar geração de cobrança · **Prioridade:** Alta.

**Descrição do RF-018:** Após a conclusão da inscrição de um aluno no semestre, o sistema de matrículas deve notificar o sistema de cobranças para possibilitar a cobrança das disciplinas daquele semestre.

Este requisito complementa os documentos de referência e ainda deve ser incorporado ao PDF de requisitos e ao diagrama de casos de uso.

**Critérios de aceitação:**

1. A conclusão da inscrição do aluno no semestre deve disparar automaticamente a notificação ao sistema de cobranças.
2. A notificação deve permitir identificar o aluno, o semestre e as disciplinas em que ele efetivamente se matriculou.
3. Uma inscrição que não tenha sido concluída não deve disparar a notificação de conclusão ao sistema de cobranças.
4. O envio deve ocorrer como parte do fluxo de matrícula, sem exigir uma ação adicional do aluno para solicitar a notificação.

## Regras de negócio associadas às histórias

| Regra | Descrição | Histórias relacionadas |
| --- | --- | --- |
| RN-001 | Cada aluno pode selecionar no máximo quatro disciplinas obrigatórias como primeira opção durante o período de matrícula. | HU-009 e HU-011 |
| RN-002 | Cada aluno pode selecionar no máximo duas disciplinas alternativas ou optativas, além das obrigatórias. | HU-009 e HU-012 |
| RN-003 | Novas matrículas só podem ser realizadas enquanto o período de matrícula estiver aberto. | HU-009, HU-011 e HU-012 |
| RN-004 | Matrículas só podem ser canceladas enquanto o período de matrícula estiver aberto. | HU-010 |
| RN-005 | Uma disciplina deve possuir pelo menos três alunos matriculados ao final do período de matrícula para ser ofertada no semestre seguinte. | HU-007 e HU-008 |
| RN-006 | Uma disciplina com menos de três alunos matriculados ao término do período de matrícula deve ser cancelada e não deve ocorrer no semestre seguinte. | HU-007 e HU-008 |
| RN-007 | A quantidade de alunos matriculados em uma disciplina não pode ultrapassar 60. | HU-009, HU-011 e HU-012 |

## Requisitos não funcionais

Os requisitos abaixo definem condições de qualidade, segurança e operação do sistema e devem ser verificados em conjunto com as histórias de usuário aplicáveis.

| Requisito | Critério de aceitação | Prioridade | Aplicação |
| --- | --- | --- | --- |
| RNF-001 | O tempo de resposta do sistema deve ser de até três segundos. | Alta | Todas as histórias. |
| RNF-002 | O banco de dados deve receber um backup completo a cada 24 horas, preferencialmente em horário de menor utilização do sistema. | Alta | Dados que sustentam as histórias. |
| RNF-003 | A sessão autenticada deve ser encerrada automaticamente após 30 minutos consecutivos sem interação do usuário. | Baixa | HU-001 e sessões utilizadas nas demais histórias. |
| RNF-004 | O sistema deve manter disponibilidade mínima de 99% durante o período oficial de matrículas, desconsiderando manutenções previamente programadas. | Alta | Todas as histórias durante o período oficial de matrículas. |
| RNF-005 | Registros de login, matrícula, cancelamento e outras operações relevantes devem permanecer armazenados por pelo menos 12 meses. | Média | HU-001, HU-009, HU-010 e demais operações relevantes. |
| RNF-006 | A interface deve ser responsiva para dispositivos móveis com iOS e Android. | Alta | Interfaces de todas as histórias. |
| RNF-007 | Após cinco tentativas consecutivas de autenticação inválida, o acesso à conta deve ser bloqueado temporariamente por 15 minutos. | Média | HU-001. |
