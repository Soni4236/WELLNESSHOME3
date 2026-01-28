# WellnessHome App Audit (APP_AUDIT.md)

This document is a **deep audit** of the current Android app in this repo (`WELLNESSHOME`). It focuses on **UI flaws**, **integration flaws**, **functional flaws**, and **transition/navigation flaws**, and gives an actionable, prioritized plan to resolve them.

---

## Executive summary (high-signal)

### Critical issues (fix first)
- **Hardcoded LAN base URL + cleartext traffic enabled**: app will fail outside your network and is insecure by default.
  - Files: `app/src/main/java/com/simats/wellnesshome/api/ApiClient.kt`, `app/src/main/AndroidManifest.xml`
- **Notifications likely don’t work on Android 13+**: permission is checked at runtime, but **not declared** and there’s no request UX.
  - Files: `app/src/main/java/com/simats/wellnesshome/NotificationHelper.kt`, `app/src/main/AndroidManifest.xml`
- **Theme/app mode is inconsistent across screens**: `BaseActivity` applies theme/accessibility, but many Activities don’t extend it; `SplashActivity` forces light mode.
  - Files: `app/src/main/java/com/simats/wellnesshome/BaseActivity.kt`, `SplashActivity.kt`, many Activities
- **Gemini integration is not production-ready**: API key placeholder in code, no key management, no networking/timeout UX, and content policies not enforced.
  - File: `app/src/main/java/com/simats/wellnesshome/AiChatActivity.kt`

### Root causes (why many bugs happen)
- **Activity-per-screen with manual `startActivity()` everywhere** → inconsistent back stack, inconsistent transitions, duplicated wiring.
- **Hardcoded UI strings and hardcoded colors** → poor localization, accessibility, dark-mode incompatibility, visual inconsistency.
- **SharedPreferences scattered across many screens** → state drift, duplicated keys, hard-to-test logic, fragile behavior.
- **No single “session/auth gating” entry** → splash always goes onboarding, even for logged-in users.

---

## Project structure (what you currently have)

- Single module: `:app`
- UI: mostly **XML layouts + Activities**
- Networking: **Retrofit 2.9**, Gson
- Billing: **Play Billing 6.1.0**
- AI: `com.google.ai.client.generativeai:generativeai:0.1.2`

Key folder hotspots:
- `app/src/main/java/com/simats/wellnesshome/` (many Activities and receivers)
- `app/src/main/java/com/simats/wellnesshome/api/` (Retrofit setup + models)
- `app/src/main/res/layout/` (many screens; large amount of hardcoded text)
- `app/src/main/res/values/` (colors/theme are present but strings are not)

---

## Severity model used in this audit

- **P0 (Critical)**: security risk, app unusable in real conditions, or revenue-breaking
- **P1 (High)**: major UX/functional bugs, common crashes, data loss
- **P2 (Medium)**: inconsistent behavior, accessibility problems, tech debt that blocks improvements
- **P3 (Low)**: polish, minor refactors, non-blocking improvements

---

## A. Integration & security audit

### A1. P0 — Hardcoded LAN base URL + cleartext traffic
- **What’s wrong**
  - `ApiClient.BASE_URL` is hardcoded to a LAN IP: `http://10.254.84.81/wellness/`
  - `android:usesCleartextTraffic="true"` is set for the whole app
- **Why it’s a problem**
  - App breaks for users not on your LAN.
  - Cleartext HTTP allows MITM attacks; Play policies and user trust risks.
- **Fix plan**
  - **Move base URL to `buildConfigField`** with `debug` and `release` values.
  - For release: use **HTTPS** and disable cleartext.
  - Add a `network_security_config` for debug-only cleartext if you must keep HTTP during development.
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/api/ApiClient.kt`
  - `app/src/main/AndroidManifest.xml`

### A2. P0 — Notification permission flow missing (Android 13+)
- **What’s wrong**
  - `NotificationHelper.showNotification()` checks `POST_NOTIFICATIONS` for Android 13+, but:
    - the permission is **not declared** in `AndroidManifest.xml`
    - there is **no runtime permission request UX**
  - Result: notifications silently never show; user sees “nothing happens”.
- **Fix plan**
  - Add `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` (Android 13+ will use it).
  - Add a **permission request** screen/flow:
    - best place: `NotificationSettingsActivity` (when user enables reminders)
    - also consider a prompt on first launch after onboarding (optional).
  - Add UX feedback when permission is missing (e.g., “Enable notifications in Settings”).
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/NotificationHelper.kt`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/simats/wellnesshome/NotificationSettingsActivity.kt` (recommended)

### A3. P1 — Alarm scheduling reliability (Android 12+ and Doze)
- **What’s wrong**
  - Dashboard schedules alarms on every `onCreate()`:
    - habit reminder: one-shot after 24h
    - health reminders: repeating every 4h
    - mood reminders: repeating every 6h (first after 30s “testing”)
  - On modern Android:
    - repeating alarms may be inexact or deferred
    - scheduling on every open can cause reset timing
    - “testing after 30s” should not ship
- **Fix plan**
  - Introduce a `ReminderScheduler` that:
    - schedules once (idempotent) using stored “scheduledAt” in prefs
    - uses `setExactAndAllowWhileIdle` only when needed
  - Consider **WorkManager** for periodic tasks (recommended for health reminders).
  - Remove test 30s trigger in production builds.
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/DashboardActivity.kt`
  - Receivers: `HabitReminderReceiver.kt`, `HealthReminderReceiver.kt`, `MoodReminderReceiver.kt`

### A4. P1 — Gemini key management + safe usage
- **What’s wrong**
  - API key is hardcoded placeholder: `"YOUR_API_KEY_HERE"`
  - No build-time configuration, no secure storage, no environment separation
  - No loading/error UI besides a placeholder message bubble
  - Prompt is free-form; no safety guardrails or disclaimers for health advice
- **Fix plan**
  - Configure key via **`local.properties`/gradle** for debug and secure remote config for release (or server-side proxy).
  - Add clear UI states:
    - sending, typing, failure, retry, offline fallback
  - Add a **medical disclaimer** and safe-response rules (no diagnosis, emergency guidance).
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/AiChatActivity.kt`
  - `app/build.gradle.kts` (for buildConfigFields)

### A5. P2 — Billing flow correctness & UX
- **What’s wrong**
  - SKU is hardcoded; offer token selection uses first offer only.
  - No UI state when productDetails missing (besides toast).
  - “Skip” and success both navigate to `SignUpActivity` (method name says `navigateToMain()` but goes to signup).
  - No restore purchases path.
- **Fix plan**
  - Rename navigation to match behavior.
  - Add “Restore purchases” button or auto-check purchases.
  - Display product price/period from `ProductDetails`.
  - Handle multiple offers; choose best or configured.
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/SubscriptionActivity.kt`

---

## B. Navigation, transitions & flow audit

### B1. P0/P1 — Entry flow is incorrect (splash always goes to onboarding)
- **What’s wrong**
  - `SplashActivity` always navigates to `OnboardingActivity` after 2s.
  - It ignores:
    - `IS_LOGGED_IN`
    - onboarding completion
    - premium state
- **Fix plan**
  - Add routing rules:
    - If `IS_LOGGED_IN=true` → `DashboardActivity`
    - else if onboarding not completed → `OnboardingActivity`
    - else → `LoginActivity`
  - Store `ONBOARDING_DONE=true` when onboarding finishes.
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/SplashActivity.kt`
  - `app/src/main/java/com/simats/wellnesshome/OnboardingActivity.kt`
  - `LoginActivity.kt`

### B2. P1 — Back stack inconsistencies & finish() misuse
- **What’s wrong**
  - Many screens call `finish()` after `startActivity()` inconsistently (sometimes yes, sometimes no).
  - This creates confusing back navigation behavior (user “back” returns to unexpected places).
- **Fix plan**
  - Define a navigation contract:
    - Auth screens: use `FLAG_ACTIVITY_CLEAR_TOP | NEW_TASK` when entering Dashboard after login
    - Settings/Profile: do NOT finish the previous screen unless intentional
  - Centralize navigation helpers in one place:
    - e.g., `Navigator` or extension functions in `ui/navigation/`
- **Files**
  - Many: `LoginActivity.kt`, `SignUpActivity.kt`, `SubscriptionActivity.kt`, etc.

### B3. P2 — No consistent screen transitions
- **What’s wrong**
  - No `overridePendingTransition`, no shared element transitions, no consistent motion system.
  - Users perceive screens as “jumpy” and unrelated.
- **Fix plan**
  - Use Material motion:
    - `MaterialSharedAxis`, `MaterialFadeThrough`, `MaterialContainerTransform`
  - Apply via a shared base (`BaseActivity`) or theme overlays.
- **Files**
  - Global: theme + Activity base

---

## C. UI/UX audit (visual, layout, accessibility)

### C1. P0/P1 — No real `strings.xml` usage (hardcoded text everywhere)
- **What’s wrong**
  - `res/values/strings.xml` only has `app_name`
  - Layouts contain hundreds of `android:text="..."` literals
- **Impact**
  - No localization, hard to edit copy, inconsistent phrasing
  - Accessibility tools often rely on consistent string resources
- **Fix plan**
  - Create real `strings.xml` with groups:
    - auth, onboarding, dashboard, reminders, ai_chat, settings
  - Replace hardcoded text with `@string/...`
- **Files**
  - `app/src/main/res/layout/*` (many)
  - `app/src/main/res/values/strings.xml`

### C2. P1 — Dark mode is incomplete (no `values-night`, hardcoded colors)
- **What’s wrong**
  - No `res/values-night/` resources
  - Many hardcoded hex colors in layouts/drawables
  - Some Activities force specific theme types:
    - `MoodSuccessActivity` uses `Theme.AppCompat.Light.NoActionBar`
- **Impact**
  - Night mode looks broken or unreadable
  - “High Contrast” feature likely inconsistent across screens
- **Fix plan**
  - Add `res/values-night/colors.xml` and map semantic colors (`bg_main`, `text_main`, etc.)
  - Replace hardcoded hex with `@color/...` semantic tokens
  - Ensure all Activities share the same Material3 base theme
- **Files**
  - `app/src/main/res/values/colors.xml` (already has semantic aliases)
  - `app/src/main/res/layout/*` (remove hex values)
  - `app/src/main/res/values/themes.xml`
  - `app/src/main/AndroidManifest.xml` and individual Activity theme overrides

### C3. P1 — Accessibility gaps
- **What’s wrong**
  - Some `ImageView`s have `contentDescription`, many likely don’t.
  - Hardcoded small text sizes (11sp labels), low-contrast colors, and fixed heights (60dp inputs) can break large font sizes.
  - `BaseActivity` applies fontScale only if a pref is set, but many Activities don’t extend `BaseActivity`.
- **Fix plan**
  - Ensure all main screens extend `BaseActivity` or adopt a unified theme approach.
  - Run through layouts:
    - add missing `contentDescription`
    - ensure touch targets >= 48dp
    - ensure constraints work with `fontScale=1.3+`
  - Use Material components where possible (`TextInputLayout`, `MaterialButton`)
- **Files**
  - `BaseActivity.kt`
  - Layouts in `res/layout/`

### C4. P2 — Layout anti-patterns / maintainability
- **Observations**
  - Many screens use nested `LinearLayout` for grids; might be okay but becomes hard to maintain.
  - Some screens use `ScrollView` + `ConstraintLayout` with hardcoded top margins (can break on small devices).
- **Fix plan**
  - Prefer:
    - `RecyclerView` for repeated items
    - `ConstraintLayout` with guidelines for responsive spacing
    - dimension resources (`res/values/dimens.xml`) for consistent spacing

---

## D. Functional logic & data/state audit

### D1. P1 — SharedPreferences key sprawl
- **What’s wrong**
  - Many direct calls like `getSharedPreferences("UserPrefs", ...)` across Activities.
  - Keys are scattered (`IS_LOGGED_IN`, `USER_ID`, `COIN_BALANCE`, etc.)
- **Impact**
  - Typos create silent bugs; hard to change storage format; hard to test.
- **Fix plan**
  - Create a single `UserSessionStore` (or `PreferencesRepository`) that:
    - defines keys in one place
    - exposes typed getters/setters
  - Consider DataStore later (optional).
- **Files**
  - Many Activities + `BaseActivity.kt`

### D2. P1 — Dashboard mixes UI + network + scheduling
- **What’s wrong**
  - `DashboardActivity` handles:
    - UI wiring
    - Retrofit calls
    - coin caching
    - alarm scheduling
    - screen-time tracking init
  - This makes it fragile and hard to debug.
- **Fix plan**
  - Extract:
    - `DashboardRepository` (API + caching)
    - `ReminderScheduler`
    - `DashboardViewModel` (optional but recommended)
- **Files**
  - `DashboardActivity.kt`

### D3. P2 — Null-safety / crash risk
- **What’s wrong**
  - At least one `!!` usage found (`BreathingExerciseActivity.kt`): `handler.post(timerRunnable!!)`
- **Fix plan**
  - Remove `!!` and guard with null checks or initialize earlier.
- **Files**
  - `app/src/main/java/com/simats/wellnesshome/BreathingExerciseActivity.kt`

---

## E. Consistency & code quality audit

### E1. P1 — BaseActivity not used widely
- **What’s wrong**
  - `BaseActivity` configures theme + accessibility, but most Activities extend `AppCompatActivity`.
- **Fix plan**
  - Make all user-facing screens extend `BaseActivity` unless there’s a clear reason not to.
  - Or move theme/accessibility to an `Application` + theme-only approach (bigger refactor).
- **Files**
  - `BaseActivity.kt` + all Activities

### E2. P2 — Theme naming mismatch bug risk
- **What’s wrong**
  - Theme defined as `Theme.WELLNESSHOME.HighContrast` in XML, but `BaseActivity` uses `R.style.Theme_WELLNESSHOME_HighContrast`.
  - This *may still compile* depending on resource name normalization, but it’s fragile and confusing.
- **Fix plan**
  - Align style names and references consistently (prefer one naming convention).
- **Files**
  - `BaseActivity.kt`, `res/values/themes.xml`

---

## Recommended “Fix Roadmap” (do this in order)

### Phase 0 — Stabilize & secure (P0)
- [ ] Move API base URL to build configs; remove LAN IP from source
- [ ] Disable cleartext in release; add debug-only network config if needed
- [ ] Add and request `POST_NOTIFICATIONS`; add user-visible explanation if denied
- [ ] Fix splash routing logic (logged-in → dashboard, otherwise onboarding/login)
- [ ] Remove test-only alarm triggers (e.g., 30s mood reminder)

### Phase 1 — UX correctness + flow (P1)
- [ ] Standardize Activities to extend `BaseActivity`
- [ ] Create navigation helper (consistent flags, consistent finish behavior)
- [ ] Add consistent transitions (Material motion)
- [ ] Fix Billing UX: product details display, restore purchases, correct naming
- [ ] Improve AI Chat UX: loading/failure/retry, disclaimer, key management

### Phase 2 — UI cleanup (P1/P2)
- [ ] Replace hardcoded strings with `@string/...` resources
- [ ] Remove hardcoded hex colors; use semantic `@color/...`
- [ ] Add `values-night` to support dark mode properly
- [ ] Accessibility pass: content descriptions, touch targets, contrast, font scale

### Phase 3 — Architecture improvement (P2/P3)
- [ ] Consolidate SharedPreferences into a typed store
- [ ] Extract repositories/schedulers out of Activities
- [ ] Add tests for session routing and key flows (login → dashboard, reminders)

---

## “Where to look first” (fast debugging targets)

- **Networking**: `app/src/main/java/com/simats/wellnesshome/api/ApiClient.kt`
- **Launch flow**: `SplashActivity.kt`, `OnboardingActivity.kt`, `LoginActivity.kt`
- **Dashboard complexity**: `DashboardActivity.kt`
- **Notifications & reminders**:
  - `NotificationHelper.kt`
  - `MoodReminderReceiver.kt`, `HabitReminderReceiver.kt`, `HealthReminderReceiver.kt`
- **AI chat**: `AiChatActivity.kt`
- **Billing**: `SubscriptionActivity.kt`
- **Themes**: `res/values/themes.xml`, `res/values/colors.xml`
- **Hardcoded UI**: `res/layout/*.xml` (many strings + hex colors)

---

## Notes / assumptions

- This audit is based on the repository state inspected in `app/src/main`.
- Some improvements (WorkManager, ViewModels, DataStore, Navigation Component) are optional, but recommended if you want a professional-grade, maintainable app.

