# RELEASE-PLAN — Revisão final e liberação v0.1 (phone + wear + backend)

> Documento vivo: todo progresso, passo e decisão é registrado aqui.
> Status geral: **EM EXECUÇÃO (Fase 1)**
> Criado: 11 Ago 2026

## Objetivo
Liberar a v0.1 utilizável do Listaih: backend no container (validado local e pronto para o ZimaOS), apps phone/wear revisados sob lentes de UI/UX/acessibilidade, testes práticos do usuário (2 celulares + relógio) e ajustes finais.

## Decisões do usuário (11 Ago 2026)
1. **Docker**: instalado localmente no Windows para validação; deploy final no **ZimaOS** (home lab)
2. **Admin WebUI**: build multi-stage dentro do container (autocontido)
3. **Revisão UX**: relatório completo em `docs/UX-REVIEW.md`; agente aplica apenas correções seguras; mudanças de design maiores → propostas aprovadas pelo usuário
4. **i18n phone**: corrigir mojibake, manter pt-BR, desativar seletor de idioma sem efeito
5. Instalar Docker Desktop (WSL2) — autorizado explicitamente pelo usuário

---

## Fase 1 — Container backend (0.1)

### 1.1 Preparação
- [x] Autorização do usuário para instalar Docker
- [x] `docs/RELEASE-PLAN.md` criado
- [x] Docker Desktop instalado (winget, 4.86.0) e CLI OK (`docker version` 29.7.2)
- [x] WSL2 instalado (2.7.11 + VirtualMachinePlatform) — reboot concluído
- [x] Daemon de pé (`docker info`/`docker ps`)

### 1.2 Arquivos de deploy
- [x] `.env` raiz criado (POSTGRES_PASSWORD, JWT_SECRET, BACKEND_PORT) — **fora do git**
- [x] `.env.example` raiz (já existia — revisado)
- [x] `Dockerfile` multi-stage: build admin (tsc+vite) + backend; production com CLI `prisma` + `prisma migrate deploy` no start; admin dist em `/app/admin/dist`; `apk add openssl libc6-compat` (corrige schema-engine do Prisma no Alpine)
- [x] `docker-compose.yml` com context raiz, healthcheck do backend, volumes nomeados
- [x] `.dockerignore` raiz
- [x] `Caddyfile` revisado (LAN :80 → backend:3000)
- [x] `deploy/README-ZIMAOS.md` — passo a passo ZimaOS
- [x] `app.module.ts`: `ADMIN_DIST_PATH` env (rootPath do admin configurável — corrige 404 no container)

### 1.3 Validação local (Windows)
- [x] `docker compose up -d --build` conclui sem erros
- [x] `curl http://localhost:3000/api/health` → `{"status":"ok",database up, redis up}`
- [x] `/api/external/health` → 401 sem `x-api-key` (esperado)
- [x] WebUI em `http://localhost:3000/admin/` → 200 (title "Listaih - Admin")
- [x] Setup via API (`POST /api/setup`) + login OK
- [x] Migrations aplicadas (6, via `prisma migrate deploy` no start)

### 1.4 Deploy no ZimaOS (usuário)
- [x] Repositório tornado **público** (`github.com/phgsbr/listaih`) — clone sem token, GHCR sem login
- [x] Deploy simplificado: `git clone` + `docker compose up -d --build` (sem GHCR, sem tar)
- [x] README-ZIMAOS reescrito (fluxo único, GHCR como alternativa opcional)
- [x] `.gitignore` + `*.tar` (arquivo de imagem não vai para o repo)
- [ ] Stack de pé no ZimaOS (`docker compose up -d --build` após clone)
- [ ] Validação pela LAN: `/api/health`, `/admin/` em `http://<zima>:3000`
- [ ] Phone: Settings → Servidor → `http://<zima>:3000` + teste de conexão OK

### 1.5 Docs
- [ ] AGENTS.md: remover "Docker não instalado", adicionar seção Deploy (local + ZimaOS)
- [ ] `docs/progress.md` atualizado

---

## Fase 2 — Revisão UX/UI (phone + wear)

### Escopo
- **Phone (11 telas):** Onboarding, Setup, Login, Home, Detail, Shopping, Scanner câmera, Purchases, Settings, AddList, AddItemSheet + dialogs + popup pós-scan
- **Wear (6 telas):** Home, Select, Shopping (+editor), Checkout, Complete, Voice + popups/keypad do scanner

### Checklist de auditoria (10 áreas)
1. Material 3 / Wear Material: shapes, elevation, tipografia, componentes corretos
2. Touch targets: 48dp (phone) / mínimo wearable; espaçamentos
3. Contraste AA de cores e cor de status (checked/unchecked, erro)
4. Estados: loading / erro / empty em TODAS as telas (wear: implementar)
5. Acessibilidade: contentDescription, semantics, role, foco, TalkBack
6. i18n: mojibake, hardcoded, seletor sem efeito (desativar)
7. Navegação: back/gestos, deep links, retorno de fluxos
8. Ergonomia: 1 mão, ordem de foco, alvos alcançáveis
9. Fricção: confirmações desnecessárias, passos para ações frequentes
10. Feedback: haptics, toasts vs snackbars, estado de botões

### Passos
- [ ] Auditoria paralela (subagente phone + subagente wear) → achados com `arquivo:linha` e severidade (Crítico/Médio/Menor/Sugestão)
- [ ] `docs/UX-REVIEW.md` consolidado (por tela, priorizado)
- [ ] Correções **seguras** aplicadas (acessibilidade, contraste, empty states, mojibake, touch targets) — cada uma: build → install → verificação
- [ ] Propostas de design maiores → lista de aprovação do usuário
- [ ] Builds `:app:assembleDebug` e `:wear:assembleDebug` OK

---

## Fase 3 — Testes práticos do usuário (2 celulares + relógio)

- [ ] `docs/TEST-SCRIPT.md` preparado (roteiro por device/tela/cenário)
- [ ] Usuário executa o roteiro e reporta achados (tela / o que fez / esperado / ocorreu)
- [ ] Agente classifica por severidade, corrige e revalida

---

## Fase 4 — Ajustes finais e liberação

- [ ] Críticos/médios dos testes corrigidos e validados
- [ ] `progress.md`, `HANDOFF-android.md`, AGENTS.md finalizados
- [ ] Tag `v0.1` no git (repo público — tag sem release de assets)
- [ ] RELEASE-PLAN.md 100% concluído

---

## Riscos e planos B
- **Docker Desktop/WSL2 falha** (virtualização off) → Docker Engine no WSL2 puro
- **UI de Stacks do ZimaOS não suporta `build:`** → usar `image:` no compose (GHCR; visibilidade do package via UI do GitHub se preciso)
- **Wear 100% mock** → revisão cobre o existente; dados reais ficam para a integração (fora da v0.1)
- **PowerShell/UTF-8** → validar saídas com cuidado em respostas JSON