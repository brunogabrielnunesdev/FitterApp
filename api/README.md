# FitterApp API

API Spring Boot do FitterApp. Os comandos abaixo são a referência oficial para desenvolvimento local no Windows.

## Pré-requisitos

- Java 21 configurado em `JAVA_HOME`.
- Docker Desktop em execução para o PostgreSQL local e para os testes de integração.

Não é necessário instalar o Maven. O Wrapper binário versionado no repositório inicia pelo Java e baixa a versão fixada em `.mvn/wrapper/maven-wrapper.properties`. Os checksums do Wrapper e da distribuição Maven também ficam fixados nesse arquivo.

## Maven Wrapper no Windows

Na pasta `api`, confirme o bootstrap com:

```powershell
.\mvnw.cmd --version
```

O bootstrap foi validado também com um `MAVEN_USER_HOME` vazio. O primeiro uso precisa de acesso HTTPS ao Maven Central para baixar a distribuição; os usos seguintes aproveitam o cache local. O arquivo baixado só é executado após a validação do checksum SHA-256.

Se o comando não iniciar, confirme a configuração do Java:

```powershell
java -version
Write-Output $env:JAVA_HOME
Test-Path "$env:JAVA_HOME\bin\java.exe"
.\mvnw.cmd --version
```

Uma rede que bloqueie `https://repo.maven.apache.org` impede o primeiro download do Maven.

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

O workflow `.github/workflows/backend-ci.yml` valida o bootstrap do Wrapper no Windows e executa essa verificação completa em cada alteração da API.

Para executar apenas uma classe de teste:

```powershell
.\mvnw.cmd -Dtest=AuthControllerTests test
```

## Administração de perfis

As rotas abaixo exigem a role `ADMIN` ou `OWNER`:

- `GET /api/v1/admin/personal-profiles?status=PENDING_REVIEW&page=0&size=20`: lista paginada; `status` é opcional e `size` aceita de 1 a 100 itens.
- `GET /api/v1/admin/personal-profiles/{profileId}`: retorna conta, revisão atual, CREF, modalidades, formas e regiões de atendimento, preço e todos os status necessários para análise.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/approval`: aprova a revisão em análise.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/rejection`: reprova a revisão; o corpo deve informar `reason`.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/suspension`: suspende um perfil aprovado ou publicado; o corpo deve informar `reason`.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/reactivation`: encerra a suspensão ativa e restaura o estado `APPROVED` ou `PUBLISHED` anterior; o corpo deve informar `reason`.

Suspensão e reativação registram o administrador, o motivo, a data e os estados anterior e novo. Um perfil suspenso não pode ser republicado pelo titular e fica fora das consultas do catálogo até a reativação administrativa.

O endpoint legado `GET /api/v1/admin/personal-profiles/pending-review` permanece disponível para compatibilidade com o painel existente.

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
