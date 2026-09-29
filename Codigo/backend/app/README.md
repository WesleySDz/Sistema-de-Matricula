# API Matrícula Mais

Controllers REST e services baseados nos RF-001 a RF-018 e nas RN-001 a RN-007 dos documentos em `Artefatos`. O projeto mantém Java 26, Spring Boot 4.1.1 e as entidades da estrutura inicial.

## Execução

Na pasta `Codigo/backend/app`, configure `SECRETARIA_LOGIN` e `SECRETARIA_SENHA` (ao menos oito caracteres) no ambiente e execute:

```bash
./mvnw spring-boot:run
```

Essas variáveis criam a primeira conta de secretaria caso o login ainda não exista. Não há credenciais padrão nem cadastro público. Alunos e professores são cadastrados pela secretaria. Senhas são armazenadas com BCrypt; nunca são retornadas pela API.

O H2 em memória e `ddl-auto=create-drop` da estrutura inicial foram mantidos: os dados, inclusive notificações pendentes e bloqueios de login, são perdidos ao reiniciar. Para implantação, configure um banco persistente e migrações antes de utilizar dados reais.

## Autenticação e CSRF

1. Faça `GET /api/auth/csrf`, preservando o cookie de sessão. A resposta contém `token`, `headerName` e `parameterName`.
2. Faça `POST /api/auth/login` com `Content-Type: application/x-www-form-urlencoded`, campos `login` e `senha`, e o token CSRF no cabeçalho indicado. Sucesso retorna `204`; credenciais inválidas, `401`; conta bloqueada, `423`.
3. Consulte novamente `/api/auth/csrf` após o login, pois o token é renovado. Preserve o cookie e envie o token nas operações POST, PUT e DELETE.
4. `GET /api/auth/me` retorna o identificador e o perfil da sessão. `POST /api/auth/logout` encerra a sessão e retorna `204`.

A sessão expira após 30 minutos sem interação. Cinco falhas consecutivas bloqueiam a conta por 15 minutos; o contador é persistido mesmo quando a autenticação falha. Uma autenticação bem-sucedida zera o contador.

## Rotas

Todas as rotas abaixo exigem autenticação. IDs são numéricos.

| Perfil | Método e rota | Operação |
| --- | --- | --- |
| Secretaria | `GET/POST /api/alunos` | Listar/cadastrar alunos |
| Secretaria | `GET/PUT/DELETE /api/alunos/{id}` | Consultar/atualizar/excluir aluno |
| Secretaria | `GET/POST /api/professores` | Listar/cadastrar professores |
| Secretaria | `GET/PUT/DELETE /api/professores/{id}` | Consultar/atualizar/excluir professor |
| Secretaria | `GET/POST /api/disciplinas` | Listar/cadastrar disciplinas |
| Secretaria | `GET/PUT/DELETE /api/disciplinas/{id}` | Consultar/atualizar/excluir disciplina |
| Secretaria | `GET/POST /api/cursos` | Listar/cadastrar cursos |
| Secretaria | `GET/PUT/DELETE /api/cursos/{id}` | Consultar/atualizar/excluir curso |
| Todos | `GET /api/semestres` e `GET /api/semestres/{id}` | Consultar semestres e seus currículos |
| Secretaria | `POST /api/semestres` | Criar semestre com matrículas abertas |
| Secretaria | `PUT /api/semestres/{id}/curriculo` | Definir disciplinas ofertadas |
| Secretaria | `GET /api/semestres/{id}/situacao-disciplinas` | Consultar ofertas ativas/canceladas e ocupação |
| Secretaria | `POST /api/semestres/{id}/encerrar-matriculas` | Encerrar inscrições e aplicar o mínimo de três alunos |
| Secretaria | `POST /api/semestres/{id}/concluir` | Concluir o período letivo para consulta no histórico |
| Secretaria | `GET /api/disciplinas/{id}/matriculas?semestreId=1` | Consultar inscritos ativos na oferta |
| Aluno | `POST /api/matriculas` | Inscrever-se nas disciplinas selecionadas |
| Aluno | `DELETE /api/matriculas/{id}` | Cancelar matrícula própria |
| Aluno | `GET /api/matriculas/me?semestreId=1` | Consultar matrículas ativas no semestre informado |
| Aluno | `GET /api/matriculas/me/disciplinas?semestreId=1` | Consultar disciplinas, créditos e professores |
| Aluno | `GET /api/matriculas/me/historico` | Consultar disciplinas de semestres concluídos |
| Professor | `GET /api/professores/me/disciplinas` | Consultar disciplinas sob sua responsabilidade |
| Professor | `GET /api/professores/me/disciplinas/{disciplinaId}/alunos?semestreId=1` | Consultar alunos de uma disciplina própria |

Nos endpoints `/me` e de matrícula, a identidade vem da sessão; o cliente não fornece um `alunoId` ou `professorId` para acessar dados de outra pessoa.

## Corpos de requisição

POST e PUT de cadastros recebem os mesmos campos. PUT substitui todos os campos editáveis, inclusive a senha nos cadastros de usuários.

```json
{
  "nome": "Ana Silva",
  "login": "ana",
  "senha": "uma-senha-segura",
  "email": "ana@example.com",
  "matricula": "20260001",
  "curso": "Sistemas de Informação"
}
```

Para professor, substitua `matricula` e `curso` por `titulacao`. Demais cadastros:

| Recurso | Corpo de exemplo |
| --- | --- |
| Disciplina | `{"codigo":"ALG101","nome":"Algoritmos","creditos":4,"professorId":2}` |
| Curso | `{"nome":"Sistemas de Informação","quantidadeCreditos":160,"disciplinaIds":[1,2]}` |
| Semestre | `{"nome":"2026/2"}` |
| Currículo | `{"disciplinaIds":[1,2,3,4,5,6]}` |

Inscrição do aluno autenticado:

```json
{
  "semestreId": 1,
  "obrigatorias": [1, 2, 3, 4],
  "optativas": [5, 6]
}
```

Use `[]` quando não houver optativas. Cada POST conclui uma inscrição nas disciplinas enviadas; chamadas posteriores acrescentam disciplinas respeitando os limites acumulados no semestre. Deve haver pelo menos uma disciplina na requisição. Uma disciplina não pode aparecer duas vezes nem já estar ativa para o aluno naquele semestre. Se qualquer seleção for inválida, nenhuma matrícula dessa requisição é salva.

Criações retornam `201`, exclusões e cancelamentos retornam `204`. Erros de entrada retornam `400`, acesso negado `403`, recurso inexistente `404` e conflitos de negócio `409`, com `ProblemDetail` nos erros de validação e negócio. Listas sem resultados retornam `[]`.

## Regras e decisões de modelagem

- O limite é de quatro obrigatórias e duas optativas por aluno e semestre, contando inscrições anteriores ainda ativas.
- A capacidade de 60 alunos considera a oferta da disciplina em cada semestre. Alterações no currículo, encerramentos, inscrições e cancelamentos usam um bloqueio transacional no semestre para coordenar requisições simultâneas.
- O encerramento das matrículas cancela ofertas com menos de três alunos e desativa suas matrículas. A situação é armazenada por semestre, sem desativar o cadastro global da disciplina.
- Encerrar inscrições e concluir o semestre letivo são ações diferentes. O histórico usa matrículas ativas de semestres explicitamente concluídos; matrículas canceladas não entram no histórico.
- O nome do semestre é único e não é editável, pois a entidade inicial `Matricula` guarda esse vínculo como texto. A distinção obrigatória/optativa foi acrescentada à matrícula, conforme o diagrama.
- O diagrama de classes possui `Turma`, enquanto o código inicial usa `Semestre` e `Disciplina`. Esta implementação preserva o código inicial e representa a oferta pela combinação semestre/disciplina, sem criar uma segunda estrutura concorrente.
- O campo `Aluno.curso` continua textual, conforme a estrutura inicial. Os cadastros de curso relacionam suas disciplinas, mas não impõem uma restrição adicional de matrícula por curso, ausente nos requisitos.
- Exclusões que destruiriam vínculos existentes são recusadas com `409`: aluno com matrículas, professor responsável por disciplinas ou disciplina associada a curso/semestre/matrícula. Isso preserva a integridade e o histórico.

## Integração de cobrança

O contrato e o endereço do sistema externo não foram fornecidos. Foi definido um adaptador HTTP configurável por `COBRANCA_URL`, com POST JSON:

```json
{
  "notificacaoId": 1,
  "alunoId": 10,
  "semestreId": 1,
  "semestre": "2026/2",
  "disciplinaIds": [1, 2, 3]
}
```

A lista contém todas as disciplinas ativas do aluno no instante da conclusão da inscrição. O registro de notificação é salvo na mesma transação das matrículas; uma inscrição rejeitada não cria cobrança. Um envio periódico a cada 30 segundos consulta as notificações confirmadas no banco, com tentativas posteriores quando o sistema externo falha. Sem URL, elas permanecem pendentes.

O envio usa `Idempotency-Key: matricula-{notificacaoId}`. O sistema receptor deve deduplicar por essa chave e tratar o conteúdo como o estado da inscrição, pois a entrega pode se repetir. O contrato deverá ser ajustado ao sistema de cobrança real, inclusive autenticação, caso exigida. Estornos e ajustes após cancelamentos não estão definidos no RF-018 e não foram inventados neste contrato.

## Verificação

```bash
./mvnw test
```

Os testes cobrem os limites por semestre, duplicidades, período de matrícula, cancelamento, histórico, mínimo de alunos, propriedade de dados, autenticação, CSRF, CRUD e a disputa concorrente pela última vaga.

Os requisitos operacionais de backups, disponibilidade, retenção de auditoria por 12 meses e tempo de resposta sob carga precisam de infraestrutura e validação específicas. Responsividade é uma responsabilidade do frontend. Não são garantidos apenas por estas camadas do backend.
