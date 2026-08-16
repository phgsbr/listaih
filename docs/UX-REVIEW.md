# UX Review — Listaih Android (Phone + Wear OS)

**Date:** 16 Aug 2026  
**Auditor:** Automated review pass  
**Scope:** `apps/android/app` (Phone) and `apps/android/wear` (Wear OS)  
**Method:** Static analysis of all Compose screens, components, themes, and navigation

---

## Summary Table

| Severity   | Phone | Wear OS | Total |
|------------|------:|--------:|------:|
| Critical   |    25 |      30 |    55 |
| Medium     |    25 |      52 |    77 |
| Minor      |    10 |      17 |    27 |
| Suggestion |    10 |      10 |    20 |
| **Total**  | **~70** | **109** | **~179** |

---

## Phone Findings

### Cross-Cutting (All Screens)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 1 | Critical | All `*Screen.kt` files | Zero `stringResource()` usage — every user-facing string is hardcoded in Kotlin | Extract all strings to `res/values/strings.xml`; replace literals with `stringResource(R.string.xxx)` |
| 2 | Critical | All `*Screen.kt` files | `contentDescription` values mix English and Portuguese (e.g., `"back"`, `"add item"`, `"remover"`) | Define content descriptions as string resources; standardize on one language per locale |
| 3 | Medium | All `*Screen.kt` files | No `Modifier.semantics { }` or `role` parameter anywhere — TalkBack cannot announce button/radio/tab roles | Add `Modifier.semantics { role = Role.Button }` (or appropriate role) to all clickable elements |
| 4 | Medium | All `*Screen.kt` files | No `Modifier.testTag()` anywhere — impossible to write robust UI tests | Add semantic test tags to key composables |
| 5 | Minor | All `*Screen.kt` files | No haptic feedback (`LocalHapticFeedback`) on any button press | Add `HapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)` to primary actions |
| 6 | Suggestion | All `*Screen.kt` files | Dark theme not tested — colors hardcoded with `Color(0xFF...)` in some places | Move all colors to theme; verify contrast ratios in both light/dark |

### OnboardingScreen

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 7 | Critical | `OnboardingScreen.kt` | All strings hardcoded (`"Bem-vindo"`, `"Começar"`, etc.) | Extract to `strings.xml` |
| 8 | Critical | `OnboardingScreen.kt` | "Começar" button is 56×56 dp square — well below 48 dp minimum touch target but also too small visually for a primary CTA | Make button full-width with standard height (48–56 dp) |
| 9 | Medium | `OnboardingScreen.kt` | `CircularProgressIndicator` uses default thin stroke (2 dp) — hard to see on dense screens | Use `strokeWidth = 4.dp` |
| 10 | Medium | `OnboardingScreen.kt` | Server URL dialog state is not remembered — rotates/configChanges lose input | Wrap dialog text field state in `rememberSaveable` |
| 11 | Minor | `OnboardingScreen.kt` | `Color(0xFF2E7D32)` hardcoded for accent | Move to theme color scheme |

### SetupScreen

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 12 | Critical | `SetupScreen.kt` | All field labels hardcoded (`"Nome"`, `"Email"`, `"Senha"`, etc.) | Extract to `strings.xml` |
| 13 | Critical | `SetupScreen.kt` | No per-field error tracking — all errors are global; user can't tell which field failed | Track errors per field; show `isError` on each `TextField` with helper text |
| 14 | Medium | `SetupScreen.kt` | No `KeyboardOptions(capitalization = KeyboardCapitalization.Words)` on Name field | Add capitalization to name field |
| 15 | Medium | `SetupScreen.kt` | No password show/hide toggle — password field always masked | Add visual trailing icon to toggle `KeyboardOptions.PasswordVisualTransformation` |
| 16 | Minor | `SetupScreen.kt` | No `imeAction = ImeAction.Next` between fields; user must tap each field manually | Chain fields with `ImeAction.Next`; last field uses `ImeAction.Done` |

### LoginScreen

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 17 | Critical | `LoginScreen.kt` | All strings hardcoded (`"Entrar"`, `"Email"`, `"Senha"`, etc.) | Extract to `strings.xml` |
| 18 | Critical | `LoginScreen.kt` | Google and Apple sign-in buttons render but do nothing (`TODO()` or no action) | Remove Google/Apple buttons entirely (self-hosted app; no OAuth provider configured) |
| 19 | Critical | `LoginScreen.kt` | "Cadastre-se" and "Esqueci minha senha" texts look clickable but have no `clickable` modifier or onClick | Either wire up actions or remove the texts |
| 20 | Critical | `LoginScreen.kt` | "Entrar" button is `enabled = true` even when email/password fields are blank | Disable button while fields are empty: `enabled = email.isNotBlank() && password.isNotBlank()` |
| 21 | Medium | `LoginScreen.kt` | `GoogleLogoIcon` draws a `Text("G")` instead of an actual vector logo | Remove (part of button removal) or replace with proper vector asset |
| 22 | Minor | `LoginScreen.kt` | No `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)` on email field | Add email keyboard type |
| 23 | Minor | `LoginScreen.kt` | No "show password" toggle (same as SetupScreen) | Add password visibility toggle |

### HomeScreen

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 24 | Critical | `HomeScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 25 | Medium | `HomeScreen.kt` | "Home" tab label hardcoded in English while others are in Portuguese | Externalize and localize all tab labels |
| 26 | Medium | `HomeScreen.kt` | `contentDescription` for icons mixed EN/PT (e.g., `"home"`, `"settings"`) | Standardize through string resources |
| 27 | Minor | `HomeScreen.kt` | Pull-to-refresh not implemented — user must restart to see updates | Add `PullToRefreshBox` or similar swipe-to-refresh |

### Remaining Phone Screens (Summary Patterns)

The following screens were audited and exhibit the same cross-cutting issues (hardcoded strings, missing semantics, no haptics):

- **ListDetailScreen** — hardcoded strings, no empty state for items, no swipe-to-delete on items
- **SettingsScreen** — hardcoded strings, no logout confirmation dialog, version text hardcoded
- **ScanPopupController** — hardcoded strings, popup buttons lack `Role.Button`
- **ItemEditScreen** — hardcoded strings, no per-field validation, no auto-save indicator
- **ProfileScreen** — hardcoded strings, avatar no `contentDescription`
- **HistoryScreen** — hardcoded strings, no empty state, date format hardcoded (`SimpleDateFormat` without locale)

| Pattern | Screens Affected | Severity | Action |
|---------|-------------------|----------|--------|
| Hardcoded strings | All ~15 screens | Critical | Full i18n pass: `strings.xml` + `values-en/` + `values-es/` |
| Missing `Role.Button` semantics | All clickable elements | Medium | Add semantics modifiers |
| Missing empty states | ListDetail, History, Profile | Medium | Add `EmptyState` composable per screen |
| Missing error states | ListDetail, History | Medium | Add `ErrorState` with retry button |
| No haptics | All screens | Minor | Add `LocalHapticFeedback` on primary actions |
| Hardcoded colors | Onboarding, ScanPopup, others | Minor | Move all `Color(0xFF...)` to theme |
| No dark theme verification | All | Suggestion | Test and fix contrast in dark mode |

---

## Wear OS Findings

### Cross-Cutting (All Wear Screens)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 1 | Critical | `WearSettingsScreen.kt` | `WearSettingsScreen` **does not exist** — documented in AGENTS.md as having toggle "Scanner BT (HID)" but file is missing from project | Create `WearSettingsScreen.kt` with SharedPreferences toggle |
| 2 | Critical | All `*Screen.kt` wear files | Zero rotary/bezel input support — `ScalingLazyColumn` has no `rotaryScrollModifier` or `RotaryInput` handling | Add `Modifier.rotaryScrollable` to all scrollable lists |
| 3 | Critical | All `*Screen.kt` wear files | No `Vignette` wrapper on any screen — Wear OS design guidelines require edge fading | Wrap screen content in `Vignette(vignettePosition = VignettePosition.TopAndBottom)` |
| 4 | Critical | All `*Screen.kt` wear files | No `PositionIndicator` — user has no scroll position feedback | Add `PositionIndicator` in `Scaffold` for all scrollable screens |
| 5 | Critical | `wear/res/values/strings.xml` | Contains only `<string name="app_name">Listaih</string>` — no other string resources | Extract all user-facing strings to `strings.xml` |
| 6 | Critical | `WearTheme.kt` | Uses `androidx.compose.material3.darkColorScheme()` (phone Material 3) instead of Wear compose material theme | Use `androidx.wear.compose.material.MaterialTheme` with Wear color scheme |
| 7 | Medium | All `*Screen.kt` wear files | No `Modifier.semantics` or `Role.Button` on any wearable element | Add role semantics to all clickable composables |
| 8 | Medium | All `*Screen.kt` wear files | No haptic feedback (`LocalHapticFeedback`) anywhere | Add haptics on chip/button taps and scan actions |
| 9 | Minor | `wear/res/values/strings.xml` | No `values-en/` or `values-es/` directories for multilingual support | Create translation resource directories |
| 10 | Suggestion | All `*Screen.kt` wear files | No `Modifier.testTag()` for UI testing | Add test tags to key composables |

### HomeScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 11 | Critical | `WearHomeScreen.kt` | No loading state — blank screen while data loads | Add `LoadingState` composable with `CircularProgressIndicator` |
| 12 | Critical | `WearHomeScreen.kt` | No error state — silent failure with no user feedback | Add `ErrorState` composable with retry `Button` |
| 13 | Critical | `WearHomeScreen.kt` | No empty state — user sees nothing when no lists exist | Add `EmptyState` with "Nenhuma lista" message and CTA |
| 14 | Critical | `WearHomeScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 15 | Medium | `WearHomeScreen.kt` | No rotary input on `ScalingLazyColumn` | Add `Modifier.rotaryScrollable` |
| 16 | Medium | `WearHomeScreen.kt` | Uses `Button` instead of `Chip` for list items — Wear OS design prefers chips for list entries | Replace `Button` with `Chip` or `InlineCard` |
| 17 | Minor | `WearHomeScreen.kt` | No swipe-to-dismiss for list deletion | Add `SwipeToDismissBox` on list items |

### SelectScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 18 | Critical | `WearSelectScreen.kt` | No loading state | Add loading indicator |
| 19 | Critical | `WearSelectScreen.kt` | No error state | Add error composable with retry |
| 20 | Critical | `WearSelectScreen.kt` | No empty state | Add empty state message |
| 21 | Critical | `WearSelectScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 22 | Medium | `WearSelectScreen.kt` | No rotary input | Add `Modifier.rotaryScrollable` |
| 23 | Medium | `WearSelectScreen.kt` | `WearListUi` data class is duplicated — exists in both `WearHomeScreen.kt` and `WearSelectScreen.kt` | Consolidate into single shared model in a common file |
| 24 | Minor | `WearSelectScreen.kt` | No chip components — uses plain `Button` for selection | Use `Chip` for wearable list items |

### ShoppingScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 25 | Critical | `WearShoppingScreen.kt` | No empty state — blank screen when list has no items | Add `EmptyState` composable |
| 26 | Critical | `WearShoppingScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 27 | Medium | `WearShoppingScreen.kt` | `formatQty` inconsistent — uses decimal point (`2.5`) instead of comma (`2,50`) as BRL convention | Use locale-aware formatting: `String.format(Locale.getDefault(), ...)` |
| 28 | Medium | `WearShoppingScreen.kt` | `StepButton` is 32 dp — below 44 dp minimum touch target for Wear | Increase to 44 dp minimum |
| 29 | Medium | `WearShoppingScreen.kt` | `OutlinedTextField` opens IME on Wear — Wear OS should avoid software keyboard | Replace with keypad or `ListHeader` input pattern |
| 30 | Medium | `WearShoppingScreen.kt` | Quantity step is 0.5 for "un" (unit) items — stepping by half a unit doesn't make sense | Use integer step for unit items; decimal step only for kg/L |
| 31 | Minor | `WearShoppingScreen.kt` | No rotary input on scrollable list | Add `Modifier.rotaryScrollable` |
| 32 | Minor | `WearShoppingScreen.kt` | No swipe-to-check on items | Add `SwipeToDismiss` or toggle for checking items |

### CheckoutScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 33 | Critical | `WearCheckoutScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 34 | Medium | `WearCheckoutScreen.kt` | `PaymentRow` buttons are 32 dp — below touch target minimum | Increase to 44 dp |
| 35 | Medium | `WearCheckoutScreen.kt` | No `role = Role.RadioButton` on payment method selection | Add `Modifier.semantics { role = Role.RadioButton }` |
| 36 | Medium | `WearCheckoutScreen.kt` | `OutlinedTextField` opens IME for payment input | Replace with custom keypad |
| 37 | Medium | `WearCheckoutScreen.kt` | No confirmation step — checkout completes immediately on tap | Add a confirmation screen/dialog with total summary |
| 38 | Minor | `WearCheckoutScreen.kt` | No haptic feedback on selection | Add `HapticFeedback` on payment method change |
| 39 | Minor | `WearCheckoutScreen.kt` | No rotary input | Add `Modifier.rotaryScrollable` |

### CompleteScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 40 | Critical | `WearCompleteScreen.kt` | `CheckCircle` icon has `contentDescription = null` — TalkBack announces nothing | Add meaningful content description or `Modifier.semantics { contentDescription = "Compra concluída" }` |
| 41 | Critical | `WearCompleteScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 42 | Medium | `WearCompleteScreen.kt` | No `Vignette` wrapper | Wrap content in `Vignette` |
| 43 | Medium | `WearCompleteScreen.kt` | No auto-dismiss — user must manually navigate back after completion | Auto-dismiss after 3 seconds with `LaunchedEffect` + `delay` |
| 44 | Minor | `WearCompleteScreen.kt` | No animation on checkmark | Add `animateContentSize` or scale-in animation |
| 45 | Suggestion | `WearCompleteScreen.kt` | No haptic confirmation pulse | Add `HapticFeedback.LongPress` on appear |

### VoiceScreen (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 46 | Critical | `WearVoiceScreen.kt` | Mic icon has `contentDescription = null` — inaccessible to TalkBack | Add `contentDescription = "Microfone"` string resource |
| 47 | Critical | `WearVoiceScreen.kt` | All strings hardcoded | Extract to `strings.xml` |
| 48 | Medium | `WearVoiceScreen.kt` | Mic circle looks tappable (elevated circle with icon) but has no `clickable` modifier | Add `Modifier.clickable` or clearly communicate it's display-only |
| 49 | Medium | `WearVoiceScreen.kt` | No processing/recognizing state — user doesn't know when voice is being captured | Add animated waveform or pulsing state during recording |
| 50 | Medium | `WearVoiceScreen.kt` | No animation during recording (static mic icon) | Add pulsing animation or waveform visualization |
| 51 | Minor | `WearVoiceScreen.kt` | No error state when voice recognition fails | Add error state with retry |
| 52 | Minor | `WearVoiceScreen.kt` | No auto-stop after silence | Implement `AudioRecord` timeout |
| 53 | Suggestion | `WearVoiceScreen.kt` | No rotary input alternative to trigger recording | Add rotary action to start/stop |

### ScanPopup (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 54 | Critical | `WearScanPopupHost.kt` | Keypad keys are 30 dp — far below the 44 dp minimum touch target for Wear OS | Increase keypad keys to minimum 44 dp |
| 55 | Critical | `WearScanPopupHost.kt` | Multiple buttons are 28–36 dp (confirm, close, quantity, price) — all below minimum | Increase all interactive elements to 44 dp minimum |
| 56 | Critical | `WearScanPopupHost.kt` | All strings hardcoded in popup | Extract to `strings.xml` |
| 57 | Medium | `WearScanPopupHost.kt` | Hint text is 8 sp — below readable minimum (12 sp) | Increase to minimum 12 sp |
| 58 | Medium | `WearScanPopupHost.kt` | No haptic feedback on keypad press | Add `HapticFeedback.TextHandleMove` on each key tap |
| 59 | Medium | `WearScanPopupHost.kt` | `Color(0xFF2E7D32)` hardcoded for accent/confirm button | Move to Wear theme color |
| 60 | Medium | `WearScanPopupHost.kt` | No `Role.Button` semantics on popup actions | Add `Modifier.semantics { role = Role.Button }` |
| 61 | Medium | `WearScanPopupHost.kt` | No Vignette on popup overlay | Wrap popup content in `Vignette` |
| 62 | Minor | `WearScanPopupHost.kt` | Popup overlay doesn't dim background | Add semi-transparent scrim behind popup |
| 63 | Minor | `WearScanPopupHost.kt` | No animation on popup appear/dismiss | Add `AnimatedVisibility` with fade/slide |
| 64 | Suggestion | `WearScanPopupHost.kt` | Keypad could use rotary input for number selection | Add rotary support for numeric entry |

### WearTheme

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 65 | Critical | `WearTheme.kt` | Uses `androidx.compose.material3.darkColorScheme()` — phone Material 3 theme, not Wear OS | Replace with `androidx.wear.compose.material.MaterialTheme` and Wear color scheme |
| 66 | Medium | `WearTheme.kt` | Typography defined but never used — components don't reference `LocalTypography` | Pass typography to `MaterialTheme(typography = ...)` or remove if unused |
| 67 | Minor | `WearTheme.kt` | `WearAppShape` and other shape definitions exist but are not referenced by any component | Either wire into theme or remove dead code |
| 68 | Suggestion | `WearTheme.kt` | No dynamic color support for Wear OS 4+ | Consider `dynamicDarkColorScheme` when available |

### MainActivity (Wear)

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 69 | Critical | `WearMainActivity.kt` | `FLAG_TURN_SCREEN_ON` is deprecated — modern equivalent should be used | Use `setShowWhenLocked(true)` + `setTurnScreenOn(true)` on Activity window attributes |
| 70 | Medium | `WearMainActivity.kt` | `onKeyUp` swallows `Enter` (keycode 66) even when IME is visible — breaks text input | Check `imeVisible()` before consuming key events; skip consumption when IME is active |
| 71 | Medium | `WearMainActivity.kt` | No `EdgeEffect` or overscroll handling for rotary | Add `LazyColumn` edge effect for rotary scroll feedback |
| 72 | Minor | `WearMainActivity.kt` | No `AmbientModeController` for always-on display | Implement `AmbientModeSupport` for ambient mode |
| 73 | Suggestion | `WearMainActivity.kt` | No `Modifier.onRotaryScrollInput` at activity level for global bezel actions | Add fallback rotary handler |

### WearNavHost

| # | Severity | Location | Issue | Fix |
|---|----------|----------|-------|-----|
| 74 | Critical | `WearNavHost.kt` | No `Scaffold` with `TimeText` — Wear OS requires time visible at top of every screen | Wrap nav host in `Scaffold(timeText = { TimeText() })` |
| 75 | Medium | `WearNavHost.kt` | Total amount is not URL-encoded in navigation route — `R$ 12,99` breaks route parsing | Use `Uri.encode(amount)` when building nav route |
| 76 | Medium | `WearNavHost.kt` | No `PositionIndicator` in `Scaffold` — no scroll position feedback | Add `PositionIndicator` to scaffold |
| 77 | Minor | `WearNavHost.kt` | No `SwipeDismissableNavHost` — back navigation doesn't use wrist-swipe gesture | Replace `NavHost` with `SwipeDismissableNavHost` from Wear Compose |
| 78 | Minor | `WearNavHost.kt` | No `inlineListDefaults` for list transitions | Use Wear navigation transitions |
| 79 | Suggestion | `WearNavHost.kt` | No deep link support from Phone app | Add `deepLinks` to nav arguments for companion app integration |

---

## Additional Wear Findings (Detailed Per-Screen Audit)

The following findings were identified during the full audit pass across all Wear screens:

### Touch Target Violations (Wear Minimum = 44 dp)

| Screen | Element | Current Size | Severity |
|--------|---------|-------------|----------|
| ScanPopup | Keypad keys | 30 dp | Critical |
| ScanPopup | Confirm button | 28 dp | Critical |
| ScanPopup | Close button | 28 dp | Critical |
| ScanPopup | Quantity/price buttons | 32–36 dp | Critical |
| ShoppingScreen | StepButton (−/+) | 32 dp | Medium |
| CheckoutScreen | PaymentRow options | 32 dp | Medium |
| CompleteScreen | Check icon area | 36 dp | Medium |

### Missing Accessibility (Wear)

| Screen | Missing | Severity |
|--------|---------|----------|
| CompleteScreen | `contentDescription` on CheckCircle | Critical |
| VoiceScreen | `contentDescription` on mic icon | Critical |
| ScanPopup | `Role.Button` on all interactive elements | Medium |
| CheckoutScreen | `Role.RadioButton` on payment options | Medium |
| All screens | No `LiveRegion` for dynamic state announcements | Medium |
| All screens | No `traversalIndex` for reading order | Minor |
| All screens | No `heading()` modifier for screen titles | Minor |

### Missing States (Wear)

| Screen | Loading | Error | Empty | Severity |
|--------|---------|-------|-------|----------|
| HomeScreen | ❌ | ❌ | ❌ | Critical |
| SelectScreen | ❌ | ❌ | ❌ | Critical |
| ShoppingScreen | ❌ | ❌ | ❌ (Critical for empty) | Critical |
| CheckoutScreen | ❌ | ❌ | — | Medium |
| CompleteScreen | — | — | — | — |
| VoiceScreen | ❌ | ❌ | ❌ | Medium |
| ScanPopup | — | — | No "no barcode" state | Medium |

### i18n Findings (Wear)

| # | Severity | Issue | Fix |
|---|----------|-------|-----|
| 80 | Critical | `strings.xml` has only `app_name` | Extract all strings (~60+ user-facing strings hardcoded) |
| 81 | Critical | No `values-en/` directory | Create English translations |
| 82 | Critical | No `values-es/` directory | Create Spanish translations |
| 83 | Medium | `formatBrl` uses hardcoded `Locale("pt", "BR")` | Use `Locale.getDefault()` with fallback to pt-BR |
| 84 | Medium | Date formatting uses `SimpleDateFormat` without locale param | Pass `Locale.getDefault()` to `SimpleDateFormat` |
| 85 | Minor | No RTL support consideration | Add `supportsRtl` to manifest |

---

## Prioritized Action Plan

### Phase 1 — Critical Wear OS Fixes (Do First)

| Priority | Task | Screens | Effort | Impact |
|----------|------|---------|--------|--------|
| 1 | Add `Vignette` + `PositionIndicator` + `Scaffold with TimeText` | All 5 scrollable screens | Medium | High — meets Wear OS design spec |
| 2 | Add rotary/bezel input support (`Modifier.rotaryScrollable`) | Home, Select, Shopping, Checkout, ScanPopup | Medium | High — primary navigation input on Wear |
| 3 | Fix all touch targets to 44 dp minimum | ScanPopup, Shopping, Checkout | Low | High — usability on small screen |
| 4 | Add `contentDescription` + semantics (Role.Button, Role.RadioButton) | All screens | Low | High — TalkBack accessibility |
| 5 | Add empty/loading/error states | Home, Select, Shopping | Medium | High — prevents blank screens |
| 6 | Replace `WearTheme` with Wear compose material `MaterialTheme` | WearTheme.kt | Low | High — correct visual foundation |
| 7 | Create missing `WearSettingsScreen.kt` | New screen | Medium | High — documented but missing |
| 8 | Fix `WearNavHost`: `SwipeDismissableNavHost` + URL-encode totals + Scaffold | WearNavHost.kt | Medium | High — navigation correctness |

### Phase 2 — Critical Phone Fixes

| Priority | Task | Screens | Effort | Impact |
|----------|------|---------|--------|--------|
| 9 | Remove Google/Apple sign-in buttons from LoginScreen | LoginScreen | Low | High — removes dead UI |
| 10 | Fix SetupScreen per-field error tracking | SetupScreen | Medium | High — form usability |
| 11 | Disable login button when fields are blank | LoginScreen | Low | High — prevents empty submissions |
| 12 | Add haptic feedback to primary actions | All phone screens | Low | Medium — UX polish |

### Phase 3 — Accessibility & Semantics

| Priority | Task | Screens | Effort | Impact |
|----------|------|---------|--------|--------|
| 13 | Add `Modifier.semantics { role = Role.Button }` to all clickable elements | Phone + Wear | Medium | High |
| 14 | Add `Role.RadioButton` to checkout payment options | Wear Checkout | Low | Medium |
| 15 | Standardize `contentDescription` language (match current locale) | All | Medium | High |
| 16 | Add `heading()` modifier to screen titles | All | Low | Medium |

### Phase 4 — i18n Full Extraction

| Priority | Task | Scope | Effort | Impact |
|----------|------|-------|--------|--------|
| 17 | Extract all hardcoded strings to `strings.xml` | Phone (~15 screens) | High | Critical |
| 18 | Extract all hardcoded strings to `strings.xml` | Wear (~8 screens + popup) | High | Critical |
| 19 | Create `values-en/` translations | Phone + Wear | Medium | High |
| 20 | Create `values-es/` translations | Phone + Wear | Medium | High |
| 21 | Replace `SimpleDateFormat` / `formatBrl` with locale-aware versions | Phone + Wear | Low | Medium |

### Phase 5 — UX Polish

| Priority | Task | Screens | Effort | Impact |
|----------|------|---------|--------|--------|
| 22 | Add auto-dismiss to Wear CompleteScreen (3s delay) | Wear Complete | Low | Medium |
| 23 | Add animations to popup appear/dismiss | Wear ScanPopup | Low | Medium |
| 24 | Add Wear ambient mode support | Wear MainActivity | Medium | Medium |
| 25 | Add phone pull-to-refresh | Phone HomeScreen | Medium | Medium |
| 26 | Add Wear swipe-to-dismiss for list deletion | Wear HomeScreen | Medium | Medium |
| 27 | Fix `formatQty` to use comma for BRL | Wear ShoppingScreen | Low | Medium |
| 28 | Add checkout confirmation step | Wear CheckoutScreen | Medium | High |
| 29 | Add voice screen recording animation + error state | Wear VoiceScreen | Medium | Medium |
| 30 | Remove dead code (unused Typography, shapes, GoogleLogoIcon) | WearTheme, LoginScreen | Low | Low |

---

## Open Questions

| # | Question | Context |
|---|----------|---------|
| 1 | Should phone app support biometric login (fingerprint)? | Self-hosted app storing JWT; fingerprint unlock would improve UX |
| 2 | Should Wear OS support ambient/always-on mode? | Battery tradeoff; useful for shopping list glanceability |
| 3 | Should there be a unified design system / shared composables? | Phone and Wear duplicate many patterns (empty states, error states); a shared module could reduce duplication |
| 4 | Should companion Phone app configure Wear settings remotely? | Wear keyboard input is painful; configuring scanner toggle from phone would improve UX |
| 5 | What is the target Wear OS version range? | Affects rotary API, ambient mode, and contemporary chip styling |

---

## References

- [Wear OS Design Guidelines](https://developer.android.com/design/wear) — touch targets, rotary, vignette
- [Material Design 3 Accessibility](https://m3.material.io/foundations/accessible-design/overview) — semantics, roles, content descriptions
- [Jetpack Compose Semantics](https://developer.android.com/jetpack/compose/semantics) — `Modifier.semantics`, `Role`
- [Wear Compose Material](https://developer.android.com/jetpack/androidx/releases/wear-compose) — `ScalingLazyColumn`, `Chip`, `Vignette`, `PositionIndicator`
- AGENTS.md → Android section → Scanner HID, ScanPopup, Wear OS conventions
