# Handoff — Sessão Android (10 Ago 2026)

Documento de continuidade. Propósito: retomar o trabalho após compactação de contexto,
sem perder o estado das decisões, arquivos e validações.

> **PLANO OFICIAL:** Ver `docs/ANDROID-PLAN.md` — documento aprovado com todas as fases,
> decisões, ordem de execução e detalhes técnicos. **Siga a ordem da seção 5 desse documento.**

---

## 1. Estado geral

- **Projeto:** Listaih (Fase 4 — Android em desenvolvimento). Branch: `master`; remote `origin` →
  **github.com/phgsbr/listaih** (público, configurado 10 Ago).
- **Fases 1–6 do plano Android concluídas e validadas no device físico** (ver §4).
- **Working tree commitado (10 Ago):** 5 commits — `1b20e84` (gitignore debug), `6fdd063`
  (backend barcode→product), `4524abb` (gitignore .env), `f6e93e7` (android Fase 5/6 + wrapper
  Gradle 8.4), `8ae4d6b` (docs). Push em `origin/master`.
- Backend (NestJS) e Admin (React) estão em fases concluídas.

## 2. Decisões do usuário (a preservar)

1. **Single-household** — sem tela de seleção de casa.
2. **Categorias em ordem alfabética** no admin.
3. Scanner BT 2D HID externo é o caminho principal; câmera só fallback (sem PiP/overlay).
4. **SEM orçamento mensal; SEM busca de preço no histórico.**
5. Android cria listas **PONTUAL/RECORRENTE** — nunca MODELO (MODELO é só Web).
6. Fluxo de compra **livre**, sem modo A/B; scanner = acelerador opcional.
7. **Popup pós-scan sem timer** — só o próximo scan confirma o anterior.
8. Scan repetido **soma quantidade**.
9. Wear OS opcional com toggle.
10. OFF via backend (`barcodeRaw`); Grocy via `grocySync: true` no checkout.
11. Admin fica no Web — Android não gerencia membros, tokens, Integrations, MODELO.
12. **Members NÃO é necessário** no Android; **Purchases É necessário** (já implementado).

## 3. Ambiente / comandos úteis (IMPORTANTE)

- **JDK padrão do PATH é Java 8** — incompatível com AGP 8.4.2. Usar o JBR 17 do Android Studio:
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
  ```
- **Sempre rodar `assembleDebug`** (não só `compileDebugKotlin` — o dispositivo não vê mudanças):
  ```powershell
  .\gradlew.bat :app:assembleDebug --console=plain
  ```
- **APK gerado:** `apps/android/app/build/outputs/apk/debug/app-debug.apk`
- **Dispositivo:** `RQ8T206PXPW` (físico). adb em `C:\Users\Pedro Henrique\AppData\Local\Android\Sdk\platform-tools\adb.exe`.
- Instalar + abrir (preserva dados):
  ```powershell
  adb install -r "app\build\outputs\apk\debug\app-debug.apk"
  adb shell am force-stop com.listaih.app
  adb shell monkey -p com.listaih.app -c android.intent.category.LAUNCHER 1
  ```
- **Rede:** baseUrl default `http://127.0.0.1:3000` + `adb reverse tcp:3000 tcp:3000`
  (manifest tem `usesCleartextTraffic="true"`).
- **Validação de UI:** `adb shell uiautomator dump /sdcard/ui.xml` + `adb pull` + regex
  (screenshot PNG corrompe via `adb exec-out` no PowerShell).
- **Scanner HID simulado:** `adb shell "input keyevent 14 15 16 16 16 16 7 7 7 8 8 8 8 134"`
  (dígitos 0–9 = keyevents 7–16; ENTER=66 tragado pelo IME no Samsung → usar NUMPAD_ENTER=134;
  idle 3s do BtScannerManager permite rajada única). Barcode de teste: `7899990001111`.
- **Timeout da tela:** se testar por muito tempo, `settings put system screen_off_timeout 1800000`
  durante o teste; **restaurar `30000` ao final**. Keyguard exige desbloqueio manual.

## 4. Fases do plano Android (estado)

| Fase | Status | Resumo |
|---|---|---|
| 1 — Login + dados reais | ✅ 09 Ago | Login real, Home/Detail reais, barcode nos models, refresh+retry automático |
| 2 — Criar lista com tipo | ✅ 09 Ago | AddListScreen PONTUAL/RECORRENTE, badge no card |
| 3 — Backend: endpoints de Product | ✅ 10 Ago (API) | lookup por barcode (OFF fallback), create, ProductBarcode alternativo |
| 4 — Android: OFF + barcode nos models | ✅ 10 Ago | ProductRepository, barcode/barcodeRaw/productId nos requests |
| 5 — Scanner HID + Haptics | ✅ 10 Ago (device) | BtScannerManager, HapticFeedback, LocalBtScanner |
| 6 — Popup pós-scan + Associação | ✅ 10 Ago (device) | Fluxo completo: reconhecer → Confirmar/+1 → desconhecido → Associar à lista → re-scan reconhece |
| 10 — Home real + Filtros | ⬜ | — |
| 7 — Settings leve | ⬜ | — |
| 8 — Onboarding expandido | ⬜ | — |
| 9 — Wear OS | ⬜ (opcional) | Estrutura mock pronta |

## 5. Lições técnicas (não regredir)

1. **`addedAt` vs `createdAt`**: API de itens envia `addedAt`. `ListItemResponse.addedAt: String?`,
   `toEntity` usa `addedAt ?: updatedAt`. Foi a causa do eterno "Itens (0)".
2. **Refresh JWT**: `/api/auth/refresh` recebe `RefreshRequest` no **body** (header quebra → 401 em loop).
   `AuthInterceptor` retry único + client próprio + pula login/refresh.
3. **Sparse-switch bug**: `keyCode == KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER` compila para
   sparse-switch com payload rejeitado pelo runtime (sempre default). Usar literais `== 66 || == 134`.
4. **Callback stale**: `DisposableEffect(btScanner)` + closure não atualiza → usar
   `rememberUpdatedState` nos handlers de scan (ListDetailScreen e ShoppingModeScreen).
5. **Icon vs category 400**: whitelist backend `['Alimentos','Farmacia','Papelaria','Material de Construcao','Geral']` —
   AppNavHost mapeia ícone → categoria antes de criar lista.
6. **Room**: `fallbackToDestructiveMigration()` — schema novo não pede migration manual em dev.
7. **UTF-8**: nunca editar arquivos com acentos via PowerShell `Set-Content` (corrompe). Usar Edit tool.
8. **$ sh adb**: no PowerShell usar `& $adb ...` (adb é executável/path do SDK).
9. **Matcher de scan no popup (Fase 6)**: `findScanItem` deve ler `viewModel.uiState.value.items` —
   nunca o `items` capturado da composição (callback externo do scanner cruza recomposições → lista "vazia").
10. **`queueSync` com `payload: Any` crasha em runtime** (`Serializer for class 'Any' is not found`).
    Assinatura é `payload: String` — serializar tipado antes (`Json.encodeToString(request)`).
11. **PATCH de item com `productId`**: requer `UpdateItemDto.productId` no source **e processo reiniciado**
    (recompilar `dist` não basta — `node dist/main.js` precisa ser reiniciado).
12. **PowerShell `Set-Content` em Kotlin/TS corrompe UTF-8** (arquivo quebrado); desfazer com
    `git checkout --` apaga trabalho não commitado — preferir reconstruir com Edit tool + antes commit.

## 6. Validações feitas

- Fase 6 validada no device (02:00–02:35 de 10 Ago): popup reconhecido (Confirmar → PATCH checked),
  +1 por scan repetido (2→3 unit), popup "Código não reconhecido" (123500123501) → Associar à lista
  → chooser → PATCH 200 com productId **sem crash** → re-scan reconhece o código recém-vinculado.
- Fase 5 validada no device (00:58 de 10 Ago): scan simulado → `barcode complete: 7899990001111`
  → toast + vibração → PUT no backend (`checked: true`, `actualPrice: 19`, `checkedAt`)
  → UI "Comprados: 1" + "✓ por current_user". Repetido 2x (NUMPAD_ENTER).
- Build `compile_out28.txt` (final, sem logs de debug) EXIT=0; install + smoke OK.
- Refresh+retry validado: Home e ListDetail retornaram 200 após 15min de sessão.
- Fase 3 validada por API: 5/5 testes de lookup/create/barcode; `nest build` EXIT=0.
- Fases 1–2 validadas no device (09 Ago): login real, Home com listas reais + badges,
  criação de lista "Teste App" como RECORRENTE confirmada no backend.
- **Não testável neste ambiente:** Redis/WebSocket sync (não instalado), push, Grocy local,
  foto de nota fiscal, login Google/Apple

## 7. Pendências / próximos passos

1. **Fase 10 — Home real + Filtros** (próxima): chips ligados às queries Room reais
   (ativas/arquivadas), badge do tipo no card (estrutura visual já existe em HomeScreen.kt).
2. Fase 7 — Settings leve (tema claro/escuro, manter Perfil/Senha/URL/Sobre); Fase 8 — Onboarding
   expandido (URL do servidor + `/setup/status`); Fase 9 — Wear OS companion (opcional).
3. Manter docs atualizados após cada fase (`progress.md`, `HANDOFF-android.md`) — atualizados em 10 Ago.

### 7.1 Working tree (estado em 10 Ago 2026)

- **Tudo commitado** via 5 commits (ver §1). Repo: `github.com/phgsbr/listaih` (público), branch `master`.
- `docs/progress.md` registra: Fase 6 validada, fixes (matcher stale, queueSync `payload: String`,
  DTO productId + restart do backend), ShoppingRepository reconstruído — se precisar, atualizar.
- Temporários fora do git (gitignore): `compile_out*.txt`, `ui_dump_*.xml`, `backend_run.log.err`,
  `gradle-temp/`, zip do Gradle, screenshots; `_tmp_login.json` **deletado** (continha JWT).
  `.env` do backend fora do repo (gitignore + commit `4524abb`).

## 8. Riscos / limitações conhecidas

- Redis não instalado (SyncService usa WARN).
- Docker não instalado no ambiente (compose do deploy não testado).
- Keyguard pode bloquear testes longos (desbloqueio manual do usuário).
- Auditoria de UI depende de uiautomator dump (regex), não de screenshots.

## 9. Como retomar

1. Ler este doc + `docs/ANDROID-PLAN.md` seção 8 (Instruções para o Agente).
2. Rodar `git status` + `git diff` para rever o working tree (repo: `github.com/phgsbr/listaih` — público).
3. Aplicar JDK 17 ANTES de qualquer comando gradle.
4. Continuar pela **Fase 10** do plano (Home real + Filtros) — depende só da Fase 1 (pronta).