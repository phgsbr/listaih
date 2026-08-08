# Listaih — Arquitetura do Sistema

> Última atualização: 04 Ago 2026

---

## Conceito

**Listaih** é um sistema self-hosted de listas de compras colaborativas. O backend é o produto central — um sistema instalado no homelab que gerencia listas, sincronização em tempo real, e integrações com Grocy e Home Assistant. Android, Wear OS e Alexa Skill são interfaces externas que consomem a API do backend.

---

## Arquitetura

```
Homelab (docker compose up)
├── Listaih Backend (NestJS)          ← sistema central
│   ├── API REST (/api)              ← clients externos (Android, Alexa)
│   ├── WebSocket (/ws)              ← sync em tempo real (Fase 2)
│   ├── Admin WebUI (/admin)        ← painel de administração (Fase 3)
│   ├── Setup Wizard (/api/setup)   ← configuração inicial one-time
│   ├── Health Check (/api/health)  ← status do sistema
│   ├── Grocy Integration            ← bidirecional (pull produtos + push listas)
│   └── Home Assistant Integration  ← bidirecional (webhooks + MQTT)
├── PostgreSQL 16                    ← banco de dados
├── Redis 7                          ← cache + pub/sub (WebSocket)
├── Caddy (reverse proxy)            ← HTTP :80 (TLS ao configurar domínio)
└── Cloudflare Tunnel (opcional)    ← HTTPS público para Alexa Skill
```

---

## Single-Household

Uma instalação do Listaih = uma casa. Não há registro público.

1. **Primeiro boot:** sistema retorna `{ isSetup: false }` em `GET /api/setup/status`
2. **Setup Wizard:** `POST /api/setup` cria o usuário admin + household único
3. **Após setup:** registro desabilitado. Novos membros entram via invite code
4. **Configurações de integração** (Grocy, HA) ficam em `SystemConfig` (singleton)

---

## Modelo de Dados

| Model | Descrição |
|---|---|
| `SystemConfig` | Config singleton do sistema (isSetup, admin, integrações) |
| `User` | Usuário com email, nome, senha hash, OAuth |
| `RefreshToken` | Tokens de refresh com expiração |
| `Household` | Grupo de compartilhamento (single-household = 1 registro) |
| `HouseholdMember` | N:N User↔Household com role (ADMIN/EDITOR/VIEWER) |
| `ShoppingList` | Lista de compras com nome, template, archivedAt |
| `ListItem` | Item da lista com qty, unit, preço, categoria, check-off |
| `Product` | Catálogo de produtos com barcode |
| `PriceEntry` | Histórico de preços por loja |

---

## APIs

### Públicos (sem auth)
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/setup/status` | Verificar se o sistema foi configurado |
| POST | `/api/setup` | Configuração inicial (one-time) |
| GET | `/api/health` | Status do sistema (DB, Redis, integrações) |
| POST | `/api/auth/login` | Login com email/senha |
| POST | `/api/auth/refresh` | Renovar access token |

### Autenticados (JWT)
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/logout` | Invalidar refresh token |
| POST | `/api/auth/change-password` | Trocar senha |
| GET | `/api/users/me` | Perfil do usuário |
| PUT | `/api/users/me` | Atualizar perfil |
| GET | `/api/users/households` | Listar households |
| POST | `/api/users/households/join` | Entrar em household via invite code |
| PATCH | `/api/users/households/:id/members/:memberId` | Alterar role de membro |
| GET | `/api/households/:id/lists` | Listar listas ativas |
| POST | `/api/households/:id/lists` | Criar lista |
| GET | `/api/lists/:id` | Detalhe da lista com itens |
| PUT | `/api/lists/:id` | Atualizar lista |
| DELETE | `/api/lists/:id` | Excluir lista |
| POST | `/api/lists/:id/items` | Adicionar item |
| PATCH | `/api/lists/:id/items/:itemId` | Atualizar item |
| DELETE | `/api/lists/:id/items/:itemId` | Remover item |
| GET | `/api/households/:id/history` | Listas arquivadas |

---

## Deploy

### Requisitos
- Docker + Docker Compose
- Porta 80 disponível (Caddy)

### Passos
```bash
# 1. Clonar o repositório
git clone <repo> && cd listaih

# 2. Configurar variáveis
cp .env.example .env
# Editar .env — trocar JWT_SECRET e POSTGRES_PASSWORD

# 3. Subir o sistema
docker compose up -d

# 4. Verificar status
curl http://localhost/api/health

# 5. Configuração inicial (setup wizard)
curl -X POST http://localhost/api/setup \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@exemplo.com","name":"Admin","password":"senha123","householdName":"Minha Casa"}'

# 6. Login
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@exemplo.com","password":"senha123"}'
```

---

## Integrações (Bidirecional)

### Grocy
- **Listaih → Grocy:** Busca produtos/chores para autocomplete na criação de itens
- **Grocy → Listaih:** Ao concluir lista, Listaih envia itens ao Grocy (shopping list ou stock)
- **Config:** `SystemConfig.grocyUrl` + `SystemConfig.grocyApiKey` + `grocyEnabled`
- **Status:** Futuro (Fase 5)

### Home Assistant
- **Listaih → HA:** Notificações via webhook REST (item adicionado, lista concluída)
- **HA → Listaih:** Webhook que HA chama para adicionar item (automação de voz ou botão)
- **MQTT Discovery:** Listaih se anuncia como entidade no HA (sensor de itens pendentes)
- **Config:** `SystemConfig.haUrl` + `SystemConfig.haWebhookToken` + `haEnabled`
- **Status:** Futuro (Fase 6)

---

## Clients Externos

### Android App (Fase 4)
- Kotlin + Jetpack Compose + Hilt + Room (offline-first)
- Consome API REST + WebSocket do backend
- Wear OS via Compose for Wear OS

### Alexa Skill (Fase 7)
- Account linking via OAuth2 ao backend Listaih
- Intents: adicionar item, listar itens, marcar item
- Requer HTTPS público (Cloudflare Tunnel)

### Admin WebUI (Fase 3)
- React + Vite + TypeScript
- Servido pelo backend em `/admin`
- Gerenciar integrações, membros e configurações do sistema
