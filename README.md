# Projeto de Reconhecimento de Voz e Controle de Transações

Aplicação backend para registrar transações financeiras e integrar recursos de inteligência artificial: chat, transcrição de áudio e text-to-speech. O projeto usa Spring Boot, persiste transações em MySQL e executa o banco com Docker Compose.

## Sobre o projeto

### O que a aplicação faz

- Cria transações com descrição, categoria e valor.
- Persiste os dados na tabela `transaction_entity` do MySQL.
- Consulta o chat da OpenAI por meio de um endpoint HTTP.
- Transcreve arquivos de áudio enviados por multipart/form-data.
- Converte texto em áudio MP3.

### Melhoria implementada

A principal melhoria foi transformar o projeto em uma aplicação completa, com persistência real e ambiente reproduzível: o MySQL passou a ser executado pelo Docker Compose, o Spring Boot conecta-se ao banco via JPA e o fluxo principal pode ser validado pelo REST Client, curl e consultas SQL.

### Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA e Hibernate
- Spring AI OpenAI
- MySQL 9.6
- Docker Compose
- Gradle
- Lombok
- VS Code REST Client

### Segurança antes do GitHub

Nunca publique API keys, senhas ou tokens. O arquivo `.gitignore` bloqueia `.env`, certificados, chaves, arquivos de credenciais e diretórios de segredos. O arquivo `.env.example` contém apenas um placeholder e pode ser versionado.

Antes do primeiro commit, confira:

```powershell
git status
git diff -- .gitignore README.md
git ls-files | Select-String -Pattern '(^|/)(\.env|.*\.pem|.*\.key|.*credentials.*|.*secrets.*)'
```

Se uma chave real já tiver sido commitada, removê-la do arquivo não basta: revogue-a no provedor, gere outra e depois remova o histórico com uma ferramenta apropriada.

## Índice

- [Preparação](#preparação)
- [Docker e Docker Compose](#docker-e-docker-compose)
- [Gradle](#gradle)
- [Endpoints](#endpoints)
- [Testes HTTP](#testes-http)
- [Visualizar o banco](#visualizar-o-banco)
- [Testes automatizados](#testes-automatizados)
- [Troubleshooting](#troubleshooting)

## Preparação

### Variável da OpenAI

No PowerShell, para a sessão atual:

```powershell
$env:OPENAI_API_KEY="sua-chave-api-aqui"
$env:OPENAI_API_KEY
```

Ou crie um arquivo `.env` na pasta `budgeting/`:

```properties
OPENAI_API_KEY=sua-chave-api-aqui
MYSQL_ROOT_PASSWORD=sua-senha-local-do-root
MYSQL_PASSWORD=sua-senha-local-do-usuário-app
```

O arquivo é importado por `spring.config.import=optional:file:.env[.properties]`.

### Codificação UTF-8 no Windows

```powershell
$OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
```

### Portas

- Aplicação Spring Boot: `8080`
- MySQL publicado pelo Docker: `3307`
- MySQL dentro do container: `3306`

Para verificar a porta da aplicação:

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

## Docker e Docker Compose

O arquivo `compose.yml` cria o serviço MySQL usado pela aplicação.

### Iniciar o banco

Abra o Docker Desktop e execute na pasta `budgeting`:

```powershell
docker info
docker compose up -d
docker compose ps
docker ps
```

Conexão local do banco:

```text
Host: 127.0.0.1
Porta: 3307
Banco: transaction
Usuário: app
Senha: valor de MYSQL_PASSWORD no .env
```

O `compose.yml` lê `MYSQL_ROOT_PASSWORD` e `MYSQL_PASSWORD` do `.env`. Como `.env` está no `.gitignore`, essas credenciais permanecem locais. Copie `.env.example` para `.env` e preencha os valores antes de executar o Compose.

O mapeamento de portas é `127.0.0.1:3307` para `container:3306`.

### Logs e encerramento

```powershell
docker compose logs -f database
docker compose stop
docker compose down
```

Para remover também o volume e os dados persistidos:

```powershell
docker compose down -v
```

Use `down -v` somente quando puder apagar o volume `transaction_data`.

### Integração com o Spring Boot

Como o projeto possui `spring-boot-docker-compose`, o Spring Boot encontra o `compose.yml` durante o `bootRun`. O fluxo recomendado é:

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

O log de sucesso deve conter:

```text
HikariPool-1 - Added connection
Database JDBC URL [jdbc:mysql://127.0.0.1:3307/transaction]
Tomcat started on port 8080
Started BudgetingApplication
```

Para gerenciar o Compose apenas manualmente, adicione temporariamente ao `application.properties`:

```properties
spring.docker.compose.enabled=false
```

Nesse caso, execute `docker compose up -d` antes do `bootRun`.

### Visualizar no VS Code

Com a extensão Docker instalada:

1. Abra a visão **Docker** na barra lateral.
2. Expanda **Containers** para visualizar o MySQL e seus logs.
3. Expanda **Volumes** para visualizar `transaction_data`.

A extensão Docker não mostra as tabelas. Para isso, use o terminal, SQLTools, DBeaver ou MySQL Workbench.

## Gradle

Execute os comandos na pasta `budgeting`:

```powershell
# Compila, testa e empacota
.\gradlew.bat clean build

# Compila sem executar testes
.\gradlew.bat clean build -x test

# Inicia a aplicação
.\gradlew.bat bootRun
```

A aplicação fica disponível em `http://localhost:8080`.

Para encerrar o `bootRun`, pressione `Ctrl+C` no terminal. Alternativamente:

```powershell
Stop-Process -Name java -Force
```

## Endpoints

### Criar transação

```text
POST /transactions
```

Corpo JSON:

```json
{
  "description": "Compras do supermercado",
  "category": "GROCERIES",
  "amount": 12550
}
```

Categorias válidas: `GROCERIES`, `PHARMA` e `AUTO`.

O endpoint retorna `201 Created`:

```json
{
  "id": "uuid-gerado",
  "category": "GROCERIES",
  "description": "Compras do supermercado",
  "amount": 12550.0
}
```

O campo `amount` é `long` no código atual. Envie um número inteiro, como `12550`.

### Chat

```text
GET /api/chat-model?prompt=Ola
```

### Transcrição

```text
POST /api/transcribe
```

Recebe `file` como `multipart/form-data`.

### Text-to-speech

```text
POST /api/synthesize
```

Recebe JSON com `text` e retorna um arquivo binário MP3 (`audio/mpeg`).

## Testes HTTP

### REST Client

Os arquivos prontos ficam em `http/`:

- `transactions.http`
- `transcribe.http`
- `speech.http`

Instale a extensão **REST Client**, abra um desses arquivos e clique em **Send Request**.

Para TTS, use **Save Response Body** e salve como `audio.mp3`. Não copie os bytes exibidos como texto.

### curl: transação

```powershell
curl.exe -X POST "http://localhost:8080/transactions" `
  -H "Content-Type: application/json" `
  -d '{"description":"Compras do supermercado","category":"GROCERIES","amount":12550}'
```

### curl: chat

```powershell
curl.exe -X GET "http://localhost:8080/api/chat-model?prompt=Ola,%20como%20voce%20esta?"
```

### curl: transcrição

```powershell
$audioPath = (Resolve-Path "src\test\resources\audio\Recording-1.m4a").Path
curl.exe -X POST "http://localhost:8080/api/transcribe" -F "file=@$audioPath"
```

### curl: text-to-speech

```powershell
curl.exe -X POST "http://localhost:8080/api/synthesize" `
  -H "Content-Type: application/json" `
  -H "Accept: audio/mpeg" `
  -d '{"text":"Olá! Este texto será convertido em áudio."}' `
  -o audio.mp3

Start-Process ".\audio.mp3"
```

Não use `Out-File` ou `>` para salvar um MP3, pois isso pode corromper os bytes binários.

## Visualizar o banco

### Pelo Docker

Verifique o container:

```powershell
docker compose ps
```

Abra um console MySQL:

```powershell
$mysqlPassword = (Get-Content .env | Select-String '^MYSQL_PASSWORD=').ToString().Split('=', 2)[1]
docker exec -it budgeting-database-1 mysql -uapp -p$mysqlPassword transaction
```

Depois execute:

```sql
SHOW TABLES;
SELECT * FROM transaction_entity;
```

Ou consulte diretamente pelo PowerShell:

```powershell
$mysqlPassword = (Get-Content .env | Select-String '^MYSQL_PASSWORD=').ToString().Split('=', 2)[1]
docker exec budgeting-database-1 mysql -uapp -p$mysqlPassword transaction -e "SELECT * FROM transaction_entity;"
```

O arquivo [dio.session.sql](dio.session.sql) contém a consulta principal para usar em um cliente SQL do VS Code.

### Por SQLTools, DBeaver ou MySQL Workbench

Use os seguintes dados:

```text
Host: localhost
Porta: 3307
Database: transaction
Username: app
Password: valor de MYSQL_PASSWORD no .env
```

Consulta:

```sql
SELECT * FROM transaction_entity;
```

## Testes automatizados

```powershell
.\gradlew.bat test
.\gradlew.bat test --tests OpenAiChatModelIT
.\gradlew.bat test --tests OpenAiTranscriptionModelIT
.\gradlew.bat test --tests OpenAiKeyTest
```

## Troubleshooting

### Docker Engine indisponível

Se `docker info` informar que `dockerDesktopLinuxEngine` não foi encontrado, abra o Docker Desktop e aguarde o Engine iniciar.

### Access denied para o usuário app

Confirme o acesso:

```powershell
$mysqlPassword = (Get-Content .env | Select-String '^MYSQL_PASSWORD=').ToString().Split('=', 2)[1]
docker exec budgeting-database-1 mysql -uapp -p$mysqlPassword transaction -e "SELECT 1;"
```

Se o volume foi inicializado com credenciais antigas e não houver dados importantes, recrie-o:

```powershell
docker compose down -v
docker compose up -d
```

### Erro 400 ou 500 na transcrição

Confirme o caminho do arquivo e use `@` no curl:

```powershell
$audioPath = (Resolve-Path "src\test\resources\audio\Recording-1.m4a").Path
curl.exe -X POST "http://localhost:8080/api/transcribe" -F "file=@$audioPath"
```

### Erro 429 da OpenAI

Indica falta de cota ou créditos na conta OpenAI. Verifique o billing antes de repetir chamadas.

## Workflow completo

1. Configure `OPENAI_API_KEY` no `.env` ou no PowerShell.
2. Inicie o Docker Desktop.
3. Execute `docker compose up -d`.
4. Confirme o banco com `docker compose ps`.
5. Execute `.\gradlew.bat bootRun`.
6. Crie uma transação usando `http/transactions.http` ou curl.
7. Consulte os dados pelo `dio.session.sql` ou `docker exec`.
8. Teste chat, transcrição e text-to-speech.

## Fluxo principal validado

Com o Docker Desktop aberto e na pasta `budgeting`:

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

Em outro terminal PowerShell, ainda na pasta `budgeting`, crie uma transação:

```powershell
curl.exe -X POST "http://localhost:8080/transactions" `
  -H "Content-Type: application/json" `
  -d '{"description":"Compras do supermercado","category":"GROCERIES","amount":12550}'
```

Depois confirme a persistência:

```powershell
docker exec budgeting-database-1 mysql -uapp -papp transaction -e "SELECT * FROM transaction_entity;"
```

O retorno HTTP esperado é `201 Created`, seguido da linha correspondente na tabela MySQL.

## Evidências e aprendizados

Durante o desenvolvimento, os principais pontos aprendidos foram:

- Um terminal aberto na pasta `projeto-final` não encontra `gradlew.bat`; os comandos Gradle devem ser executados em `projeto-final\budgeting`.
- `bootRun` é um processo contínuo. O terminal permanece ocupado enquanto o servidor está ativo; `Ctrl+C` encerra a aplicação.
- O Docker CLI pode estar instalado mesmo quando o Docker Engine está parado. `docker info` é a verificação mais direta.
- O mapeamento `3307:3306` significa que a aplicação executada no Windows acessa `127.0.0.1:3307`, enquanto o MySQL escuta `3306` dentro do container.
- Variáveis `MYSQL_*` inicializam o banco apenas na primeira criação do volume. Se o volume já existir, alterar a senha no `compose.yml` não altera automaticamente a conta existente.
- O erro de autenticação `Access denied for user 'app'` foi resolvido corrigindo a configuração e garantindo a conta `app` no banco.
- Arquivos MP3 são binários. No REST Client, a resposta parece texto corrompido; é necessário usar **Save Response Body** ou `curl -o audio.mp3`.
- O upload de áudio precisa usar multipart correto e, no curl, o prefixo `@` antes do caminho do arquivo.
- `spring.jpa.hibernate.ddl-auto=update` preserva dados durante o desenvolvimento; `create` recria tabelas ao iniciar e pode apagar dados.

Os arquivos `http/transactions.http`, `http/transcribe.http`, `http/speech.http` e `dio.session.sql` registram exemplos reproduzíveis do processo de teste.

## Estrutura relevante

```text
budgeting/
├── build.gradle
├── compose.yml
├── dio.session.sql
├── http/
│   ├── transactions.http
│   ├── transcribe.http
│   └── speech.http
├── src/main/java/dio/budgeting/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
└── src/main/resources/application.properties
```

**Última atualização:** 2026-08-18
