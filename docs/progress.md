# Listaih — Estado do Projeto

> Última atualização: 10 Ago 2026 (Fase 6 Android — popup pós-scan + associação validados no device; repo no GitHub)

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
| 4. App Android | Kotlin + Compose, offline-first, Wear OS | 🚧 Em desenvolvimento (Fases 1–8 do plano concluídas; falta 9 — Wear OS) |
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
│   │       ├── Settings.tsx    ✅ i18n (profile, household, language+currency, security, about, danger zone)
│   │       └── Purchases.tsx    ✅ i18n (purchase history, detail, checkout, edit)
│   ├── backend/                     ✅ Build validado
│   │   ├── Dockerfile              ✅ Multi-stage (node:22-alpine)
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json           strictPropertyInitialization: false, strictNullChecks: false
│   │   ├── nest-cli.json
│   │   ├── .env / .env.example
│   │   ├── prisma/
│   │   │   ├── schema.prisma      ✅ 11 models (User, ApiToken, RefreshToken, Household, HouseholdMember, ShoppingList, ListItem, Purchase, Product, PriceEntry, SystemConfig) + 3 enums (PaymentMethod, ListType, ReceiptStatus)
│   │   │   └── migrations/
│       │   │       ├── 20260805024405_init
│       │   │       ├── 20260805210807_add_api_tokens
│       │   │       ├── 20260805231521_add_currency
│       │   │       ├── 20260806041017_add_list_category
│       │   │       ├── 20260808002858_add_external_api
│       │   │       ├── 20260808173208_phase5a_checkout_purchase_gs1_off
│       │   │       └── 20260810021352_add_product_barcodes
│   │   └── src/
│   │       ├── main.ts            json({ limit: '10mb' }), CORS, ServeStaticModule (/admin)
│   │       ├── app.module.ts       9 módulos
│   │       ├── prisma/             PrismaModule (@Global) + PrismaService
│   │       └── modules/
│   │           ├── auth/           login, refresh, logout, change-password, JWT
│   │           ├── users/          perfil, households, roles, regenerate-code
│   │           ├── lists/          CRUD listas + itens, histórico, checkout, Purchase CRUD, GS1 parser, OFF service
│   │           ├── setup/          Setup Wizard (one-time)
│   │           ├── health/         Healthcheck (DB + Redis + integrações)
│   │           ├── sync/           SyncGateway (Socket.io) + SyncService (Redis pub/sub)
│   │           ├── system/         SystemConfig CRUD (Grocy, HA, currency, AI fields)
│   │           ├── grocy/          Hybrid match, unit conversion, stock status, GS1 best_before
│   │           └── tokens/         API tokens CRUD (for external clients)
│       └── android/                     🚧 Em desenvolvimento (Fases 1–6 do plano)
│       ├── settings.gradle.kts
│       ├── build.gradle.kts
│       ├── gradle/libs.versions.toml
│       ├── app/
│       │   ├── build.gradle.kts
│       │   ├── src/main/
│       │   │   ├── AndroidManifest.xml    usesCleartextTraffic="true"
│       │   │   ├── java/com/listaih/app/
│       │   │   │   ├── ListaihApplication.kt
│       │   │   │   ├── MainActivity.kt       onKeyDown → BtScannerManager, CompositionLocalProvider
│       │   │   │   ├── MainViewModel.kt
│       │   │   │   ├── data/
│       │   │   │   │   ├── local/
│       │   │   │   │   │   ├── AppDatabase.kt   fallbackToDestructiveMigration
│       │   │   │   │   │   ├── Converters.kt
│       │   │   │   │   │   ├── entity/Entities.kt (ShoppingListWithCounts)
│       │   │   │   │   │   └── dao/Daos.kt
│       │   │   │   │   ├── network/
│       │   │   │   │   │   ├── ApiService.kt    + products (lookup/create/barcodes)
│       │   │   │   │   │   └── model/ApiModels.kt (ListItemResponse.addedAt: String?)
│       │   │   │   │   ├── preferences/AppPreferences.kt
│       │   │   │   │   ├── repository/
│       │   │   │   │   │   ├── ShoppingRepository.kt
│       │   │   │   │   │   └── ProductRepository.kt   (Fase 4)
│       │   │   │   │   └── scanner/                (Fase 5)
│       │   │   │   │       ├── BtScannerManager.kt (buffer HID, ENTER/NUMPAD_ENTER, idle 3s)
│       │   │   │   │       ├── HapticFeedback.kt   (sucesso/erro + beep)
│       │   │   │   │       └── LocalBtScanner.kt   (CompositionLocal)
│       │   │   │   ├── di/
│       │   │   │   │   ├── AppModule.kt
│       │   │   │   │   ├── NetworkModule.kt
│       │   │   │   │   └── AuthInterceptor.kt     (refresh+retry automático, @Body RefreshRequest)
│       │   │   │   ├── navigation/AppNavHost.kt      (mapeia ícone→category)
│       │   │   │   ├── sync/
│       │   │   │   │   ├── SyncWorker.kt
│       │   │   │   │   ├── BootReceiver.kt
│       │   │   │   │   └── SocketSyncService.kt
│       │   │   │   └── ui/
│       │   │   │       ├── theme/Theme.kt, Typography.kt
│       │   │   │       └── screens/
│       │   │   │           ├── onboarding/OnboardingScreen.kt
│       │   │   │           ├── login/LoginScreen.kt, LoginViewModel.kt
│       │   │   │           ├── home/HomeScreen.kt
│       │   │   │           ├── detail/ListDetailScreen.kt, ListDetailViewModel.kt
│       │   │   │           ├── addlist/AddListScreen.kt        (PONTUAL/RECORRENTE)
│       │   │   │           ├── additem/AddItemBottomSheet.kt
│       │   │   │           ├── shopping/ShoppingModeScreen.kt, ShoppingModeViewModel.kt, BarcodeScannerScreen.kt
│       │   │   │           ├── purchases/PurchasesScreen.kt, PurchasesViewModel.kt
│       │   │   │           └── settings/SettingsScreen.kt, SettingsViewModel.kt
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

### Models (12)
| Model | Descrição |
|---|---|
| `SystemConfig` | Config singleton (isSetup, adminUserId, grocyUrl, grocyApiKey, haUrl, haWebhookToken, currency, apiEnabled, apiBaseUrl, apiKey) |
| `User` | email, name, passwordHash, avatar, provider, googleId?, appleId? |
| `ApiToken` | name, token, tokenHash, prefix, type, userId, lastUsedAt, revokedAt |
| `RefreshToken` | token, userId, expiresAt |
| `Household` | name, inviteCode |
| `HouseholdMember` | householdId, userId, role (ADMIN/EDITOR/VIEWER) |
| `ShoppingList` | name, householdId, category?, template, listType?, archivedAt |
| `ListItem` | listId, name, quantity, unit, estimatedPrice, actualPrice, category, checked, position, barcode?, barcodeRaw?, productId?, offData? |
| `Product` | name, barcode?, category?, defaultUnit |
| `ProductBarcode` | barcode @unique, productId (barcode alternativo → Product) |
| `PriceEntry` | productId, storeName?, price, date, userId |
| `Purchase` | listId, storeName?, total, paymentMethod, status?, items (JSON), createdAt |

### Enums (5)
| Enum | Valores |
|---|---|
| `Unit` | unit, kg, g, L, ml |
| `HouseholdRole` | ADMIN, EDITOR, VIEWER |
| `PaymentMethod` | cash, pix, credit_card, debit_card, other |
| `ListType` | PONTUAL, RECORRENTE, MODELO |
| `ReceiptStatus` | pending, uploaded, missing |

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

### Products (`/api/products`) — Fase 3 (10 Ago)
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| GET | `/products/lookup/:barcode` | Buscar produto por barcode (findUnique → fallback ProductBarcode → OFF cria Product) | ✅ JWT |
| POST | `/products` | Criar produto (name, barcode?, category?, defaultUnit?) | ✅ JWT |
| POST | `/products/:productId/barcodes` | Associar barcode alternativo a um produto | ✅ JWT |

### Purchases (`/api/`) — Phase 5A
| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/lists/:id/checkout` | Finalizar compra (paymentMethod, grocySync, total itens checked) | ✅ JWT |
| GET | `/households/:householdId/purchases` | Histórico de compras da casa | ✅ JWT |
| GET | `/lists/:id/purchases` | Compras de uma lista | ✅ JWT |
| GET | `/purchases/:id` | Detalhe de uma compra | ✅ JWT |
| PATCH | `/purchases/:id` | Atualizar compra (storeName, total, paymentMethod) | ✅ JWT |
| GET | `/lists/:id/stock-status` | Status de estoque (Grocy) por item | ✅ JWT |

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
- **Device físico:** baseUrl default `http://127.0.0.1:3000` — testar com `adb reverse tcp:3000 tcp:3000`; manifest tem `usesCleartextTraffic="true"` (HTTP local)
- **Categoria de lista:** whitelist backend `['Alimentos','Farmacia','Papelaria','Material de Construcao','Geral']` — ícone do AddListScreen mapeado para categoria válida
- **Wear OS:** Module separado com Compose for Wear OS (ScalingLazyColumn, Vignette, CircularProgressIndicator)
- **Build:** AGP 8.4.2, Kotlin 1.9.22, Compose Compiler 1.5.11, minSdk 26/30; JDK 17 (`$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`); sempre `assembleDebug`
- **JWT:** access 15min, refresh 7d — `AuthInterceptor` faz refresh+retry automático (chama `/api/auth/refresh` com `RefreshRequest` no **body**, client próprio p/ evitar ciclo; pula login/refresh no interceptor)
- **API items:** `ListItemResponse.addedAt: String?` (backend **não** envia `createdAt`) — `toEntity` usa `addedAt ?: updatedAt`
- **Scanner HID:** `BtScannerManager` — comparar `keyCode` com **literais** (`== 66 || == 134`); constantes de framework em `||` geram sparse-switch com payload rejeitado pelo runtime; ENTER (66) é tragado pelo IME no Samsung → usar `134` (NUMPAD_ENTER); `rememberUpdatedState` p/ callbacks do scanner (DisposableEffect com closure stale não atualiza)
- **Simulação HID no device:** keyevents `7..16` = dígitos `0..9`, `134` = finish; enviar em comando único com idle 3s

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
 - **Purchases.tsx** ✅ Fase 5B — página de histórico de compras (sumário, tabela, detalhe com itens), checkout dialog integrado
 - **i18n** — todas as 8 páginas + Layout traduzidas, 3 idiomas (~310 keys cada), interpolation support no t()
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

### ✅ Concluído (Fases 1–2 do plano — validado no device 09 Ago)
- **Fase 1.1 — Login real**: `LoginScreen` chama `repository.login()` → JWT salvo → `getHouseholds()` → `householdId`. **Validado no device Samsung `RQ8T206PXPW`**: onboarding → login (admin@exemplo.com) → Home com dados reais.
- **Fase 1.2 — Home real**: DAO `getActiveListsWithCounts` (com `COALESCE`, não `??` — parser do Room rejeita `??`), `ShoppingListWithCounts` em Entities.kt, `getActiveListsUiFlow()`/`toShoppingListUi()` no repository, MainViewModel observa o Flow. Removidas as listas mock.
- **Fase 1.3 — Detail real**: novo `ListDetailViewModel.kt` (Hilt, SavedStateHandle `listId`/`listName`; observa `getListItems`, `syncListItems` no init, toggle/checkAll/uncheckAll/deleteChecked/addItem/updateItem/renameList). `ListDetailScreen` sem mocks. Abre sem crash no device.
- **Fase 1.4 — Barcode real**: `ListItemEntity` e `ListItemResponse` com `barcode`/`barcodeRaw`/`productId`; `ShoppingModeViewModel` usa `entity.barcode`.
- **Fase 2 — Criar lista com tipo**: `AddListScreen` com FilterChips PONTUAL/RECORRENTE (sem MODELO), `CreateListRequest.listType`, `ShoppingListEntity.listType` (coluna nova, coberta por `fallbackToDestructiveMigration`), `createList(householdId, name, category, listType)`, badge do tipo no card da Home. **Validado no device**: "Teste App" criada via UI → backend confirmou `type=RECORRENTE`, `category=Alimentos`.
- **Bugs corrigidos na validação no device**:
  - `android:usesCleartextTraffic="true"` no manifest (Android bloqueia HTTP local por padrão → "CLEARTEXT communication not permitted")
  - Ícone do AddListScreen era enviado como `category` → 400 (whitelist backend `['Alimentos','Farmacia','Papelaria','Material de Construcao','Geral']`); AppNavHost agora mapeia Compras→`Alimentos`, Farmácia→`Farmacia`
  - baseUrl default `http://10.0.2.2:3000` (emulador) → `http://127.0.0.1:3000` para device físico via `adb reverse tcp:3000 tcp:3000`

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
- **Settings conectado ao backend:** perfil real, casa, convite, alterar senha, export JSON real
- **Purchases (histórico):** cards de resumo, detalhe com edição, checkout integrado

### 📋 Plano Aprovado — Fase 4 Revisada
**Documento oficial:** `docs/ANDROID-PLAN.md` — aprovado em 09 Ago 2026.

**Decisões-chave:**
- Single-household (sem seleção de casa)
- Fluxo de compra livre; scanner = acelerador opcional (sem seleção de modo)
- Popup pós-scan sem timer; próximo scan confirma o anterior
- Associação de produto: 3 opções (Associar / Cadastrar / Genérico)
- Criar lista expõe PONTUAL e RECORRENTE (sem MODELO)
- Grocy: app só envia `grocySync: true` no checkout; backend faz match por barcode
- Open Food Facts: backend busca via `barcodeRaw`; app não chama OFF
- Wear OS: opcional, último; popup espelha no relógio se configurado

**Ordem de execução (ver ANDROID-PLAN.md seção 5):**
1. ✅ Fase 1 — Login real + dados reais (Home, Detail, ShoppingMode)
2. ✅ Fase 2 — Criar lista com tipo PONTUAL/RECORRENTE
3. ✅ Fase 3 — Backend: endpoints de Product (lookup, create, associate)
4. ✅ Fase 4 — Android: OFF + barcode nos models (CreateItemRequest, UpdateItemRequest, ProductRepository)
5. ✅ Fase 5 — Scanner Bluetooth HID + Câmera + Haptics
6. ✅ Fase 6 — Popup pós-scan + Associação (validado no device 10 Ago)
7. ✅ Fase 10 — Home real + Filtros (validado no device 11 Ago, commit f441ace)
8. ✅ Fase 7 — Settings leve (tema claro/escuro/sistema, URL do servidor com teste de conexão, toggle Wear OS) — 11 Ago (device)
9. ✅ Fase 8 — Onboarding expandido (Conectar ao servidor + Entrar ou criar conta via /api/setup/status) — 11 Ago (device)
10. ⬜ Fase 9 — Wear OS companion (opcional)

### ✅ Concluído (Fase 5 — Scanner HID + Haptics, validado no device 10 Ago)
- **Fase 5 — Scanner Bluetooth HID**: `BtScannerManager` (buffer de dígitos, ENTER/NUMPAD_ENTER finaliza, idle 3s, callback `onBarcodeScanned`), `HapticFeedback` (sucesso: 80ms + beep; erro: dupla + alarm tone), `LocalBtScanner` (CompositionLocal provido pela `MainActivity` — `onKeyDown` delega). Câmera (`BarcodeScannerScreen`) mantida como fallback.
- **Validado no device Samsung `RQ8T206PXPW`**: scan simulado via `adb input keyevent` (13 dígitos + `134`) → match por barcode → toggle + haptic + toast + PUT no backend (`checked: true`, `actualPrice` auto-calculado) → UI "Comprados: 1". Testes 2x.
- **Correções no caminho (validação no device)**:
  - `AuthInterceptor` com refresh automático: corrigido body vs header (`POST /api/auth/refresh` espera `{ refreshToken }` no body — o app mandava no header → 401 em loop pós-15min)
  - **Bug antigo de itens nunca sincronizando**: API envia `addedAt`, modelo esperava `createdAt` → NPE em `Instant.parse` no `toEntity` (por isso "Itens (0)" desde sempre). Corrigido: `ListItemResponse.addedAt: String?` + fallback
  - **Kotlin compiler + sparse-switch**: `||` com duas constantes de framework compila para sparse-switch com payload rejeitado pelo runtime (cai sempre no default). Corrigido com comparação literal `== 66 || == 134`
  - **Callback stale**: `DisposableEffect(btScanner)` registrava handler da 1ª composição (items vazios) → scan nunca dava match. Corrigido com `rememberUpdatedState` (ListDetailScreen e ShoppingModeScreen)
- **Simulação HID via adb**: keyevents 7–16 = dígitos 0–9; `66` (ENTER) é tragado pelo IME no Samsung — usar `134` (NUMPAD_ENTER); idle de 3s permite rajada única
- **Screen-off**: durante testes usar `settings put system screen_off_timeout 1800000` (revertido para 30000 ao final); keyguard padrão exige desbloqueio manual do usuário

### ✅ Concluído (Fase 6 — Popup pós-scan + Associação, validado no device 10 Ago)
- **Implementado**: `ScanPopupController` (sem timer; próximo scan confirma o anterior), popup reconhecido (nome, qtd, preço — não acende tela) com Confirmar, scan repetido do mesmo código = +1 na quantidade, popup não reconhecido (acende tela `FLAG_TURN_SCREEN_ON`) com 3 opções: **Associar à lista** / **Cadastrar novo** / **Genérico**, `ScanItemActions.kt` (ações do popup). Associação: chooser de itens → cria Product (`POST /api/products`) → `PATCH` item com `productId`.
- **Validado no device Samsung `RQ8T206PXPW` (10 Ago, ~02:00–02:35)**:
  1. Scan de código associado (7899990001111) → popup reconhecido "Manteiga Teste / 2 unit" → Confirmar → PATCH checked ✓
  2. Re-scan com popup aberto → +1 (2 → 3 unit, atualiza backend) ✓
  3. Scan desconhecido (123500123501) → popup "Código não reconhecido" ✓
  4. Associar à lista → chooser → "Manteiga Teste" → **PATCH 200** com productId, **sem crash** ✓
  5. Re-scan do código recém-associado → popup reconhecido ✓ (barcode vinculado)
- **Bugs corrigidos no caminho**:
  1. **Matcher stale no popup**: `findScanItem` lia `items` da composição (callback externo do scanner podia cruzar recomposições → lista "vazia"). Corrigido: ler `viewModel.uiState.value.items`.
  2. **Crash `kotlinx.serialization.SerializationException: Serializer for class 'Any'`**: `queueSync(entityType, id, op, payload: Any)` chamava `Json.encodeToString(payload)` — sem serializer de `Any` em runtime. Corrigido: assinatura `payload: String` (callers serializam tipado).
  3. **400 no PATCH de associação**: backend rodava com `UpdateItemDto` antigo (sem `productId`); dist recompilado mas processo não reiniciado. Corrigido: `node dist/main.js` reiniciado (PID 60172 → 142252).
  4. **`ShoppingRepository.kt` reconstruído**: `git checkout --` acidental reverteu para o HEAD (quebrado, 429 linhas); a versão da Fase 6 (658 linhas) não estava commitada. Reconstruído do zero: coroutines suspensas (sem `blockingFirst`/`.await()`), `RefreshRequest(refreshToken)` no body, `entity.copy()` no updateList, métodos recuperados (`getActiveListsUiFlow`, `getHouseholds`, `saveHouseholdId`, `getPurchase`, `getProfile`, `regenerateInviteCode`, `changePassword`, `exportLocalData`). `assembleDebug` OK.

### 🚧 Próximo passo imediato
**Fase 9 — Wear OS Companion** (ver ANDROID-PLAN.md seção 4): Data Layer API
Phone → Watch (lista ativa + progresso), WearScanPopupScreen (acende e destrava tela),
haptics no Watch, toggle já existente no Settings ("Usar Wear OS para detalhar scan").
Fases 1–8 concluídas e validadas no device (Home real, Settings leve, Onboarding expandido, 11 Ago).

### 🚧 Em andamento (anterior, agora reorganizado no plano)
- Integração completa Repository → UI (ViewModels)
- Testes de build e execução
- Socket.io real-time handlers
- Sincronização bidirecional completa

### Próximos passos imediatos (atualizados conforme plano)
1. ✅ Fase 1: Login real, MainViewModel, ListDetailViewModel, ShoppingMode barcode
2. ✅ Fase 2: AddListScreen com tipo (validado no device 09 Ago)
3. ✅ Fase 3: Backend endpoints de Product (lookup/OFF, create, barcode alternativo) — validado por API 10 Ago
4. ✅ Fase 4: Android OFF + barcode nos models (ProductRepository, CreateItem/UpdateItem)
5. ✅ Fase 5: Scanner Bluetooth HID + Haptics (validado no device 10 Ago)
6. ✅ Fase 6: Popup pós-scan + Associação (validado no device 10 Ago)
7. ✅ Fase 10: Home real + Filtros (validado no device 11 Ago)
8. ✅ Fase 7: Settings leve (validado no device 11 Ago)
9. ✅ Fase 8: Onboarding expandido (validado no device 11 Ago)

### Melhorias futuras (não bloqueantes)
- Code-splitting no admin (chunks > 500kb warning do Vite)
- `getListCategoryLabel()` poderia usar i18n em vez de hardcoded pt-BR
- Backup/exportação de dados (botões atualmente disabled)
- Zona de perigo (limpar arquivadas, resetar integrações, resetar sistema — disabled)

---

## Próximos Passos (Android)

> Ordem de execução oficial: `docs/ANDROID-PLAN.md` seção 5. Fases 1–8 do plano
> concluídas e validadas no device. A seguir, **Fase 9 — Wear OS Companion**.
>
> **Repo no GitHub:** `github.com/phgsbr/listaih` (privado, branch `master`).
> Último commit: `8e9e720` (Fase 7 — Settings leve + fix HealthResponse, 11 Ago).
> `.env` do backend fora do repo.

### Fase 6 — ✅ Concluída (10 Ago)
Popup pós-scan sem timer (reconhecido: Confirmar, +1 por scan repetido; não reconhecido:
Associar à lista / Cadastrar novo / Genérico) — validado no device (ver seção Fase 4 acima).
Bugs corrigidos no caminho: matcher stale (`uiState.value.items`), crash `queueSync`
(`payload: String`), 400 no PATCH de associação (backend reiniciado com `productId` no DTO),
`ShoppingRepository.kt` reconstruído após checkout acidental.

### Fases seguintes (após Fase 6)
- ✅ Fase 10 — Home real + Filtros (chips ligados às queries Room reais) — 11 Ago
- ✅ Fase 7 — Settings leve (tema, URL do servidor, toggle Wear OS, remover admin do mobile) — 11 Ago
- ✅ Fase 8 — Onboarding expandido (conectar ao servidor + setup status → wizard/login) — 11 Ago
- ⬜ Fase 9 — Wear OS companion (opcional)

### Validação no device (a cada fase)
- Build: `.\gradlew.bat :app:assembleDebug --console=plain` (JDK 17 do Android Studio)
- Instalar: `adb install -r apps/android/app/build/outputs/apk/debug/app-debug.apk` (preserva dados)
- `adb reverse tcp:3000 tcp:3000` (device físico)
- Checks: UI via `uiautomator dump` + regex; backend via logcat OkHttp/Retrofit; scan via keyevents `7..16` + `134`
- Restaurar `screen_off_timeout` (30000) ao final dos testes; keyguard exige desbloqueio manual

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
