# Plano de Ação — App Android Listaih

> Aprovado em 09 Ago 2026. Este documento é a fonte de verdade para o desenvolvimento
> da Fase 4 (Android). Qualquer dúvida durante a implementação, volte aqui.

---

## 1. Objetivo

Transformar o app Android em uma ferramenta útil para um comprador no supermercado:
ver o que falta, saber quanto está gastando, escanear produtos, e finalizar a compra.
O app é uma ajuda — não uma obrigação. O fluxo é livre.

### Princípios

1. **O fluxo de compra é livre.** Usuário olha a lista, pega o produto, marca (ou não),
   preenche preço (ou não), finaliza. Nenhuma etapa é obrigatória.
2. **Scanner é um acelerador opcional.** Se o scanner Bluetooth estiver conectado, ele
   acelera compras longas. Se não estiver, o fluxo manual continua idêntico.
3. **Não há seleção de "modo de compra".** Não existe tela "Modo A vs Modo B". O scanner
   é uma camada de automação por cima do fluxo existente.
4. **Admin fica no Web.** Android não gerencia membros, tokens, integrações, tipos de
   lista MODELO, nem configura Grocy/HA. Android cria listas (PONTUAL/RECORRENTE),
   executa compras, e consulta histórico.
5. **Single-household.** Uma instalação = uma casa. Sem tela de seleção de casa.
6. **Offline-first.** Room é a verdade durante a compra. Sync depois quando houver rede.

---

## 2. Decisões Tomadas com o Usuário

| Decisão | Escolha | Contexto |
|---|---|---|
| Seleção de casa (household) | Não. Assume a única casa. | Single-household por design |
| Ordem das categorias | Alfabética. Não é drag-reorder. | Irrelevante para o usuário |
| Scanner no bolso | Scanner 2D Bluetooth externo (HID). Câmera como fallback. | Combinado desde o início |
| Orçamento mensal | Não implementar. | Nunca foi pedido |
| Busca de preço no histórico | Não implementar. | Nunca foi pedido |
| Criar lista no Android | Sim. Expõe PONTUAL e RECORRENTE (sem MODELO). | |
| Onboarding | Manter e expandir. Conecta ao servidor, cria usuário, login Google no futuro. | |
| Telas de montar vs comprar | Separadas. ListDetail = montar em casa. ShoppingMode = comprar no mercado. | |
| Timer do popup pós-scan | Sem timer. Só o próximo scan confirma o anterior. | |
| Timer do popup no Wear OS | Sem timer. Respeita o tempo de apagamento de tela do Wear OS. | |
| Pop-up pós-scan | Sempre abre. Não acende tela nem destrava (reconhecido). Acende (não reconhecido). | |
| Scans repetidos do mesmo código | Uma linha, quantidade incrementa (+1 por scan). | |
| Associação de produto | 3 opções: Associar à lista, Cadastrar novo, Genérico. | "Manteiga Pikachu = manteiga Sonic" |

---

## 3. Arquitetura do App

### Estrutura atual (existente, não descartar)

```
apps/android/
├── app/                          — Phone app
│   ├── data/
│   │   ├── local/                — Room (7 entities + DAOs + SyncQueue)
│   │   ├── network/              — Retrofit ApiService (52 endpoints) + ApiModels
│   │   ├── preferences/          — DataStore (auth, settings, baseURL)
│   │   └── repository/           — ShoppingRepository (offline-first pattern)
│   ├── di/                       — Hilt modules (App, Network, AuthInterceptor)
│   ├── navigation/               — AppNavHost (Navigation Compose)
│   ├── sync/                     — SyncWorker, BootReceiver, SocketSyncService
│   └── ui/
│       ├── theme/                — Material 3 (primary #006B3C)
│       └── screens/
│           ├── onboarding/       — 4 passos com ilustrações
│           ├── login/           — Email/senha (TODO: login real)
│           ├── home/            — Listas com filtros, busca, FAB
│           ├── detail/          — Itens agrupados por categoria (TODO: dados reais)
│           ├── additem/         — Bottom sheet
│           ├── addlist/        — Criar lista (TODO: tipo de lista)
│           ├── shopping/       — ShoppingMode + BarcodeScanner
│           ├── purchases/      — Histórico + detalhe + edição
│           └── settings/       — Perfil, casa, senha, export
└── wear/                         — Wear OS module
    └── ui/screens/             — Home, Shopping, Complete, Select, Voice
```

### Stack confirmada (AGENTS.md)

- Kotlin 1.9.22, AGP 8.4.2, Compose Compiler 1.5.11
- Material 3, Room, DataStore, Retrofit, Socket.io, WorkManager, Hilt
- Navigation Compose, Coil, Accompanist
- minSdk 26 (Phone), minSdk 30 (Wear OS)
- Build: `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; $env:Path="$env:JAVA_HOME\bin;$env:Path"`
- Compile: `.\gradlew.bat :app:compileDebugKotlin --console=plain`
- APK: `.\gradlew.bat :app:assembleDebug --console=plain`

### Cadeia de dados para Grocy

```
App Android escaneia "7891000100103"
  → GET /api/products/lookup/7891000100103
    → Backend: Product não existe → busca OFF → OFF acha "Leite Integral"
      → Cria Product { barcode, name, category }
    → Retorna Product para o app
  → App cria/atualiza ListItem com productId + barcode

No checkout (grocySync: true):
  → POST /api/lists/{id}/checkout { grocySync: true }
    → Backend: lista tem grocyAssociated + config.grocyEnabled
      → GrocySyncService.sendPurchasedItemsToStock()
        → Match por barcode (exato) → Product.barcode → GrocyProduct.barcode
        → Se não acha: match por nome → fuzzy ≥90% → cria no Grocy
        → addStockEntry: product_id, amount, best_before_date (GS1 se houver), price
        → Item na despensa do Grocy ✓

Comportamento pós-checkout por tipo de lista:
  → PONTUAL: arquiva a lista
  → RECORRENTE: desmarca todos os itens, reset para próximo ciclo
  → MODELO: bloqueia checkout (não exposto no app)
```

---

## 4. Fases de Implementação

### FASE 1 — ✅ CONCLUÍDA (09 Ago) — Destravar Dados Reais (pré-requisito de tudo)

| # | Ação | Arquivos |
|---|---|---|
| 1.1 | **Login real** — `LoginScreen` chama `repository.login(email, senha)` → salva JWT → busca única household em `getHouseholds()` → salva `householdId` | `LoginScreen.kt`, `MainViewModel.kt`, `ShoppingRepository.kt:59` |
| 1.2 | **Home com listas reais** — `MainViewModel` chama `syncLists(householdId)` e observa `shoppingListDao.getActiveLists()` como Flow. Remove 5 listas mock. | `MainViewModel.kt:25-31`, `HomeScreen.kt` |
| 1.3 | **Detalhe com itens reais** — Novo `ListDetailViewModel` chama `syncListItems(listId)` e observa `listItemDao.getItemsByListId()`. Remove 7 itens mock. | `ListDetailScreen.kt:88-96`, novo `ListDetailViewModel.kt` |
| 1.4 | **ShoppingMode carrega barcode real** — `ShoppingModeViewModel.loadItems()` usa `entity.barcode` em vez de `barcode = null`. | `ShoppingModeViewModel.kt:90` |

### FASE 2 — ✅ CONCLUÍDA (09 Ago) — Criar Lista com Tipo

> **Validação no device** (Samsung `RQ8T206PXPW`, 09 Ago): login real, Home com listas
> reais + badges de tipo, criação de lista via UI confirmada no backend
> (`type=RECORRENTE`, `category=Alimentos`). Bugs corrigidos no caminho:
> `usesCleartextTraffic="true"` (HTTP local), ícone mapeado para categoria válida
> (whitelist backend — `icon` não é enviado como `category`), baseUrl default
> `http://127.0.0.1:3000` para device físico via `adb reverse tcp:3000 tcp:3000`.

| # | Ação | Arquivos |
|---|---|---|
| 2.1 | `AddListScreen` expõe seletor `PONTUAL` / `RECORRENTE` (sem MODELO) | `AddListScreen.kt` |
| 2.2 | `CreateListRequest` adiciona `listType: String` | `ApiModels.kt` |
| 2.3 | `ShoppingRepository.createList()` passa `listType` | `ShoppingRepository.kt:134` |
| 2.4 | Card na Home mostra badge do tipo | `HomeScreen.kt` |

### FASE 3 — ✅ CONCLUÍDA (10 Ago) — Backend: Endpoints de Product

> **Implementado e validado por API (10 Ago):** `GET /api/products/lookup/:barcode`
> (busca local → barcodes alternativos → fallback OFF cria Product), `POST /api/products`,
> `POST /api/products/:productId/barcodes`. Requer JWT. Novos: `CreateProductDto`,
> `AddBarcodeDto`, model `ProductBarcode` (migration `20260810021352_add_product_barcodes`).
> Testes: lookup OFF criou Nutella; create manual; barcode alternativo resolvido no
> lookup seguinte; barcode desconhecido também acha dados no OFF (comportamento do OFF).
> Nota: endpoints estão no módulo `lists` (rotas `/api/products/...`).

| # | Ação | Arquivos |
|---|---|---|
| 3.1 | `GET /api/products/lookup/:barcode` — busca Product por barcode; se não acha, busca OFF; se OFF acha, cria Product e retorna | Novo endpoint em `lists.controller.ts` + `lists.service.ts` |
| 3.2 | `POST /api/products` — cria Product com `barcode`, `name`, `category`, `defaultUnit` | Novo endpoint |
| 3.3 | `POST /api/products/:productId/barcodes` — adiciona barcode alternativo a um Product existente (caso "manteiga Pikachu = manteiga Sonic") | Novo endpoint |

> Módulo `lists` já tem `OffService`, `GS1Parser`, `GrocySyncService`. Product é a ponte
> entre o scan do app e o match no Grocy no checkout.

### FASE 4 — ✅ CONCLUÍDA (10 Ago) — Android: Open Food Facts + Barcode nos Models

| # | Ação | Arquivos |
|---|---|---|
| 4.1 | `CreateItemRequest` adiciona `barcode`, `barcodeRaw`, `productId` | `ApiModels.kt:87` |
| 4.2 | `UpdateItemRequest` adiciona `barcode`, `barcodeRaw`, `productId` | `ApiModels.kt:96` |
| 4.3 | `ShoppingRepository.createItem()` passa `barcodeRaw` + `productId` | `ShoppingRepository.kt:207` |
| 4.4 | `ShoppingRepository.updateItem()` passa `barcodeRaw` + `productId` | `ShoppingRepository.kt:226` |
| 4.5 | Novo `ProductRepository` — chama `GET /products/lookup/:barcode` e `POST /products` | Novo `ProductRepository.kt` |

> Backend já faz: se `barcodeRaw` enviado sem `name` → busca OFF → preenche
> nome/categoria automaticamente. Android só precisa enviar o campo.
>
> ✅ **Fase 4 implementada (10 Ago)**: `CreateItemRequest`/`UpdateItemRequest` com
> `barcode`/`barcodeRaw`/`productId`; `ShoppingRepository.createItem` e `updateItem`
> propagam os campos (local + remoto); novo `ProductRepository.kt` com `lookup()`
> (GET `/api/products/lookup/:barcode`, salva no Room) e `create()` (POST
> `/api/products`); `ApiService` com os 2 endpoints; `ProductResponse`/
> `ProductBarcodeResponse`/`CreateProductRequest` em ApiModels. Build `assembleDebug` OK.

### FASE 5 ✅ CONCLUÍDA (10 Ago) — Scanner Bluetooth HID + Câmera + Haptics

> **Implementado e VALIDADO no device físico (Samsung `RQ8T206PXPW`, 10 Ago):**
> disparo completo do scan simulado via `adb input keyevent` (barcode
> `7899990001111` + ENTER) → match com item da lista → toggle + vibração + toast +
> PUT no backend (`checked: true`, `actualPrice` auto-calculado) → UI "Comprados: 1".
> Barcodes canônicos: `0..9 = keyevent 7..16`, `ENTER = 66`, `NUMPAD_ENTER = 134`
> (models HID reais costumam mandar 134 — o 66 injetado via adb é tragado pelo IME
> no Samsung; 134 passa).
> **Bugs encontrados e corrigidos no caminho:**
> 1. **Kotlin compiler + sparse-switch**: `keyCode == KEYCODE_ENTER || keyCode ==
>    KEYCODE_NUMPAD_ENTER` compilava para um sparse-switch cujo payload o runtime
>    rejeitava (ident packed vs sparse) → caía sempre no default. Corrigido com
>    comparação literal (`== 66 || == 134`, comentário no código).
> 2. **Callback stale**: o `DisposableEffect(btScanner)` registrava o handler da
>    1ª composição (items ainda vazios) → scan nunca dava match. Corrigido com
>    `rememberUpdatedState` (ListDetailScreen e ShoppingModeScreen).

| # | Ação | Arquivos |
|---|---|---|
| 5.1 | `MainActivity.onKeyDown/Up` detecta padrão HID (dígitos rápidos ≤50ms/tecla + Enter) | `MainActivity.kt` |
| 5.2 | `BtScannerManager` — buffer + callback unificado (mesmo handler da câmera) | Novo `BtScannerManager.kt` |
| 5.3 | `BarcodeScannerScreen` mantido como fallback de câmera | `BarcodeScannerScreen.kt` |
| 5.4 | `HapticFeedback.kt` — `VibrationEffect` (80ms sucesso, duplo erro) + `ToneGenerator` beep | Novo `HapticFeedback.kt` |

### FASE 6 — ✅ CONCLUÍDA (10 Ago) — Popup de Detalhe Pós-Scan

> **Implementado e VALIDADO no device físico (Samsung `RQ8T206PXPW`, 10 Ago):** fluxo
> completo de ponta a ponta — scan reconhecido → popup com Confirmar → PATCH checked;
> scan repetido com popup aberto → +1 na quantidade; scan desconhecido →
> "Código não reconhecido" (acende a tela) com 3 opções; **Associar à lista** →
> chooser → `POST /api/products` + `PATCH` com productId (200, sem crash) → re-scan
> do mesmo código → reconhecido. Detalhes em `docs/progress.md`.
> **Bugs corrigidos no caminho:**
> 1. **Matcher stale**: `findScanItem` lia `items` da composição (callback externo do
>    scanner cruza recomposições → lista "vazia"). Corrigido: `viewModel.uiState.value.items`.
> 2. **Crash `queueSync`**: `payload: Any` + `Json.encodeToString` → `Serializer for class 'Any'`
>    em runtime. Corrigido: assinatura `payload: String` (callers serializam tipado).
> 3. **400 no PATCH de associação**: backend rodava com `UpdateItemDto` sem `productId` —
>    recompilar `dist` não basta, o processo `node dist/main.js` precisa ser reiniciado.
> 4. **`ShoppingRepository.kt` reconstruído** após `git checkout --` acidental (working tree
>    não commitado na época) — coroutines suspensas, `RefreshRequest(refreshToken)` no body,
>    métodos de ViewModels recuperados. `assembleDebug` OK.

| # | Ação | Detalhe |
|---|---|---|
| 6.1 | `ScanPopupController` — gerencia popup atual; recebe scan → mostra; próximo scan → confirma anterior e mostra novo | **Sem timer** |
| 6.2 | Popup de **reconhecido**: nome, qtd, preço. **Não acende tela, não destrava**. | Discreto |
| 6.3 | Popup de **não reconhecido**: 3 botões. **Acende a tela** com `FLAG_TURN_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED`. | Usuário precisa agir |
| 6.4 | Botão **"Associar à lista"** → busca itens → seleciona → `POST /api/products` com barcode + categoria → vincula `productId` no item | "Manteiga Pikachu = manteiga Sonic" |
| 6.5 | Botão **"Cadastrar novo"** → cadastro com barcode preenchido | Novo Product + novo ListItem |
| 6.6 | Botão **"Genérico"** → cria "Produto [código]" sem detalhes | Editar depois |
| 6.7 | Scan repetido do mesmo código = **+1 na quantidade** da linha existente | Uma linha, qtd incrementa |
| 6.8 | Se não toca no popup e escaneia o próximo → anterior confirmado com dados que tem | Auto-confirma, sem timer |

### FASE 7 — Settings Leve

| Manter | Remover (Web only) |
|---|---|
| Perfil (nome, email do backend) | Gerenciar membros |
| Alterar senha | Regenerar convite |
| Tema (claro/escuro/sistema) — novo | Exportar backup |
| URL do servidor | Configurar Grocy/HA |
| Usar Wear OS para detalhar scan `[ ]` | Tokens |
| Sobre | — |

> A integração com Grocy é controlada por `grocyAssociated` na lista (definida no
> Web Admin) e pelo toggle "Sincronizar com Grocy" no checkout (já existe no app).
> O app não configura Grocy — apenas envia `grocySync: true` no checkout.

### FASE 8 — Onboarding Expandido

| Passo | Ação |
|---|---|
| 1-2 | Explicação + ilustrações (mantém) |
| 3 | "Conectar ao servidor" → campo URL → testa `GET /health` |
| 4 | "Entrar ou criar conta" → `GET /setup/status` → wizard ou login |
| 5 | (Futuro) Google/Apple — placeholder |

### FASE 9 — Wear OS Companion (opcional, último)

| # | Ação |
|---|---|
| 9.1 | Data Layer API: Phone → Watch envia lista ativa + progresso |
| 9.2 | `WearScanPopupScreen` — recebe scan → mostra detalhe → **acende e destrava tela** → edita → envia de volta |
| 9.3 | **Sem timer** — respeita tempo de apagamento de tela do Wear OS |
| 9.4 | Haptics no Watch ao confirmar item |
| 9.5 | Config no Phone: toggle "Usar Wear OS para detalhar scan" |

### FASE 10 — Home Real + Filtros

| # | Ação |
|---|---|
| 10.1 | Listas do Room (ativas / arquivadas) |
| 10.2 | Filtros chips ligados às queries reais |
| 10.3 | Badge do tipo da lista (Pontual/Recorrente) no card |

---

## 5. Ordem de Execução

| Ordem | Fase | Dependência | Estado |
|---|---|---|---|
| 1 | Fase 1 — Login + dados reais | Nenhuma | ✅ 09 Ago (device) |
| 2 | Fase 2 — Criar lista com tipo | Fase 1 | ✅ 09 Ago (device) |
| 3 | Fase 3 — Backend: endpoints de Product | Fase 1 | ✅ 10 Ago (API) |
| 4 | Fase 4 — Android: OFF + barcode nos models | Fase 3 | ✅ 10 Ago |
| 5 | Fase 5 — Scanner HID + Haptics | Fase 1 | ✅ 10 Ago (device) |
| 6 | Fase 6 — Popup pós-scan + Associação | Fase 4 + Fase 5 | ✅ 10 Ago (device) |
| 7 | Fase 10 — Home real + Filtros | Fase 1 | ⬜ |
| 8 | Fase 7 — Settings leve | Fase 1 | ⬜ |
| 9 | Fase 8 — Onboarding expandido | Fase 1 | ⬜ |
| 10 | Fase 9 — Wear OS | Fases 1-7 sólidas | ⬜ (opcional) |

---

## 6. Funções que NÃO funcionam agora (não gastar tempo)

| Função | Motivo | Quando volta |
|---|---|---|
| WebSocket sync em tempo real | Redis não instalado → SyncService emite WARN | Quando Redis subir em produção |
| Notificações push de outros membros | Precisa Redis + FCM | Mesmo que acima |
| Grocy sync no checkout | Grocy não rodando local | Quando Grocy for deployado (código pronto) |
| Foto de nota fiscal (receipt upload) | Precisa multipart + storage no backend | Feature futura |
| Login em dispositivo físico (LAN) | Backend em localhost | ✅ Resolvido: `adb reverse tcp:3000 tcp:3000` (validações no device físico funcionam) |
| Login Google/Apple | Backend sem OAuth implementado | Fase futura |

---

## 7. Estado Atual do Código (antes deste plano)

### O que funciona e manter

| Componente | Arquivo | Estado |
|---|---|---|
| ShoppingRepository (login, listas, itens, checkout, compras, perfil) | `ShoppingRepository.kt` | ✅ offline queue com Room |
| ApiService (52 endpoints) | `ApiService.kt` | ✅ completo |
| Room (7 entities + DAOs + SyncQueue) | `Entities.kt`, `Daos.kt` | ✅ estrutura pronta |
| ShoppingModeScreen (checkboxes, total, checkout) | `ShoppingModeScreen.kt` | ✅ carrega do servidor; precisa cache Room |
| BarcodeScannerScreen (CameraX + MLKit) | `BarcodeScannerScreen.kt` | ✅ modal full-screen |
| CheckoutDialog (pagamento + Grocy toggle) | `ShoppingModeScreen.kt:351` | ✅ envia `grocySync` |
| PurchasesScreen + ViewModel | `PurchasesScreen.kt`, `PurchasesViewModel.kt` | ✅ histórico + detalhe |
| SettingsScreen + ViewModel | `SettingsScreen.kt`, `SettingsViewModel.kt` | ✅ perfil, senha, export |
| HomeScreen (estrutura visual) | `HomeScreen.kt` | ✅ cards, filtros, busca |
| Wear screens (5 telas) | `wear/ui/screens/` | ✅ estrutura (mock) |

### O que está quebrado e precisa corrigir

| Problema | Arquivo:linha | Correção |
|---|---|---|
| Login é mock (TODO) | `LoginScreen.kt:171` | Chamar `repository.login()` |
| MainViewModel com listas mock | `MainViewModel.kt:25-31` | Observar Room |
| ListDetailScreen com itens mock | `ListDetailScreen.kt:88-96` | Novo ViewModel + Room |
| ShoppingModeViewModel: barcode = null | `ShoppingModeViewModel.kt:90` | Usar `entity.barcode` |
| CreateItemRequest sem barcodeRaw/productId | `ApiModels.kt:87` | Adicionar campos |
| UpdateItemRequest sem barcodeRaw/productId | `ApiModels.kt:96` | Adicionar campos |
| Backend sem endpoint de Product lookup | `lists.controller.ts` | Criar endpoints |

---

## 8. Instruções para o Agente

1. **Leia este documento antes de começar qualquer tarefa.**
2. **Siga a ordem de execução da seção 5.** Não pule fases.
3. **Não descarte o que já funciona** (seção 7). Melhore por cima.
4. **Não implemente funções da lista da seção 6.** São perda de tempo agora.
5. **Admin fica no Web.** Android não gerencia membros, tokens, Grocy config, MODELO.
6. **Single-household.** Sem tela de seleção de casa.
7. **Scanner é acelerador opcional.** Sem tela de seleção de modo de compra.
8. **Popup pós-scan: sem timer.** Só o próximo scan confirma o anterior.
9. **Sempre rode `assembleDebug`** (não só `compileDebugKotlin`) para o dispositivo ver mudanças.
10. **JDK 17 obrigatório:** `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`
11. **Não faça commit sem aprovação explícita do usuário.**
12. **A cada fase concluída, valide com build + instalação no dispositivo.**
13. **Grocy: o app só envia `grocySync: true` no checkout. O backend faz todo o trabalho
    de match por barcode → Product → GrocyProduct → addStockEntry.**
14. **ON FOOD FACTS: o app envia `barcodeRaw`. O backend busca OFF, preenche nome/categoria,
    e salva `offData` no item. O app NÃO chama OFF diretamente.**
