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

Esse é o comando oficial de validação completa. A suíte usa Testcontainers para iniciar um PostgreSQL 17 vazio. Durante o bootstrap do contexto, o Flyway valida e aplica, em ordem, as migrations `V1` a `V10`; em seguida os testes conferem o schema, as restrições e o comportamento da aplicação. O Docker precisa estar em execução.

O workflow `.github/workflows/backend-ci.yml` valida o bootstrap do Wrapper no Windows e executa essa verificação completa em cada alteração da API.

Para executar apenas uma classe de teste:

```powershell
.\mvnw.cmd -Dtest=AuthControllerTests test
```

## Administração de perfis

O CREF é opcional no fluxo profissional. Um perfil completo pode ser enviado, aprovado e publicado sem CREF. Quando o personal optar por informá-lo, `registrationCode` e `documentImageKey` continuam obrigatórios em conjunto no endpoint de atualização; esses dados permanecem privados e aparecem apenas nas consultas administrativas.

As rotas abaixo exigem a role `ADMIN` ou `OWNER`:

- `GET /api/v1/admin/personal-profiles?status=PENDING_REVIEW&page=0&size=20`: lista paginada; `status` é opcional e `size` aceita de 1 a 100 itens.
- `GET /api/v1/admin/personal-profiles/{profileId}`: retorna conta, revisão atual, CREF, modalidades, formas e regiões de atendimento, preço e todos os status necessários para análise.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/approval`: aprova a revisão em análise.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/rejection`: reprova a revisão; o corpo deve informar `reason`.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/suspension`: suspende um perfil aprovado ou publicado; o corpo deve informar `reason`.
- `PATCH /api/v1/admin/personal-profiles/{profileId}/reactivation`: encerra a suspensão ativa e restaura o estado `APPROVED` ou `PUBLISHED` anterior; o corpo deve informar `reason`.
- `POST /api/v1/admin/personal-profiles`: cria uma conta nova em confirmação de e-mail e um perfil em rascunho. A requisição recebe senha temporária, dados profissionais e motivo administrativo.
- `PUT /api/v1/admin/personal-profiles/{profileId}`: edita os campos da revisão administrativa quando ela está em `DRAFT` ou `REJECTED`, reutilizando as validações de preço, modalidades, atendimento, regiões e CREF.

Suspensão e reativação registram o administrador, o motivo, a data e os estados anterior e novo. Um perfil suspenso não pode ser republicado pelo titular e fica fora das consultas do catálogo até a reativação administrativa.

O cadastro manual sempre cria uma conta nova; e-mails já existentes são rejeitados. A conta segue o fluxo normal de confirmação de e-mail e recebe inicialmente a role `STUDENT`. A role `PERSONAL` continua sendo concedida apenas após a aprovação do perfil. Criação e edição registram `origin: ADMIN` na auditoria, e a senha temporária nunca aparece nas respostas.

O endpoint legado `GET /api/v1/admin/personal-profiles/pending-review` permanece disponível para compatibilidade com o painel existente.

## Administração de usuários

As rotas abaixo exigem a role `ADMIN` ou `OWNER`:

- `GET /api/v1/admin/users?query=bruno&status=ACTIVE&role=PERSONAL&page=0&size=20`: lista usuários com busca opcional por nome ou e-mail e filtros opcionais por status e role. `size` aceita de 1 a 100 itens.
- `GET /api/v1/admin/users/{userId}`: retorna os dados operacionais da conta e suas roles.

As respostas administrativas de usuários nunca incluem hashes de senha, refresh tokens, tokens de verificação ou tokens de recuperação.

## Gerenciamento de modalidades

As rotas administrativas exigem a role `ADMIN` ou `OWNER`:

- `GET /api/v1/admin/modalities`: lista todas as modalidades, incluindo as inativas.
- `POST /api/v1/admin/modalities`: cria uma modalidade ativa; o corpo recebe `name` e o `slug` é gerado pela API.
- `PUT /api/v1/admin/modalities/{modalityId}`: atualiza nome e slug da modalidade.
- `PATCH /api/v1/admin/modalities/{modalityId}/activation`: ativa ou desativa usando o corpo `{"active": true|false}`.

Nomes são únicos sem diferenciar maiúsculas e minúsculas. A desativação preserva os vínculos históricos, mas remove a modalidade de `GET /api/v1/public/modalities` e impede sua seleção em novos cadastros ou revisões.

## Eventos do funil

As etapas do funil do MVP são persistidas com origem, usuário quando identificado e timestamp UTC:

| Etapa | Evento/tabela | Origem |
| --- | --- | --- |
| Cadastro concluído com sucesso | `ACCOUNT_COMPLETED` em `funnel_events` | `MOBILE_APP` no cadastro comum e `ADMIN_WEB` no cadastro manual |
| Perfil profissional criado | `PROFILE_STARTED` em `funnel_events` | `MOBILE_APP` ou `ADMIN_WEB`, conforme o fluxo |
| Perfil enviado para análise | `PROFILE_SUBMITTED` em `funnel_events` | `MOBILE_APP` |
| Pesquisa e filtros do catálogo | `search_events` | parâmetro `source` da consulta |
| Visualização de perfil | `profile_view_events` | parâmetro `source` da consulta |
| Início de contato por WhatsApp | `contact_events` | parâmetro `source` da requisição |

Nas rotas públicas de personal, `source` aceita `MOBILE_APP`, `PUBLIC_WEB` ou `ADMIN_WEB` e assume `PUBLIC_WEB` quando omitido. Pesquisas registram o termo normalizado, filtros efetivamente recebidos, paginação e total de resultados. Visualizações e contatos podem ser anônimos; quando um JWT válido é enviado, o usuário também é associado ao evento. A migration `V8` cria `funnel_events`; as tabelas de pesquisa, visualização e contato permanecem as definidas na `V6`.

### Deduplicação de métricas

A migration `V9` diferencia eventos brutos e únicos nas tabelas `search_events`, `profile_view_events` e `contact_events`:

- **Bruto:** toda ação recebida e processada que não seja retry da mesma chave de idempotência. Corresponde a todas as linhas da tabela.
- **Único:** primeira ocorrência da mesma ação e identidade dentro da janela. Corresponde às linhas com `unique_event = true`.
- **Retry idempotente:** repetição com a mesma `X-Idempotency-Key` para a mesma identidade e tipo; não cria outra linha.

As janelas móveis são de 5 minutos para pesquisa, 30 minutos para visualização de perfil e 10 minutos para contato por WhatsApp. A impressão digital considera usuário ou visitante, origem e os dados relevantes da ação. Paginação não cria outra pesquisa única quando termo e filtros permanecem iguais.

Clientes devem enviar uma chave nova em `X-Idempotency-Key` para cada ação lógica e reutilizá-la somente em retries. Para tráfego anônimo, `X-Visitor-Id` identifica de forma estável a instalação ou sessão e permite deduplicação semântica entre requisições. Sem JWT e sem `X-Visitor-Id`, ações diferentes não são agrupadas para evitar unir visitantes distintos; ainda é possível eliminar um retry exato usando `X-Idempotency-Key`. Identificadores e chaves nunca são persistidos em claro nas estruturas de deduplicação, apenas hashes SHA-256.

## Dashboard administrativo

`GET /api/v1/admin/dashboard/funnel?from=2026-08-01&to=2026-08-31&timezone=America/Sao_Paulo` exige role `ADMIN` ou `OWNER` e retorna:

- contas concluídas, perfis iniciados e enviados a partir de `funnel_events`;
- aprovações e reprovações pela data histórica `reviewed_at` das revisões;
- pesquisas, visualizações de perfil e contatos por WhatsApp, cada um com totais `raw` e `unique`.

`from` e `to` são datas ISO-8601 obrigatórias e inclusivas no timezone informado. `timezone` aceita identificadores IANA e assume `America/Sao_Paulo`; a resposta explicita `startInclusive` e `endExclusive` com seus offsets. A consulta usa intervalo semiaberto, portanto inclui o início do primeiro dia e exclui exatamente o início do dia posterior a `to`, inclusive em transições de horário de verão. A migration `V10` adiciona os índices usados pelas agregações.

## Autorização e privacidade

Todas as rotas sob `/api/v1/admin/**` exigem `ADMIN` ou `OWNER` em duas camadas: no filtro HTTP e com `@PreAuthorize` nos controllers administrativos. A matriz esperada é:

| Identidade | Rotas administrativas |
| --- | --- |
| Anônimo | `401 Unauthorized` |
| `STUDENT` | `403 Forbidden` |
| `PERSONAL` | `403 Forbidden` |
| `ADMIN` | Permitido |
| `OWNER` | Permitido |

Nas rotas `/api/v1/me/personal-profile/**`, a identidade é sempre obtida do `sub` do JWT; identificadores de usuário enviados pelo cliente não são aceitos. Leituras usam o usuário autenticado e mutações com `profileId` consultam simultaneamente perfil e proprietário. Um perfil pertencente a outra conta recebe o mesmo `404 PROFILE_NOT_FOUND` de um identificador inexistente, evitando confirmar sua existência.

Os contratos públicos de perfil não expõem e-mail, telefone, WhatsApp, CREF ou chave de documento. O WhatsApp só é fornecido indiretamente pela URL retornada no endpoint explícito de início de contato, que também registra a métrica. Contratos administrativos de usuário podem expor dados operacionais da conta, mas nunca senha, hash, token ou segredo.

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
