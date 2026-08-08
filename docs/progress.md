# Listaih — Estado do Projeto

> Última atualização: 08 Ago 2026 (Fase 4 — Android App estruturado)

---

## Visão Geral

**Listaih** é um sistema self-hosted de listas de compras colaborativas para homelabs. O backend é o produto central — instalado no homelab, gerencia listas, sincronização em tempo real e integrações com Grocy e Home Assistant. Android, Wear OS e Alexa Skill são interfaces externas que consomem a API.

---

## Roadmap

| Fase | Foco | Status |
|---|---|---|
| 1. Core Backend | Setup wizard, auth, healthcheck, Docker, Caddy, single-household | ✅ Concluído |
| 2. WebSocket Sync | Socket.io, eventos em tempo real, Redis pub/sub | ✅ Concluído |
| 3. Admin WebUI | React 19 + Vite 8 + MUI v9, dashboard, i18n, integrações | ✅ Concluído |
| 4. App Android | Kotlin + Compose, offline-first, Wear OS | 🚧 Em desenvolvimento |
| 5. Grocy | Sync bidirecional, mapeamento de produtos | 🚧 Em desenvolvimento |
| 6. Home Assistant | Webhooks, MQTT discovery, notificações | ⬜ Pendente |
| 7. Alexa Skill | Account linking, intents, Cloudflare Tunnel | ⬜ Pendente |

---

## Decisões de Arquitetura

| Decisão | Escolha |
|---|---|
| Arquitetura | Monolito Modular (NestJS 10) |
| Tenant | Single-household (uma instalação = uma casa) |
| Backend | Node.js 22 + TypeScript + NestJS 10 + Prisma 5 |
| Banco de Dados | PostgreSQL 16 (local: PostgreSQL 17 via winget) |
| Cache/Sync | Redis 7 (não instalado localmente — SyncService falha graciosamente) |
| Auth | Email/senha + JWT (access 15m + refresh 7d) |
| Real-time | WebSockets (Socket.io) — 6 eventos de sync |
| Admin WebUI | React 19 + Vite 8 + TypeScript + MUI v9 (`@mui/material@9`) |
| Deploy | `docker compose up` (backend + postgres + redis + caddy) |
| Reverse Proxy | Caddy (HTTP :80, TLS ao configurar domínio) |
| i18n | 3 idiomas: pt-BR (padrão), en-US, es-ES |

---

## Estrutura do Monorepo

```
Listaih/
├── .env.example
├── .gitignore
├── docker-compose.yml              ✅ Backend + PostgreSQL + Redis + Caddy
├── Caddyfile                       ✅ Reverse proxy HTTP :80
├── logo.png
├── design/
│   └── mockups.html                ✅ 8 telas Phone + 6 telas Wear OS
├── docs/
│   ├── architecture.md
│   └── progress.md                 ✅ (este arquivo)
├── apps/
│   ├── admin/                      ✅ Build validado
│   │   ├── package.json
│   │   ├── vite.config.ts          base: '/admin/', alias '@' → './src'
│   │   ├── tsconfig.app.json       ignoreDeprecations: "6.0"
│   │   ├── public/
│   │   │   ├── logo.svg
│   │   │   └── icons/
│   │   │       ├── grocy.png
│   │   │       ├── home-assistant.png
│   │   │       └── amazon-alexa.png
│   │   └── src/
│   │       ├── main.tsx
│   │       ├── App.tsx             <I18nProvider> → <AuthProvider> → <RouterProvider>
│   │       ├── index.css
│   │       ├── theme/index.ts      MUI theme (Material 3)
│   │       ├── services/api.ts     Axios instance (baseURL /api, interceptors JWT)
│   │       ├── components/
│   │       │   └── Layout.tsx      Sidebar, AppBar, profile photo, language selector
│   │       ├── hooks/
│   │       │   ├── useAuth.tsx     Login, logout, updateUser, User context
│   │       │   ├── useHousehold.ts Lists, members, invite code, ShoppingList interface
│   │       │   ├── useI18n.tsx     i18n: I18nProvider, useI18n, 3 dicts (~270 keys each)
│   │       │   └── useSystemConfig.ts  useSystemConfig, formatCurrency, getCurrencySymbol
│   │       ├── utils/
│   │       │   └── categories.tsx  35 product categories (MDI icons), 5 list categories
│   │       └── pages/
│   │           ├── Login.tsx       ✅ i18n
│   │           ├── Dashboard.tsx   ✅ i18n
│   │           ├── Lists.tsx       ✅ i18n (active/archived sections, list categories)
│   │           ├── Members.tsx     ✅ i18n (avatar shows profile photo)
│   │           ├── Clients.tsx     ✅ i18n (tokens CRUD)
│   │           ├── Integrations.tsx ✅ i18n (Grocy, HA, Alexa)
│   │           └── Settings.tsx    ✅ i18n (profile, household, language+currency, security, about, danger zone)
│   ├── backend/                     ✅ Build validado
│   │   ├── Dockerfile              ✅ Multi-stage (node:22-alpine)
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json           strictPropertyInitialization: false, strictNullChecks: false
│   │   ├── nest-cli.json
│   │   ├── .env / .env.example
│   │   ├── prisma/
│   │   │   ├── schema.prisma      ✅ 10 models (User, ApiToken, RefreshToken, Household, HouseholdMember, ShoppingList, ListItem, Product, PriceEntry, SystemConfig)
│   │   │   └── migrations/
│   │   │       ├── 20260805024405_init
│   │   │       ├── 20260805210807_add_api_tokens
│   │   │       ├── 20260805231521_add_currency
│   │   │       ├── 20260806041017_add_list_category
│   │   │       └── 20260808002858_add_external_api
│   │   └── src/
│   │       ├── main.ts            json({ limit: '10mb' }), CORS, ServeStaticModule (/admin)
│   │       ├── app.module.ts       9 módulos
│   │       ├── prisma/             PrismaModule (@Global) + PrismaService
│   │       └── modules/
│   │           ├── auth/           login, refresh, logout, change-password, JWT
│   │           ├── users/          perfil, households, roles, regenerate-code
│   │           ├── lists/          CRUD listas + itens, histórico, category
│   │           ├── setup/          Setup Wizard (one-time)
│   │           ├── health/         Healthcheck (DB + Redis + integrações)
│   │           ├── sync/           SyncGateway (Socket.io) + SyncService (Redis pub/sub)
│   │           ├── system/         SystemConfig CRUD (Grocy, HA, currency)
│   │           └── tokens/         API tokens CRUD (for external clients)
│   └── android/                     🚧 Em desenvolvimento
│       ├── settings.gradle.kts
│       ├── build.gradle.kts
│       ├── gradle/libs.versions.toml
│       ├── app/
│       │   ├── build.gradle.kts
│       │   ├── src/main/
│       │   │   ├── AndroidManifest.xml
│       │   │   ├── java/com/listaih/app/
│       │   │   │   ├── ListaihApplication.kt
│       │   │   │   ├── MainActivity.kt
│       │   │   │   ├── MainViewModel.kt
│       │   │   │   ├── data/
│       │   │   │   │   ├── local/
│       │   │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   │   ├── Converters.kt
│       │   │   │   │   │   ├── entity/Entities.kt
│       │   │   │   │   │   └── dao/Daos.kt
│       │   │   │   │   ├── network/
│       │   │   │   │   │   ├── ApiService.kt
│       │   │   │   │   │   └── model/ApiModels.kt
│       │   │   │   │   ├── preferences/AppPreferences.kt
│       │   │   │   │   └── repository/ShoppingRepository.kt
│       │   │   │   ├── di/
│       │   │   │   │   ├── AppModule.kt
│       │   │   │   │   ├── NetworkModule.kt
│       │   │   │   │   └── AuthInterceptor.kt
│       │   │   │   ├── navigation/AppNavHost.kt
│       │   │   │   ├── sync/
│       │   │   │   │   ├── SyncWorker.kt
│       │   │   │   │   ├── BootReceiver.kt
│       │   │   │   │   └── SocketSyncService.kt
│       │   │   │   └── ui/
│       │   │   │       ├── theme/Theme.kt, Typography.kt
│       │   │   │       └── screens/
│       │   │   │           ├── onboarding/OnboardingScreen.kt
│       │   │   │           ├── login/LoginScreen.kt
│       │   │   │           ├── home/HomeScreen.kt
│       │   │   │           ├── detail/ListDetailScreen.kt
│       │   │   │           ├── additem/AddItemBottomSheet.kt
│       │   │   │           └── settings/SettingsScreen.kt
│       │   │   └── res/
│       │   │       ├── values/strings.xml, colors.xml, themes.xml
│       │   │       └── xml/backup_rules.xml, data_extraction_rules.xml
│       └── wear/
│           ├── build.gradle.kts
│           └── src/main/
│               ├── AndroidManifest.xml
│               └── java/com/listaih/wear/
│                   ├── WearApplication.kt
│                   ├── MainActivity.kt
│                   ├── WearMainViewModel.kt
│                   ├── navigation/WearNavHost.kt
│                   ├── ui/theme/WearTheme.kt, WearTypography.kt
│                   └── screens/
│                       ├── home/WearHomeScreen.kt
│                       ├── shopping/WearShoppingScreen.kt
│                       ├── complete/WearCompleteScreen.kt
│                       ├── select/WearSelectScreen.kt
│                       └── voice/WearVoiceScreen.kt
└── packages/
    └── shared/                     ⬜ (tipos/schemas compartilhados — futuro)
```

---

## Modelo de Dados (Prisma)

### Models (10)
| Model | Descrição |
|---|---|
| `SystemConfig` | Config singleton (isSetup, adminUserId, grocyUrl, grocyApiKey, haUrl, haWebhookToken, currency) |
| `User` | email, name, passwordHash, avatar, provider, googleId?, appleId? |
| `ApiToken` | name, token, tokenHash, prefix, type, userId, lastUsedAt, revokedAt |
| `RefreshToken` | token, userId, expiresAt |
| `Household` | name, inviteCode |
| `HouseholdMember` | householdId, userId, role (ADMIN/EDITOR/VIEWER) |
| `ShoppingList` | name, householdId, category?, template, archivedAt |
| `ListItem` | listId, name, quantity, unit, estimatedPrice, actualPrice, category, checked, position |
| `Product` | name, barcode?, category?, defaultUnit |
| `PriceEntry` | productId, storeName?, price, date, userId |

### Enums (2)
| Enum | Valores |
|---|---|
| `Unit` | unit, kg, g, L, ml |
| `HouseholdRole` | ADMIN, EDITOR, VIEWER |

---

## APIs Implementadas

### Setup (`/api/setup`) — Público
| Método | Rota | Descrição |
|---|---|---|
| GET | `/setup/status` | Verificar se o sistema foi configurado |
| POST | `/setup` | Configuração inicial (cria admin + household) |

### Health (`/api/health`) — Público
| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Status do DB, Redis e integrações |

### Auth (`/api/auth`)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/login` | Login com email/senha | ❌ |
| POST | `/auth/refresh` | Renovar access token | ❌ |
| POST | `/auth/logout` | Invalidar refresh token | ✅ JWT |
| POST | `/auth/change-password` | Trocar senha | ✅ JWT |

### Users (`/api/users`)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/users/me` | Perfil completo do usuário | ✅ JWT |
| PUT | `/users/me` | Atualizar nome/avatar | ✅ JWT |
| GET | `/users/households` | Listar households | ✅ JWT |
| POST | `/users/households/join` | Entrar em household via inviteCode | ✅ JWT |
| GET | `/users/households/:id/members` | Listar membros | ✅ JWT |
| PATCH | `/users/households/:id/members/:memberId` | Alterar role | ✅ JWT |
| DELETE | `/users/households/:id/members/:memberId` | Remover membro | ✅ JWT |
| PATCH | `/users/households/:id/regenerate-code` | Regenerar invite code | ✅ JWT |

### Lists (`/api/`)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/households/:id/lists` | Listar listas ativas | ✅ JWT |
| POST | `/households/:id/lists` | Criar lista (com category) | ✅ JWT |
| GET | `/lists/:id` | Detalhe da lista com itens | ✅ JWT |
| PUT | `/lists/:id` | Atualizar lista (nome, category, archivedAt) | ✅ JWT |
| DELETE | `/lists/:id` | Excluir lista | ✅ JWT |
| POST | `/lists/:id/items` | Adicionar item | ✅ JWT |
| PATCH | `/lists/:id/items/:itemId` | Atualizar item (auto-calc actualPrice quando checked) | ✅ JWT |
| DELETE | `/lists/:id/items/:itemId` | Remover item | ✅ JWT |
| GET | `/households/:id/history` | Listas arquivadas | ✅ JWT |

### System (`/api/system`)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/system/config` | Obter config do sistema | ✅ JWT |
| PUT | `/system/config` | Atualizar config (grocyUrl, grocyApiKey, haUrl, haWebhookToken, currency) | ✅ JWT |

### Tokens (`/api/tokens`)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/tokens` | Listar tokens do usuário | ✅ JWT |
| POST | `/tokens` | Criar novo token | ✅ JWT |
| DELETE | `/tokens/:id` | Revogar token | ✅ JWT |

### WebSocket (Socket.io)
| Evento | Direção | Descrição |
|---|---|---|
| `item_added` | Server → Client | Item adicionado a uma lista |
| `item_updated` | Server → Client | Item atualizado |
| `item_removed` | Server → Client | Item removido |
| `list_created` | Server → Client | Lista criada |
| `list_updated` | Server → Client | Lista atualizada |
| `list_deleted` | Server → Client | Lista excluída |

---

## Sistema de i18n (Admin WebUI)

- **Arquivo:** `apps/admin/src/hooks/useI18n.tsx`
- **Provider:** `<I18nProvider>` envolve toda a app em `App.tsx`
- **Hook:** `useI18n()` → `{ lang, setLang, t }`
- **Função `t(key)`:** Busca no dicionário do idioma atual, fallback pt-BR, fallback key
- **Idiomas:** pt-BR (padrão), en-US, es-ES
- **Storage:** `localStorage` key `listaih_language`
- **~270 chaves** por dicionário, cobrindo: nav, layout, profile, pw, login, dashboard, lists, members, clients, integrations, settings
- **Seletor de idioma:** Presente na sidebar (Layout.tsx) e na página de Settings ("Localização & Moeda")

---

## Sistema de Categorias (Admin WebUI)

- **Arquivo:** `apps/admin/src/utils/categories.tsx`
- **5 list categories:** Alimentos, Farmacia (label "Farmácia"), Papelaria, Material de Construcao (label "Material de Construção"), Geral
- **35 product categories** com ícones MDI (`@mui/icons-material`)
- `getProductCategoriesForList(listCategory)` filtra produtos por categoria de lista
- `getListCategoryIcon(name)` retorna ícone da categoria de lista
- `getListCategoryLabel(name)` retorna label traduzido (mas atualmente hardcoded pt-BR)
- Identifiers ASCII-only (sem acentos) para evitar problemas de encoding

---

## Convenções e Padrões Importantes

### Backend
- `PrismaModule` é `@Global()` — todos os módulos acessam PrismaService sem import explícito
- `tsconfig.json`: `strictPropertyInitialization: false`, `strictNullChecks: false`
- Backend **NÃO** usa aliases `@` nos paths
- `main.ts`: `app.use(json({ limit: '10mb' }))` para permitir upload de avatar (data URL)
- ServeStaticModule: `serveRoot: '/admin'`, `rootPath: join(__dirname, '..', '..', 'admin', 'dist')`
- `updateItem` auto-calcula `actualPrice = estimatedPrice * quantity` quando `checked: true`
- `generateTokens()` retorna objeto completo do user: `{ id, email, name, avatar }`
- `UpdateProfileDto`: `@IsOptional()` em `name` e `avatar`

### Admin Frontend
- Vite `base: '/admin/'` (todos os assets têm prefixo `/admin/`)
- Alias `@` → `./src` (configurado em `vite.config.ts` e `tsconfig.app.json`)
- `tsconfig.app.json`: `ignoreDeprecations: "6.0"`
- MUI v9: `TextField` usa `slotProps.input` (não `InputProps`); `slotProps.htmlInput` para `readOnly`; `slotProps.select` para select props
- **NO `tertiary` in PaletteOptions** — usar primary, secondary, error, warning, info, success
- Imagens: usar paths relativos (`./logo.svg`, `./icons/grocy.png`) — Vite injeta base automaticamente
- `useSystemConfig`: `formatCurrency(value, currency)` usa símbolos (R$, $, etc.); `getCurrencySymbol(currency)` exportado
- Backend lists endpoints retornam arrays simples (não `{ value, Count }`) — `Array.isArray` check no frontend
- Variável `t` do i18n: evitar shadowing (ex: renomear para `tk` em `.map()`)

### Android
- **Arquitetura:** Offline-first com Room + Repository pattern + WorkManager sync
- **DI:** Hilt para injeção de dependência (Application, ViewModels, Workers)
- **Navegação:** Navigation Compose com type-safe routes
- **Temas:** Material 3 com cores do design system (primary #006B3C)
- **Dados:** Room (7 entities) + DataStore Preferences (auth, settings)
- **Rede:** Retrofit + Kotlinx Serialization + OkHttp interceptors (JWT auth)
- **Real-time:** Socket.io client para WebSocket sync (6 eventos)
- **Sync:** WorkManager periodic (15 min) + fila local (SyncQueueEntity)
- **Wear OS:** Module separado com Compose for Wear OS (ScalingLazyColumn, Vignette, CircularProgressIndicator)
- **Build:** AGP 8.4.2, Kotlin 1.9.22, Compose Compiler 1.5.11, minSdk 26/30

### Credenciais de teste (local)
- PostgreSQL: user `listaih`, password `listaih`, database `listaih`, port 5432
- Admin user: `admin@exemplo.com` / `senha123`

### Limitações do ambiente de desenvolvimento
- Docker não instalado — `docker compose up` não testável
- Redis não instalado — SyncService emite WARN logs, WebSocket sync não testável
- PowerShell `Invoke-RestMethod` pode corromper caracteres UTF-8 com acentos em testes de API (browser funciona fine)

---

## Status Detalhado — Fase 3 (Admin WebUI)

### ✅ Concluído
- **Projeto React + Vite + TypeScript + MUI v9** criado e configurado
- **ServeStaticModule** no backend servindo admin em `/admin/`
- **Login.tsx** — tela de login com i18n, logo, self-hosted badge
- **Layout.tsx** — sidebar com navegação, AppBar, profile photo (resize via canvas antes de upload), seletor de idioma na sidebar, menu de perfil (editar, alterar senha, sair)
- **Dashboard.tsx** — stat cards (listas ativas, itens pendentes, valor estimado, itens marcados), listas recentes com click → navegação, status das integrações, formatCurrency
- **Lists.tsx** — CRUD completo, itens agrupados por categoria, seletor de categoria de lista, diálogo de edição com category+name, seções separadas para ativas e arquivadas, arquivar/restaurar/excluir
- **Members.tsx** — role change (admin/editor/viewer), remover membro, copiar invite code, avatar mostra foto de perfil
- **Clients.tsx** — tokens CRUD (GET, POST, DELETE), token exibido apenas na criação, warning de copiar agora
- **Integrations.tsx** — Grocy (url, apiKey, testar conexão), Home Assistant (url, webhookToken), Alexa (placeholder futuro), wired to `/system/config`
- **Settings.tsx** — perfil (nome, email), alterar senha, household (nome, invite code, regenerar), localização & moeda (seletor de idioma + moeda), segurança (HTTPS, rate limiting — disabled), backup (export, auto — disabled), sobre, zona de perigo (disabled)
- **i18n** — todas as 7 páginas + Layout traduzidas, 3 idiomas (~270 keys cada)
- **Sistema de categorias** — 2 tiers (5 list categories + 35 product categories), ícones MDI, filtros
- **Image paths fixed** — todos `src` usam paths relativos
- **Profile photo upload fixed** — canvas resize 256×256 JPEG, json limit 10mb, @IsOptional no DTO
- **Spelling corrections** — acentuação corrigida em todos os arquivos (Integrações, Configurações, Ações, etc.)

### Builds
- Admin: `npm run build` em `apps/admin` → ✅ sem erros
- Backend: `npx nest build` em `apps/backend` → ✅ sem erros
- Backend rodando local: `node dist/main.js` na porta 3000

---

## Status Detalhado — Fase 4 (App Android)

### ✅ Concluído (Estrutura base)
- **Projeto Kotlin + Jetpack Compose** criado em `apps/android/` com Gradle Version Catalogs
- **Módulos:** `app` (Phone) + `wear` (Wear OS)
- **Dependências:** Material 3, Room, DataStore, Retrofit, Socket.io, WorkManager, Hilt, Navigation Compose, Coil, Accompanist
- **Data Layer:**
  - Room database com 7 entities (ShoppingList, ListItem, Product, PriceEntry, Household, User, SyncQueue)
  - DAOs para todas as entities com Flow support
  - DataStore Preferences para auth tokens, base URL, settings
  - Repository pattern combinando local + remote
- **Network Layer:**
  - Retrofit API service com todos os endpoints do backend
  - AuthInterceptor para JWT automático
  - SocketSyncService para WebSocket real-time sync
- **Sync Layer:**
  - SyncWorker (WorkManager) para periodic sync (15 min)
  - BootReceiver para agendar sync no boot
  - SyncQueueEntity para offline-first queue
- **UI Screens (Phone):**
  - OnboardingScreen — 4 passos com ilustrações
  - LoginScreen — Email/senha + Google + Apple
  - HomeScreen — Listas com chips filter, FAB, BottomNav (Home/Histórico/Config)
  - ListDetailScreen — Itens agrupados por categoria, progress ring, total row, FAB add item
  - AddItemBottomSheet — Nome, qty, price, unit chips, category chips, sugestões
  - SettingsScreen — Perfil, Casa, Idioma/Moeda, Tema, Notificações, Offline, Backup, Sobre, Danger Zone
- **UI Screens (Wear OS):**
  - WearHomeScreen — Progress ring, lista de listas, actions (Ver todas, Voz)
  - WearShoppingScreen — ScalingLazyColumn, categorias, chips, CircularProgressIndicator
  - WearCompleteScreen — Animação de conclusão, total gasto
  - WearSelectScreen — Lista de listas com badges
  - WearVoiceScreen — Microfone, reconhecimento simulado
- **Temas:** Material 3 (Phone + Wear) com cores do design system

### 🚧 Em andamento
- Integração completa Repository → UI (ViewModels)
- Testes de build e execução
- Socket.io real-time handlers
- Sincronização bidirecional completa

### Próximos passos imediatos
1. Validar build: `cd apps/android && ./gradlew assembleDebug`
2. Implementar ViewModels para cada tela
3. Conectar UI ao Repository
4. Testar sync offline-first
5. Implementar Wear OS complication/tiles

---

## Próximos Passos (Retomada)

### Imediato (validação)
1. Testar o admin em `http://localhost:3000/admin/` com Ctrl+F5
2. Verificar se o seletor de idioma funciona em Settings e na sidebar
3. Verificar se a troca de idioma traduz todas as páginas
4. **Build do Android:** `cd apps/android && ./gradlew assembleDebug`

### Fase 4 — App Android (continuação)
1. Implementar ViewModels para Home, Detail, AddItem, Settings
2. Conectar UI screens ao ShoppingRepository
3. Testar sync offline-first com WorkManager
4. Implementar handlers Socket.io para real-time updates
5. Testar Wear OS no emulador

### Melhorias futuras (não bloqueantes)
- Code-splitting no admin (chunks > 500kb warning do Vite)
- `getListCategoryLabel()` poderia usar i18n em vez de hardcoded pt-BR
- Backup/exportação de dados (botões atualmente disabled)
- Zona de perigo (limpar arquivadas, resetar integrações, resetar sistema — disabled)

---

## Como Rodar

### Desenvolvimento Local
```bash
# Pré-requisitos: PostgreSQL local rodando (user: listaih, pw: listaih, db: listaih, port: 5432)

# Backend
cd apps/backend
npm install
npx prisma generate
npx prisma migrate deploy   # ou npx prisma migrate dev
npx nest build
node dist/main.js            # http://localhost:3000/api

# Admin (build + serve via backend)
cd apps/admin
npm install
npm run build               # gera apps/admin/dist/
# O backend serve em http://localhost:3000/admin/
```

### Login de teste
- URL: `http://localhost:3000/admin/`
- Email: `admin@exemplo.com`
- Senha: `senha123`
