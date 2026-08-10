# AGENTS.md — Listaih

## Projeto
Listaih — Sistema self-hosted de listas de compras colaborativas para homelabs.
Backend é o produto central (NestJS). Admin WebUI em React. Android, Wear OS, Alexa e HA são interfaces externas.

## Status
- **Fase 1 (Core Backend):** ✅ Concluído
- **Fase 2 (WebSocket Sync):** ✅ Concluído
- **Fase 3 (Admin WebUI + i18n + External API):** ✅ Concluído
- **Fase 4 (Android):** 🚧 Em desenvolvimento
- **Fase 5 (Grocy):** ⬜ Pendente
- **Fase 6 (Home Assistant):** ⬜ Pendente
- **Fase 7 (Alexa):** ⬜ Pendente

## Stack
- **Backend:** NestJS 10 + Prisma 5 + PostgreSQL 16 + Redis 7 + JWT + Socket.io
- **Admin:** React 19 + Vite 8 + TypeScript + MUI v9 (`@mui/material@9`)
- **Android:** Kotlin + Jetpack Compose + Material 3 + Room + DataStore + Retrofit + Socket.io + WorkManager
- **Wear OS:** Compose for Wear OS + Material 3 for Wear
- **Deploy:** Docker Compose (backend + postgres + redis + caddy)

## Estrutura
```
apps/backend/    — NestJS (11 módulos: auth, users, lists, setup, health, sync, system, tokens, prisma, external-api, grocy)
apps/admin/      — React + Vite (7 páginas: Login, Dashboard, Lists, Members, Clients, Integrations, Settings)
apps/android/    — Kotlin + Compose (Phone + Wear OS modules)
packages/shared/ — (futuro)
docs/            — architecture.md, progress.md
```

## Comandos
```bash
# Backend — build e run
cd apps/backend && npx nest build && node dist/main.js

# Admin — build (gera dist/ que o backend serve em /admin/)
cd apps/admin && npm run build

# Android — build
cd apps/android && ./gradlew assembleDebug

# Wear OS — build
cd apps/android && ./gradlew :wear:assembleDebug

# Prisma
cd apps/backend && npx prisma generate && npx prisma migrate deploy
```

## Convenções Críticas

### Backend
- `tsconfig.json`: `strictPropertyInitialization: false`, `strictNullChecks: false`
- **NÃO usa** aliases `@` nos paths
- `PrismaModule` é `@Global()` — não precisa importar nos módulos
- `main.ts`: `json({ limit: '10mb' })` para avatar data URLs
- ServeStaticModule: `serveRoot: '/admin'`, `rootPath: join(__dirname, '..', '..', 'admin', 'dist')`
- `updateItem` auto-calcula `actualPrice = estimatedPrice * quantity` quando `checked: true`

### Admin Frontend
- Vite `base: '/admin/'` — todos os assets têm prefixo
- Alias `@` → `./src`
- MUI v9: `TextField` usa `slotProps.input` (não `InputProps`); `slotProps.htmlInput` para `readOnly`
- **NO `tertiary` in PaletteOptions** — usar primary, secondary, error, warning, info, success
- Imagens: paths relativos (`./logo.svg`, `./icons/grocy.png`)
- Variável do i18n `t`: evitar shadowing em `.map()` (usar `tk`)
- `tsconfig.app.json`: `ignoreDeprecations: "6.0"`

### i18n
- `apps/admin/src/hooks/useI18n.tsx` — provider, hook, 3 dicionários (pt-BR, en-US, es-ES)
- `useI18n()` → `{ lang, setLang, t }`
- `t(key)` busca no dicionário atual, fallback pt-BR, fallback key
- Idioma salvo em `localStorage` key `listaih_language`
- Default: pt-BR

### Android
- `apps/android/app` — Phone app module
- `apps/android/wear` — Wear OS module
- Material 3 theming (`androidx.compose.material3`)
- Room database (`androidx.room`) com 7 entities + sync queue; `fallbackToDestructiveMigration()` (schema novo não pede migration manual em dev)
- DataStore Preferences (`androidx.datastore`) para auth tokens e settings
- Retrofit + Kotlinx Serialization para API REST
- Socket.io client (`io.socket:socket.io-client`) para real-time sync
- WorkManager (`androidx.work`) para periodic sync (15 min)
- Hilt (`com.google.dagger:hilt`) para dependency injection
- Navigation Compose (`androidx.navigation:navigation-compose`)
- Coil (`io.coil-kt:coil-compose`) para image loading
- Accompanist para permissions e system UI controller
- `minSdk 26`, `targetSdk 34`, `compileSdk 34`
- Kotlin 1.9.22, AGP 8.4.2, Compose Compiler 1.5.11
- Build: JDK 17 (`$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`); sempre `.\gradlew.bat :app:assembleDebug --console=plain`; APK em `apps/android/app/build/outputs/apk/debug/app-debug.apk`; device `RQ8T206PXPW` (adb em `C:\Users\Pedro Henrique\AppData\Local\Android\Sdk\platform-tools\adb.exe`); `adb install -r` preserva dados
- **baseUrl default `http://127.0.0.1:3000`** — device físico usa `adb reverse tcp:3000 tcp:3000`; manifest tem `usesCleartextTraffic="true"`
- **JWT access 15min / refresh 7d**: `AuthInterceptor` faz refresh+retry automático — `/api/auth/refresh` recebe `RefreshRequest` no **body** (header quebra); pula login/refresh; client próprio evita ciclo
- **API de itens envia `addedAt` (e não `createdAt`)** — `ListItemResponse.addedAt: String?`; `toEntity` usa `addedAt ?: updatedAt`
- **Scanner HID**: `data/scanner/BtScannerManager.kt` — comparar `keyCode` com literais `66`/`134` (constantes de framework em `||` compilam p/ sparse-switch com payload rejeitado pelo runtime); `ENTER`(66) é tragado pelo IME no Samsung, usar `NUMPAD_ENTER`(134); callbacks do scanner via `rememberUpdatedState` (closure stale não atualiza); simular scan: keyevents `7..16` (dígitos 0–9) + `134` num comando único (idle 3s)
- UI check no device: `uiautomator dump` + regex (screenshot PNG corrompe via `adb exec-out` no PowerShell)

### Wear OS
- Compose for Wear OS (`androidx.wear.compose:compose-material`)
- ScalingLazyColumn para listas otimizadas
- CircularProgressIndicator para progress rings
- Chip components para itens de lista
- Vignette para edge fading
- `minSdk 30` (Wear OS 3+)

## Credenciais de teste (local)
- PostgreSQL: `listaih` / `listaih` / `listaih` / `localhost:5432`
- Admin: `admin@exemplo.com` / `senha123`
- URL: `http://localhost:3000/admin/`

## Limitações do ambiente
- Docker não instalado — `docker compose up` não testável
- Redis não instalado — SyncService emite WARN, WebSocket sync não testável
- PowerShell pode corromper UTF-8 com acentos em testes de API

## Migrations Prisma
1. `20260805024405_init` — schema inicial
2. `20260805210807_add_api_tokens` — ApiToken model
3. `20260805231521_add_currency` — currency no SystemConfig
4. `20260806041017_add_list_category` — category no ShoppingList
5. `20260808002858_add_external_api` — apiEnabled, apiBaseUrl, apiKey no SystemConfig
6. `20260808173208_phase5a_checkout_purchase_gs1_off` — Purchase model, checkout, GS1 parser, OFF no ListItem
7. `20260810021352_add_product_barcodes` — ProductBarcode model (barcode alternativo → Product)

## External API (Agentes IA / Clientes Externos)
**Módulo:** `apps/backend/src/modules/external-api/`

### Autenticação
- Header: `x-api-key: <sua-chave>`
- Configurado em: Admin → Integrações → API Externa → "Integração ativa" + "API Key"
- Strategy: `ApiKeyStrategy` (passport-headerapikey)
- Guard: `ApiKeyGuard`

### Endpoints (`/api/external/`)
| Método | Rota | Descrição | Rate Limit |
|--------|------|-----------|------------|
| GET | `/health` | Health check | **Sem limite** (`@SkipThrottle`) |
| GET | `/config` | Retorna `apiBaseUrl` | **Sem limite** |
| GET | `/households/:householdId/lists` | Listas da casa (query `archived=true/false`) | 60 req/min |
| GET | `/lists/:listId` | Lista única com itens | 60 req/min |
| GET | `/lists/:listId/items` | Itens de uma lista | 60 req/min |

### Rate Limiting
- Global: 100 req/min/IP (ThrottlerModule no AppModule)
- External API: 60 req/min/IP (override no controller)
- Health/config: isentos (`@SkipThrottle`)

### Audit Log
- Interceptor: `ExternalApiAuditInterceptor`
- Loga: método, URL, IP, API Key (mascarada), User-Agent, duração, status HTTP
- Output no console: `[ExternalAPI] GET /api/external/health | IP: ::1 | Key: abc... | UA: ... | 45ms | 200 OK`

### Configuração via Admin
1. Acesse `http://localhost:3000/admin/` → Integrações
2. Card **"API Externa"** (ícone azul)
3. Preencha: URL Base da API (opcional), API Key
4. Ative: "Integração ativa"
5. Salve

### Configuração via API (admin)
```bash
# Login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@exemplo.com","password":"senha123"}'

# Habilitar API Externa
curl -X PUT http://localhost:3000/api/system/config \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"apiEnabled":true,"apiKey":"sua-chave-secreta"}'
```

### Uso por agentes (Hermes, Openclaw, etc.)
```bash
# Health check
curl http://localhost:3000/api/external/health \
  -H "x-api-key: sua-chave-secreta"

# Listar listas ativas
curl "http://localhost:3000/api/external/households/<householdId>/lists" \
  -H "x-api-key: sua-chave-secreta"

# Itens de uma lista
curl http://localhost:3000/api/external/lists/<listId>/items \
  -H "x-api-key: sua-chave-secreta"
```

### i18n (Admin → Integrações)
- `integ.externalApi` / `integ.externalApi.desc`
- `integ.apiBaseUrl` / `integ.apiBaseUrlPh`
- `integ.apiKey`
- Disponível em: pt-BR, en-US, es-ES

## Grocy Integration (Fase 5)
**Módulo:** `apps/backend/src/modules/grocy/`

### Configuração (Admin → Integrações)
1. Acesse `http://localhost:3000/admin/` → Integrações
2. Card **"Grocy"** (ícone laranja)
3. Preencha: URL do Grocy (ex: `http://grocy:9283`), API Key
4. Ative: "Integração ativa"
5. Salve

### Endpoints (`/api/grocy/`) — Requer JWT
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/test-connection` | Testar conexão com Grocy |
| POST | `/send-to-stock/:listId` | Enviar itens comprados → despensa |
| POST | `/sync-list/:listId` | Sincronizar lista → Grocy shopping list |
| POST | `/sync-from-grocy` | Importar produtos do Grocy |

### Fluxos
1. **Enviar para despesa** — Botão "Enviar para o Grocy" na lista: pega itens `checked: true`, cria produto no Grocy se não existir, adiciona ao stock (despensa)
2. **Sincronizar lista** — Envia todos os itens da lista para a shopping list do Grocy
3. **Importar do Grocy** — Busca produtos do Grocy e cria no Listaih (para autocomplete)

### GrocyApiClient
- `apps/backend/src/modules/grocy/grocy-api.client.ts`
- Wrapper tipado para REST API do Grocy (`/api/objects/...`, `/api/stock/...`)
- Headers: `GROCY-API-KEY`
- Auto-carrega config do `SystemConfig` no `onModuleInit`

### GrocySyncService
- `apps/backend/src/modules/grocy/grocy-sync.service.ts`
- Lógica de match por nome (case-insensitive)
- Cria produtos automaticamente se não existirem
- Mapeia unidades (kg, g, L, ml, unit)

### i18n (Admin → Integrações)
- `integ.grocy` / `integ.grocy.desc`
- `integ.grocyUrl` / `integ.grocyUrlPh`
- `integ.grocyApiKey`
- Disponível em: pt-BR, en-US, es-ES

## Docs
- `docs/progress.md` — estado detalhado do projeto (atualizado em 07 Ago 2026)
- `docs/architecture.md` — arquitetura self-hosted
