<div align="center">
  <img src="assets/Transparente%20FitterApp%20Logo.png" alt="Símbolo do FitterApp" width="180" />

  # FitterApp

  **Encontre o personal certo para o seu próximo treino.**

  Marketplace fitness local para conectar alunos e personal trainers.

  Projeto em desenvolvimento · Validação inicial em Umuarama — PR
</div>

## Sobre o produto

O FitterApp nasce para facilitar a descoberta de personal trainers próximos, reunindo em um único lugar informações que normalmente estão espalhadas em redes sociais, indicações e conversas.

Pelo aplicativo, o aluno poderá pesquisar profissionais, comparar modalidades, regiões de atendimento e preços iniciais, conhecer o perfil de cada personal e iniciar uma conversa diretamente pelo WhatsApp.

Para o personal, o FitterApp oferece uma presença profissional organizada e uma nova forma de ser encontrado por potenciais alunos da sua região.

## Objetivo do MVP

O primeiro lançamento será enxuto e terá como objetivo validar este ciclo:

```text
Usuário cria uma conta
    ↓
Pesquisa e encontra um personal
    ↓
Analisa o perfil profissional
    ↓
Inicia contato pelo WhatsApp
    ↓
O FitterApp registra a conversão
```

A validação começará em Umuarama, permitindo acompanhar o uso de perto, conversar com os primeiros usuários e evoluir o produto a partir de contatos reais.

## Funcionalidades do MVP

### Aluno

- Registro e confirmação de e-mail.
- Login, logout e recuperação de senha.
- Catálogo de personal trainers.
- Pesquisa por nome.
- Filtros por modalidade, bairro e tipo de atendimento.
- Perfil profissional detalhado.
- Contato direto pelo WhatsApp.

### Personal trainer

- Conta única com contextos de aluno e personal.
- Criação e edição do perfil profissional.
- Seleção de modalidades e regiões de atendimento.
- Envio do perfil para aprovação.
- Acompanhamento da análise.
- Correção e reenvio de perfil reprovado.

### Administração

- Painel administrativo web.
- Consulta de usuários e perfis profissionais.
- Aprovação e reprovação de personais.
- Suspensão e reativação de perfis.
- Cadastro manual de profissionais.
- Gerenciamento de modalidades.
- Métricas básicas do funil de validação.

## Fora do MVP

Para proteger o prazo e validar primeiro o núcleo do negócio, não fazem parte da versão inicial:

- Agendamento e disponibilidade.
- Avaliações.
- Marketplace de academias.
- Vínculo entre personal e academia.
- Chat interno.
- Pagamentos e assinaturas.
- Mapas e cálculo de distância.
- Notificações push.

Esses recursos serão avaliados em versões posteriores, de acordo com os dados do piloto.

## Aplicações

O projeto é formado por quatro aplicações independentes que compartilham a mesma identidade e, quando necessário, a mesma API:

| Diretório | Aplicação | Responsabilidade |
|---|---|---|
| `api/` | API REST | Autenticação, autorização, regras de negócio, persistência e métricas |
| `mobile/` | Aplicativo React Native | Experiência principal de alunos e personais |
| `frontend/` | Painel React | Administração e moderação do marketplace |
| `landing/` | Landing page | Apresentação pública, captação e divulgação do produto |
| `assets/` | Identidade visual | Arquivos-fonte da marca e referências visuais |

## Arquitetura

```text
                     ┌────────────────────┐
                     │ Landing page       │
                     │ React + Vite       │
                     └────────────────────┘

┌────────────────────┐                   ┌────────────────────┐
│ Painel admin       │                   │ Aplicativo mobile  │
│ React + Vite       │                   │ React Native/Expo  │
└─────────┬──────────┘                   └─────────┬──────────┘
          │                                        │
          └──────────────────┬─────────────────────┘
                             │ REST/JSON
                   ┌─────────▼──────────┐
                   │ API Spring Boot    │
                   │ Monólito modular   │
                   └─────────┬──────────┘
                             │
                   ┌─────────▼──────────┐
                   │ PostgreSQL         │
                   └────────────────────┘
```

As regras de negócio e permissões ficam centralizadas na API. O painel e o aplicativo são clientes distintos do mesmo backend.

## Stack

### API

- Java 21.
- Spring Boot 3.5.x.
- Spring Web MVC.
- Spring Data JPA.
- Spring Security.
- JWT com access token e refresh token rotativo.
- Bean Validation.
- PostgreSQL.
- Flyway.
- MapStruct.
- OpenAPI e Swagger UI.
- Maven Wrapper.
- JUnit, Spring Security Test e Testcontainers.

### Painel administrativo

- React.
- TypeScript.
- Vite.
- Tailwind CSS.
- TanStack Query.
- React Hook Form.
- Zod.
- Axios.

### Landing page

- React.
- TypeScript.
- vinext + Vite.
- Tailwind CSS.
- Build compatível com Cloudflare Workers.

### Mobile

- React Native.
- Expo.
- TypeScript.
- Expo Router.
- Expo Secure Store.
- TanStack Query.
- React Hook Form.
- Zod.
- Axios.

### Infraestrutura

- Docker Compose para o ambiente local.
- PostgreSQL em container.
- API documentada com OpenAPI 3.
- Aplicações organizadas por contexto e microdomínio.

## Organização do código

### API por microdomínio

```text
api/src/main/java/com/fitterapp/
├── common/
├── auth/
├── user/
├── personal/
├── modality/
└── analytics/
```

Cada microdomínio concentra seus controllers, DTOs, entidades, mappers, repositories, services e validadores.

### Clientes por feature

```text
src/
├── app/
├── common/
│   ├── components/
│   ├── hooks/
│   ├── services/
│   ├── theme/
│   └── utils/
├── features/
│   ├── auth/
│   ├── marketplace/
│   └── personal/
└── assets/
```

Componentes compartilhados são agrupados por responsabilidade, enquanto componentes específicos permanecem dentro de sua feature.

## Identidade visual

O FitterApp utiliza uma estética esportiva, urbana e tecnológica:

| Token | Cor | Uso principal |
|---|---|---|
| Preto principal | `#080808` | Fundos |
| Preto secundário | `#111111` | Cards e superfícies |
| Branco quente | `#F6F4EE` | Texto e superfícies claras |
| Verde-limão | `#C7FF3D` | Ações e identidade |
| Violeta elétrico | `#7657FF` | Destaques secundários |

- **Títulos:** Manrope.
- **Interface e textos:** DM Sans.
- **Componentes:** superfícies arredondadas, alto contraste e movimento discreto.

As diretrizes completas de identidade visual são mantidas localmente e não fazem parte do repositório remoto.

## Estrutura do repositório

```text
FitterApp/
├── api/
├── assets/
├── frontend/
├── landing/
├── mobile/
└── README.md
```

Planejamentos, anotações e o guia visual detalhado são mantidos localmente e não fazem parte do repositório remoto.

## Desenvolvimento local

Os projetos ainda serão inicializados. Depois do scaffold, esta seção será atualizada com os comandos definitivos para:

- iniciar o PostgreSQL;
- executar as migrations;
- iniciar a API;
- iniciar o painel administrativo;
- iniciar a landing page;
- executar o aplicativo no emulador Android;
- rodar linters, testes e builds.

### Requisitos já definidos

- Java 21.
- Node.js LTS e npm.
- Docker Desktop com WSL 2 no Windows.
- Android Studio, Android SDK e emulador.
- Git.

## Status do projeto

O projeto está na fase de definição e preparação:

- [x] Conceito do produto.
- [x] Escopo inicial do MVP.
- [x] Stack e arquitetura.
- [x] Processo de desenvolvimento.
- [x] Style guide e símbolo da marca.
- [x] Ambiente local preparado.
- [ ] Pesquisa e validação inicial do negócio.
- [ ] Modelagem do banco e migrations.
- [ ] API.
- [ ] Painel administrativo.
- [ ] Aplicativo mobile.
- [ ] Landing page.
- [ ] Piloto em Umuarama.

## Roadmap inicial

### MVP 1.0

Autenticação, perfis profissionais, aprovação administrativa, catálogo, filtros, WhatsApp e métricas.

### MVP 1.1

Pequenas melhorias condicionadas à conclusão e estabilidade do MVP 1.0, como favoritos, compartilhamento de perfil e denúncia básica.

### Versões posteriores

Agendamento, avaliações, academias, vínculos entre personal e academia e novas formas de monetização serão planejados a partir dos resultados da validação.

---

<div align="center">
  <strong>FitterApp</strong><br />
  Performance, proximidade e tecnologia para conectar pessoas ao treino certo.
</div>
