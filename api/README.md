# FitterApp API

API Spring Boot do FitterApp. Os comandos abaixo são a referência oficial para desenvolvimento local no Windows.

## Pré-requisitos

- Java 21 disponível em `JAVA_HOME` e no `PATH`.
- Docker Desktop em execução para o PostgreSQL local e para os testes de integração.
- PowerShell 5.1 (`powershell.exe`) disponível no `PATH` para o Maven Wrapper no Windows.

Não é necessário instalar o Maven. O Wrapper baixa e fixa a versão definida em
`.mvn/wrapper/maven-wrapper.properties`.

## Maven Wrapper no Windows

Na pasta `api`, confirme o bootstrap com:

```powershell
.\mvnw.cmd --version
```

O bootstrap foi validado também com um `MAVEN_USER_HOME` vazio. O primeiro uso precisa de acesso HTTPS ao Maven Central para baixar a distribuição; os usos seguintes aproveitam o cache local.

Se o comando terminar com `Cannot start maven from wrapper`, habilite o diagnóstico e confirme os pré-requisitos:

```powershell
Get-Command powershell.exe
java -version
$env:MVNW_VERBOSE = "true"
.\mvnw.cmd --version
Remove-Item Env:\MVNW_VERBOSE
```

O script `mvnw.cmd` chama especificamente o Windows PowerShell (`powershell.exe`), mesmo quando o terminal atual usa PowerShell 7 (`pwsh`). Uma rede que bloqueie `https://repo.maven.apache.org` também impede o primeiro bootstrap.

Em Linux e macOS, use o equivalente:

```bash
./mvnw --version
```

## Testes e migrations

Execute a verificação completa na pasta `api`:

```powershell
.\mvnw.cmd clean verify
```

Esse é o comando oficial de validação completa. A suíte usa Testcontainers para iniciar um PostgreSQL 17 vazio. Durante o bootstrap do contexto, o Flyway valida e aplica, em ordem, as migrations `V1` a `V6`; em seguida os testes conferem o schema, as restrições e o comportamento da aplicação. O Docker precisa estar em execução.

Para executar apenas uma classe de teste:

```powershell
.\mvnw.cmd -Dtest=AuthControllerTests test
```

## Execução local

Na raiz do repositório, inicie o PostgreSQL:

```powershell
docker compose up -d postgres
```

Depois, na pasta `api`, inicie a aplicação:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrão, a API conecta em `jdbc:postgresql://localhost:5432/fitterapp`. As variáveis aceitas estão documentadas no arquivo `.env.example` da raiz. Ao iniciar a API diretamente pelo Maven, defina no terminal as variáveis que quiser sobrescrever; o Docker Compose não exporta o conteúdo de `.env` para o processo Java.

Para encerrar apenas os serviços, preservando os dados locais:

```powershell
docker compose stop
```

## Recuperação de senha

A API envia o link de recuperação pelo Resend após salvar a solicitação. Configure
`RESEND_API_KEY`, `RESEND_FROM` e `PASSWORD_RESET_URL`. O endereço de recuperação
deve ser uma URL HTTPS pública que abra a rota `/reset-password` do painel web;
o endereço local padrão é `http://localhost:5173/reset-password`. O mesmo link
funciona para contas usadas no mobile. O token expira após 30 minutos e só pode
ser usado uma vez. Em caso de falha na entrega, a resposta permanece genérica e
a falha é registrada nos logs da API, sem incluir e-mail ou token.

## Métricas do marketplace

O catálogo registra pesquisas na primeira página, visualizações de perfil e
contatos iniciados. O mobile informa os cabeçalhos `X-Visitor-Id` e
`X-Idempotency-Key`: requisições repetidas com a mesma identidade e chave não
são contabilizadas novamente para o mesmo tipo de evento. Quando não há
identificador de visitante, uma conta autenticada pode ser usada como identidade;
eventos sem nenhuma identidade continuam nos totais, mas não na contagem única.
Um retorno ao aplicativo é uma abertura posterior associada à mesma identidade.

O dashboard registra contagens por período e filtros usados. O histórico de
envio, aprovação e reprovação começa com a aplicação da migration V12; os dados
de períodos anteriores não podem ser reconstruídos integralmente.

## Mídia em disco na VPS

Defina `FITTERAPP_STORAGE_ROOT` como um diretório persistente, por exemplo
`/var/lib/fitterapp/uploads`, fora do diretório publicado pelo Nginx. O processo
da API precisa de permissão de leitura e escrita nesse diretório. Ao executar
a API em contêiner, monte o diretório da VPS como volume e configure essa
variável com o caminho **dentro** do contêiner; não use a camada descartável
do contêiner. O padrão para desenvolvimento local é `./uploads` na pasta da API.

Imagens JPEG, PNG e WebP de até 5 MB são validadas, redimensionadas para no
máximo 1600 pixels no maior lado e convertidas para WebP. Apenas a chave fica no
banco. O codec WebP incluído oferece binários para Windows e Linux x86-64;
confirme a arquitetura antes de provisionar outra plataforma.

O Nginx deve encaminhar `/api/` para a API com `client_max_body_size 6m`.
Não exponha o diretório de uploads diretamente por `alias` ou diretório estático:
a rota `/api/v1/public/media/` só entrega fotos da versão publicada, enquanto
fotos em análise e documentos CREF usam rotas administrativas autenticadas.

Inclua o diretório persistente e um dump consistente do PostgreSQL na mesma
rotina de backup, grave as cópias fora da VPS e teste a restauração dos dois
conjuntos. As imagens antigas não são apagadas automaticamente, pois podem
continuar referenciadas por outras revisões. Configure permissões restritas
no diretório e nas cópias, já que ele contém documentos privados.

## Conta autenticada

`GET /api/v1/me` retorna os dados básicos e as roles da conta autenticada.
`PUT /api/v1/me` atualiza `fullName` e `phoneNumber` (no formato internacional,
por exemplo `+5544999999999`). A conta é identificada pelo JWT. O e-mail não
é alterado por essa rota, pois sua mudança exige confirmação própria.
