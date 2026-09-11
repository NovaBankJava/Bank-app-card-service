# Bank App · Card Service

> Microsserviço de **cartão virtual** do NovaBank. Gera cartões virtuais, controla seu ciclo de vida, vincula limites e segrega transações — de forma segura e isolada da conta.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.7-brightgreen)
![Maven](https://img.shields.io/badge/build-Maven-blue)
![Architecture](https://img.shields.io/badge/architecture-Hexagonal-purple)
![OpenAPI](https://img.shields.io/badge/docs-Swagger%20UI-85EA2D)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)

---

## Sumário

- [Sobre](#sobre)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Como rodar](#como-rodar)
- [Perfis e variáveis de ambiente](#perfis-e-variáveis-de-ambiente)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Padrão de resposta](#padrão-de-resposta)
- [Segurança dos dados do cartão](#segurança-dos-dados-do-cartão)
- [Roadmap de issues](#roadmap-de-issues)
- [Convenções do projeto](#convenções-do-projeto)
- [Testes](#testes)

---

## Sobre

Este serviço é responsável por tudo que diz respeito a **cartões virtuais**: geração, ativação/revogação, vínculo de limite e segregação das transações feitas nele. Faz parte do ecossistema de microsserviços do NovaBank, ao lado de serviços como `Bank-app-account-service` e `Bank-app-user-service`.

Como serviço independente, ele **não compartilha banco nem classes** com os demais. O vínculo com usuário e conta é feito por referência (ids), e a consistência entre serviços é responsabilidade de quem orquestra as chamadas — não deste serviço.

> **Nota sobre os dados do cartão:** os cartões gerados são **fictícios** (BIN de teste). Eles são válidos pelo algoritmo de Luhn, mas não são emitidos por nenhuma bandeira real e não funcionam em compras de verdade. O objetivo é implementar os **padrões de segurança** de manuseio de dados de cartão, não emitir cartões reais.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Persistência | Spring Data JPA |
| Banco (dev) | H2 (em memória) |
| Banco (prod) | PostgreSQL |
| Mapeamento | MapStruct |
| Boilerplate | Lombok |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc |
| Build | Maven (via wrapper `mvnw`) |
| Container | Docker (multi-stage) |

---

## Arquitetura

O serviço segue **arquitetura hexagonal (ports & adapters)**. A regra central: o **domínio não conhece infraestrutura**. Casos de uso dependem de *interfaces* (ports); os detalhes (banco, criptografia, HTTP) ficam em *adapters* que implementam essas interfaces.

```
                 entrada (in)                         saída (out)
   ┌───────────────────────────┐          ┌───────────────────────────┐
   │  CardController (REST)     │          │  CardRepositoryAdapter     │
   │  DTOs · Mappers            │          │  (JPA + criptografia)      │
   └─────────────┬─────────────┘          └─────────────▲─────────────┘
                 │ implementa/usa                        │ implementa
        ┌────────▼────────┐   depende de   ┌─────────────┴─────────────┐
        │ GenerateCard    │───────────────▶│  ports.in / ports.out     │
        │ UseCase (impl)  │                │  (interfaces do domínio)   │
        └────────┬────────┘                └───────────────────────────┘
                 │ usa
        ┌────────▼────────────────────────────────────┐
        │  domain.model: Card (imutável), Luhn         │
        └──────────────────────────────────────────────┘
```

**Por que assim?** Porque o caso de uso de geração pode ser testado sem banco, sem HTTP e sem criptografia — bastam mocks das interfaces. Trocar Postgres por outro banco, ou o esquema de cifra, não toca o domínio.

---

## Estrutura de pastas

```
src/main/java/org/example/bankappcardservice/
├── domain/
│   ├── model/            # Card (imutável), Luhn (algoritmo puro)
│   ├── ports/
│   │   ├── in/           # portas de entrada (ex.: GenerateCardUseCase)
│   │   └── out/          # portas de saída (ex.: CardRepositoryPort)
│   └── exception/        # exceções de domínio
├── application/
│   └── usecase/          # implementação dos casos de uso
└── infra/
    ├── adapter/
    │   ├── in/           # controller, dto, mapper (HTTP)
    │   └── out/          # mappers de saída
    ├── config/           # beans e configuração (CardConfig, OpenApiConfig)
    ├── integration/      # clientes de outros serviços (futuro)
    └── repository/       # persistência
        ├── entity/       # @Entity JPA + conversores
        └── crypto/       # criptografia (AES-GCM) + blind index (HMAC)
```

---

## Como rodar

Pré-requisitos: **Java 21**. Não é necessário instalar o Maven — use o wrapper.

```bash
# Linux / macOS / WSL
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

Sobe no perfil `dev` (H2 em memória) em `http://localhost:8080`.

Rodar os testes:

```bash
./mvnw clean test          # Linux/macOS/WSL
.\mvnw.cmd clean test      # Windows
```

Rodar com Docker (perfil `prod`, exige as variáveis de ambiente abaixo):

```bash
docker build -t bank-app-card-service .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/carddb \
  -e DB_USER=... -e DB_PASSWORD=... \
  -e CARD_AES_KEY=... -e CARD_HMAC_KEY=... \
  bank-app-card-service
```

---

## Perfis e variáveis de ambiente

| Perfil | Banco | Uso |
|---|---|---|
| `dev` (padrão) | H2 em memória | Desenvolvimento local. Chaves de criptografia de teste embutidas. |
| `prod` | PostgreSQL | Produção. Todas as chaves e credenciais vêm de variáveis de ambiente. |

Variáveis lidas pelo serviço:

| Variável | Descrição | Perfil |
|---|---|---|
| `DB_URL` | URL JDBC do Postgres | prod |
| `DB_USER` | Usuário do banco | prod |
| `DB_PASSWORD` | Senha do banco | prod |
| `CARD_AES_KEY` | Chave AES-256 em base64 (32 bytes) para cifrar o PAN | dev/prod |
| `CARD_HMAC_KEY` | Chave HMAC em base64 (32 bytes) para o blind index | dev/prod |
| `CARD_BIN` | BIN fictício (6 primeiros dígitos). Padrão: `400000` | dev/prod |

> As chaves do perfil `dev` são fixas apenas para o app subir localmente. **Nunca** use essas chaves — nem qualquer chave versionada — em produção. Gere as suas com `openssl rand -base64 32`.

---

## Documentação da API (Swagger)

Com o serviço rodando:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Endpoints

| Método | Rota | Descrição | Issue |
|---|---|---|---|
| `POST` | `/api/v1/cards/generateCard` | Gera um cartão virtual | [#8](#8--geração-segura-de-númerocvvvalidade) ✅ |
| `PATCH` | `/api/v1/cards/activate/{cardId}` | Ativa um cartão | [#9](#9--ativaçãorevogação-de-cartão-virtual) 🚧 |
| `PATCH` | `/api/v1/cards/revoke/{cardId}` | Revoga um cartão | [#9](#9--ativaçãorevogação-de-cartão-virtual) 🚧 |
| ... | ... | (demais rotas conforme as issues avançam) | |

---

## Padrão de resposta

Todo endpoint responde com **HTTP 200**. O resultado é sinalizado pelo campo `status` dentro do envelope `ApiResponse<T>` — o mesmo padrão usado pelos outros serviços do NovaBank.

```json
{
  "status": 0,
  "description": "OK",
  "data": {
    "id": "12676868-7aab-4476-8f17-5e10eec1c5b8",
    "number": "4000000965550843",
    "cvv": "776",
    "expiry": "2030-09"
  }
}
```

Códigos de `status`:

| Código | Significado |
|---|---|
| `0` | Sucesso |
| `1` | Entrada inválida |
| `3` | Falha na geração do cartão |
| `99` | Erro inesperado |

> Os códigos genéricos (`0`, `1`) seguem o padrão dos demais serviços. Os específicos de cartão (`3`, `99`) estão sujeitos a alinhamento com o time para evitar colisão de significado entre serviços.

---

## Segurança dos dados do cartão

O tratamento dos dados sensíveis segue os princípios do PCI DSS aplicáveis a um projeto de estudo (não é uma certificação real):

- **PAN (número) cifrado em repouso** com **AES-256-GCM** e IV aleatório por gravação. Como o texto cifrado muda a cada escrita, ele não pode ser indexado nem consultado diretamente.
- **Blind index** — um **HMAC-SHA256** determinístico do número, guardado em coluna própria. É ele que carrega a constraint `UNIQUE` e permite checar duplicidade e fazer lookups, sem expor o número.
- **CVV nunca é persistido.** É gerado e retornado **uma única vez**, no momento da criação. Não existe coluna de CVV no banco (alinhado ao PCI DSS req. 3.2).
- **Mascaramento:** apenas os **últimos 4 dígitos** (`last4`) ficam em claro, para exibição.
- **Sem log de dado sensível:** o PAN completo e o CVV nunca são escritos em log.

---

## Roadmap de issues

O microsserviço é construído de forma colaborativa, uma issue por vez. Cada issue abaixo é uma fatia funcional entregue via PR.

### #8 — Geração segura de número/CVV/validade
`feat` · **✅ concluída**

> **História:** como usuário do app, quero gerar um cartão virtual com dados próprios, para fazer compras online com mais segurança.

| RF | Descrição | Situação |
|---|---|---|
| RF01 | Gerar número, CVV e validade únicos e válidos (Luhn) | ✅ |
| RF02 | Armazenar dados sensíveis tokenizados/criptografados | ✅ (AES-GCM + blind index) |
| RF03 | Vincular o cartão ao usuário e à conta principal | ✅ (por referência: `userId`/`accountId`) |
| RF04 | Seguir padrões PCI DSS na geração/armazenamento | ⚠️ Reinterpretado — ver nota abaixo |

**Nota de escopo (RF04):** conformidade PCI DSS de verdade não se aplica a cartões fictícios (exige BIN real, auditoria, ambiente certificado). O RF04 foi convertido em **critérios concretos e testáveis**: PAN cifrado em repouso, CVV não persistido, mascaramento e ausência de log de dado sensível. Ver [Segurança dos dados do cartão](#segurança-dos-dados-do-cartão).

**Nota de escopo (RF03):** o serviço apenas **persiste** `userId` e `accountId` como referência. A validação de que a conta existe e pertence ao usuário (e que é a conta "principal") depende de outro serviço e está **fora do escopo desta issue**.

---

### #9 — Ativação/revogação de cartão virtual
`feat` · **🚧 aberta**

> **História:** como usuário do app, quero poder ativar ou revogar meu cartão virtual quando quiser, para ter controle total sobre seu uso.

| RF | Descrição |
|---|---|
| RF01 | Endpoint para ativação do cartão gerado |
| RF02 | Endpoint para revogação (cancelamento) imediata |
| RF03 | Impedir novas transações em cartão revogado a partir da revogação |
| RF04 | Permitir gerar um novo cartão após revogação do anterior |

**Pontos de atenção:** introduz uma **máquina de estados** do cartão (ex.: `ACTIVE` → `REVOKED`). O RF03 cria acoplamento com o fluxo de transação (#11): a autorização precisa consultar o status do cartão — via chamada síncrona ou evento + cache. Depende do campo de status na entidade, que já pode ser previsto na modelagem.

---

### #10 — Vínculo de limite (próprio ou compartilhado)
`feat` · **🚧 aberta**

> **História:** como usuário do app, quero escolher se meu cartão virtual usa um limite próprio ou compartilha o limite do cartão físico, para organizar melhor meus gastos.

| RF | Descrição |
|---|---|
| RF01 | Configurar o cartão com limite próprio (valor definido pelo usuário) |
| RF02 | Configurar o cartão como compartilhado com o limite do cartão físico |
| RF03 | Validar que a soma dos limites próprios não ultrapasse o limite total da conta |

**Pontos de atenção:** o RF03 exige conhecer o limite total da conta, que vive em outro serviço/contexto (as issues #01–#07 do repositório tratam de limite). Provavelmente envolve integração (`infra/integration/`) ou recebe o limite disponível como entrada. Vale alinhar com o time onde essa validação mora.

---

### #11 — Registro e segregação de transações do cartão virtual
`feat` · **🚧 aberta**

> **História:** como usuário do app, quero ver separadamente as transações feitas no meu cartão virtual, para diferenciar meus gastos online dos gastos no cartão físico.

| RF | Descrição |
|---|---|
| RF01 | Marcar cada transação com a origem (físico ou virtual, e qual cartão virtual) |
| RF02 | Endpoint de consulta de transações filtrado por tipo de cartão |
| RF03 | Consolidar o total gasto por cartão virtual no período da fatura |

**Pontos de atenção:** a maior parte desta issue depende de **onde as transações são armazenadas** — provavelmente num serviço de transação/fatura, não neste. Este serviço tende a expor a identidade e o status do cartão; a marcação e a consulta das transações ficam no serviço dono delas, referenciando o `cardId`. Definição de fronteira a alinhar com o time.

---

## Convenções do projeto

- **Código, comentários e Javadoc em inglês.** Documentação de projeto (este README, issues) em português.
- **Domínio imutável:** modelos de domínio usam `@Getter` (Lombok) com campos `final`, não `@Data`.
- **Entidades JPA** usam `@Data`.
- **MapStruct** para mapeamentos "campo a campo" (DTO ↔ domínio). Transformações com lógica (criptografia, derivações) ficam em código explícito, não em MapStruct.
- **Ids como `String`.** Sem foreign keys entre serviços.
- **Nunca 404:** toda resposta é HTTP 200 com o envelope `ApiResponse`.
- **Rotas nomeadas** (ex.: `/generateCard`), versionadas sob `/api/v1`.
- **Logs** em pontos relevantes dos casos de uso (`@Slf4j`).
- **Validação em profundidade:** no DTO (`@Valid`) e no caso de uso.
- **TDD:** testes escritos junto (ou antes) da implementação.

---

## Testes

```bash
./mvnw clean test
```

Cobertura atual (#8):

| Suíte | O que valida |
|---|---|
| `LuhnTest` | Validação e cálculo do dígito verificador |
| `GenerateCardServiceTest` | Geração determinística (random/clock injetados), unicidade, retry, vínculo |
| `CardCipherTest` | Cifra não-determinística, blind index determinístico, round-trip |
| `CardControllerTest` | Endpoint responde 200 com envelope; erro de validação vira `status: 1` |

---

<p align="center"><sub>NovaBank · Card Service · construído com arquitetura hexagonal e ☕</sub></p>
