# Matrícula Mais — terminal e API

Controllers REST e services baseados nos RF-001 a RF-018 e nas RN-001 a RN-007 dos documentos em `Artefatos`. O projeto mantém Java 26, Spring Boot 4.1.1 e as entidades da estrutura inicial.

## Interface de terminal

O projeto pode ser utilizado pelo terminal com um único login para todos os usuários. O perfil é identificado pelo backend a partir da conta autenticada: `SECRETARIA`, `ALUNO` ou `PROFESSOR`. Não há seleção manual de perfil nem logins separados.

Os iniciadores requerem o **JDK 26**, com `java` disponível no PATH. Na pasta `Codigo/backend/app`, configure a primeira conta de secretaria e execute conforme seu sistema.

**Linux e macOS (Bash):**

```bash
read -r -p "Login inicial da secretaria: " SECRETARIA_LOGIN
read -r -s -p "Senha inicial (mínimo 8 caracteres): " SECRETARIA_SENHA
export SECRETARIA_LOGIN SECRETARIA_SENHA
./terminal.sh
```

**Windows (PowerShell):**

```powershell
$credencial = Get-Credential -Message "Primeira conta da secretaria (senha com mínimo de 8 caracteres)"
$env:SECRETARIA_LOGIN = $credencial.UserName
$env:SECRETARIA_SENHA = $credencial.GetNetworkCredential().Password
.\terminal.cmd
```

No Prompt de Comando (CMD), o iniciador é `terminal.cmd`; as variáveis `SECRETARIA_LOGIN` e `SECRETARIA_SENHA` devem estar configuradas no ambiente para criar a primeira conta.

Os scripts compilam o projeto e iniciam o terminal e o backend no mesmo processo Java. A primeira compilação precisa das dependências Maven disponíveis. Depois que a conta estiver criada, as variáveis não são necessárias: execute apenas `./terminal.sh` no Linux/macOS ou `.\terminal.cmd` no Windows. As credenciais de contas existentes não são sobrescritas na inicialização. Os iniciadores usam a pasta do backend como diretório de trabalho, mesmo quando chamados de outra pasta.

Também é possível compilar e iniciar manualmente, inclusive no Windows:

```bash
./mvnw package
java -jar target/matricula-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=terminal
```

No PowerShell, substitua `./mvnw package` por `.\mvnw.cmd package`. O comando `java -jar` é o mesmo nos três sistemas. Execute o JAR a partir da pasta do backend para manter o mesmo diretório de dados. O arquivo `.sh` não é necessário no Windows.

O campo de autenticação é **Login**, já existente no projeto. O e-mail permanece um dado cadastral; se desejar usá-lo como identificador, informe esse endereço também no campo `login` ao cadastrar a conta.

### Menus por perfil

| Perfil | Funcionalidades no terminal |
| --- | --- |
| Secretaria | Cadastrar, listar, consultar, atualizar e excluir alunos, professores, disciplinas e cursos; cadastrar semestres; definir currículos; consultar matrículas e situação das ofertas; encerrar matrículas e concluir o semestre letivo. |
| Aluno | Consultar ofertas, realizar matrícula, consultar matrículas e detalhes das disciplinas, cancelar matrícula própria e consultar histórico. |
| Professor | Consultar suas disciplinas, os respectivos alunos e os currículos dos semestres. |
| Todos | Consultar o próprio perfil e sair da conta para permitir novo login. |

A opção `0` volta ao menu anterior ou sai da conta, conforme indicado na tela. Na tela de login, `0` encerra o programa. Use `/voltar` durante o preenchimento de um formulário para cancelar a operação. IDs são exibidos nas consultas; listas de disciplinas são informadas como `1,2,3`, e Enter representa uma lista vazia. Operações de alteração pedem confirmação.

No cadastro e na atualização de alunos, a etapa **SELEÇÃO DE CURSO** consulta os cursos cadastrados e exibe `[ID] Nome`. Informe o ID de um desses cursos. Um ID inexistente apresenta `Curso inválido. Escolha um dos cursos disponíveis.` e repete a lista até uma escolha válida ou `/voltar`. Se não houver cursos, o formulário é cancelado com orientação para cadastrar um curso pelo menu de cursos primeiro.

Em um terminal interativo, a senha não é exibida. Consoles de IDE ou entrada redirecionada podem não fornecer `System.console()`; nesse caso a interface informa que a senha ficará visível. O JAR executado diretamente em um terminal é a opção preferencial.

### Dados e arquitetura

O perfil `terminal` usa **H2 em arquivo** (`data/matricula-mais.mv.db`) e conserva os cadastros entre execuções. Os logs técnicos ficam em `data/matricula-mais.log`, fora dos menus. Esses arquivos são ignorados pelo Git. Não execute duas instâncias usando o mesmo arquivo de banco.

```text
CLI (menus e formulários)
    -> ApiClient (HTTP local, cookie de sessão e CSRF)
    -> Spring Security (autenticação e autorização)
    -> Controllers existentes
    -> Services existentes
    -> Repositories -> Banco de dados
```

O backend escuta apenas em `127.0.0.1`, em uma porta livre escolhida automaticamente. A interface não acessa repositories ou services diretamente e não decide se uma operação de negócio é válida. As restrições de `role` e de propriedade dos dados são verificadas no servidor, inclusive se uma rota for chamada fora dos menus. As validações e mensagens de erro dos endpoints são apresentadas ao usuário. Sessão expirada exige novo login.

O ponto de entrada apenas inicia o Spring e, quando `app.cli.enabled=true`, executa a interface e fecha a aplicação ao sair. Um futuro frontend pode consumir as mesmas rotas sem reimplementar autenticação, autorização ou regras de matrícula.

## Execução apenas da API

Na pasta `Codigo/backend/app`, configure `SECRETARIA_LOGIN` e `SECRETARIA_SENHA` (ao menos oito caracteres) no ambiente e execute:

```bash
./mvnw spring-boot:run
```

Essas variáveis criam a primeira conta de secretaria caso o login ainda não exista. Não há credenciais padrão nem cadastro público. Alunos e professores são cadastrados pela secretaria. Senhas são armazenadas com BCrypt; nunca são retornadas pela API.

Tanto a API quanto o terminal usam o H2 em arquivo (`data/matricula-mais.mv.db`) e preservam os cadastros e a sequência de matrículas entre execuções. Para implantação web, configure o banco e as migrações adequados ao ambiente. Apenas os testes usam bancos em memória, sem acessar os dados locais.

## Autenticação e CSRF

1. Faça `GET /api/auth/csrf`, preservando o cookie de sessão. A resposta contém `token`, `headerName` e `parameterName`.
2. Faça `POST /api/auth/login` com `Content-Type: application/x-www-form-urlencoded`, campos `login` e `senha`, e o token CSRF no cabeçalho indicado. Sucesso retorna `204`; credenciais inválidas, `401`; conta bloqueada, `423`.
3. Consulte novamente `/api/auth/csrf` após o login, pois o token é renovado. Preserve o cookie e envie o token nas operações POST, PUT e DELETE.
4. `GET /api/auth/me` retorna identificador, login, nome, e-mail e perfil da sessão. `POST /api/auth/logout` encerra a sessão e retorna `204`.

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
  "cursoId": 1
}
```

A matrícula do aluno é gerada automaticamente pelo backend e retornada no cadastro e nas consultas. Ela não deve ser enviada no POST ou PUT e não é solicitada pelo terminal. Se um cliente antigo enviar `matricula`, esse campo é ignorado: ele não escolhe nem altera o número.

O campo `cursoId` é obrigatório e deve ser o ID positivo de um curso já cadastrado, consultado em `GET /api/cursos`. O backend verifica sua existência em todo POST e PUT de aluno; nomes livres não substituem o ID e nenhum curso é criado nesse fluxo. A resposta do aluno inclui `cursoId` e `curso` (nome atual do curso selecionado).

Para professor, substitua `cursoId` por `titulacao` (texto). Demais cadastros:

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

- Cada novo aluno recebe uma matrícula numérica a partir de `1000001`. A emissão usa uma identidade gerada pelo banco, registrada separadamente e na mesma transação do cadastro. Esse registro permanece mesmo após excluir o aluno, evitando reutilização de números. Cadastros simultâneos recebem números distintos.
- O número é persistido e imutável nas atualizações. Matrículas antigas são mantidas; números já ocupados por cadastros legados são pulados. A sequência pode ter intervalos após transações canceladas ou colisões com números antigos.
- O limite é de quatro obrigatórias e duas optativas por aluno e semestre, contando inscrições anteriores ainda ativas.
- A capacidade de 60 alunos considera a oferta da disciplina em cada semestre. Alterações no currículo, encerramentos, inscrições e cancelamentos usam um bloqueio transacional no semestre para coordenar requisições simultâneas.
- O encerramento das matrículas cancela ofertas com menos de três alunos e desativa suas matrículas. A situação é armazenada por semestre, sem desativar o cadastro global da disciplina.
- Encerrar inscrições e concluir o semestre letivo são ações diferentes. O histórico usa matrículas ativas de semestres explicitamente concluídos; matrículas canceladas não entram no histórico.
- O nome do semestre é único e não é editável, pois a entidade inicial `Matricula` guarda esse vínculo como texto. A distinção obrigatória/optativa foi acrescentada à matrícula, conforme o diagrama.
- O diagrama de classes possui `Turma`, enquanto o código inicial usa `Semestre` e `Disciplina`. Esta implementação preserva o código inicial e representa a oferta pela combinação semestre/disciplina, sem criar uma segunda estrutura concorrente.
- `Aluno.curso` é uma associação `ManyToOne` para `Curso`, persistida pela chave estrangeira `curso_id`. A seleção consulta `CursoController -> CursoService -> CursoRepository`; o `AlunoService` reutiliza `CursoService.buscarEntidade` antes de salvar. Os cadastros de curso relacionam suas disciplinas, mas não impõem uma restrição adicional de matrícula por curso.
- Em bancos de versões anteriores, `ddl-auto=update` acrescenta `curso_id`, mas não converte automaticamente o antigo texto `curso` em um vínculo. Alunos antigos continuam acessíveis, com `cursoId` e `curso` nulos na resposta até a seleção de um curso existente pela atualização do cadastro. A coluna antiga é preservada no banco; nenhum curso é criado a partir dela.
- Exclusões que destruiriam vínculos existentes são recusadas com `409`: aluno com matrículas, curso vinculado a aluno, professor responsável por disciplinas ou disciplina associada a curso/semestre/matrícula. Isso preserva a integridade e o histórico.

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

Os testes de integração da interface executam entradas de terminal contra um servidor HTTP real, verificando o login único dos três perfis, troca de usuário, cadastro, inscrição, cancelamento, consultas do professor, histórico, mensagens de validação e acesso negado mesmo fora dos menus. Há também verificação de entrada inválida, cancelamento de formulário e fim de entrada. Nesses testes de integração, a autenticação passa pelos filtros reais de segurança.

A seleção de curso tem cobertura de ID válido, repetição após IDs inexistentes, entrada textual recusada, cancelamento e ausência de cursos. Os testes da API verificam o vínculo persistido, troca e renomeação do curso, rejeição de IDs inválidos sem cadastros parciais e preservação da chave estrangeira ao tentar excluir um curso em uso.

Os requisitos operacionais de backups, disponibilidade, retenção de auditoria por 12 meses e tempo de resposta sob carga precisam de infraestrutura e validação específicas. Responsividade é uma responsabilidade do frontend. Não são garantidos apenas por estas camadas do backend.
