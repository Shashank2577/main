# Provider Removal Scope — Groq & Cerebras

_Generated: 2026-04-06_
_Source scan: `/Users/shashanksaxena/Documents/Personal/Code/openClaw-android/app/src/`_

---

## Summary

Removing Groq and Cerebras touches **8 files** across 4 packages. No test files exist (confirmed by scan). The two provider implementation files are deleted entirely; the remaining 6 files require targeted line-level edits.

---

## File-by-File Breakdown

### FILE 1: `llm/GroqProvider.kt` — DELETE ENTIRELY

**Action:** Delete the file.

**Content summary:**
- `class GroqProvider(apiKeyProvider: () -> String) : LlmProvider`
- `providerId = "groq"`, `displayName = "Groq"`
- Hits `https://api.groq.com/openai/v1/chat/completions`
- Also contains shared OpenAI-compatible utilities (`// Shared utilities for OpenAI-compatible APIs (Groq, Cerebras, etc.)`)
- Those shared utilities are also used by `CerebrasProvider.kt`

**Risk:** MEDIUM — shared utilities. Verify `CerebrasProvider.kt` does not import from this file before deleting. If it does, the utilities are deleted along with Groq (which is correct — Cerebras is also being removed).

**Required changes:** None — full delete.

---

### FILE 2: `llm/CerebrasProvider.kt` — DELETE ENTIRELY

**Action:** Delete the file.

**Content summary:**
- `class CerebrasProvider(apiKeyProvider: () -> String) : LlmProvider`
- `providerId = "cerebras"`, `displayName = "Cerebras"`
- Hits `https://api.cerebras.ai/v1/chat/completions`

**Risk:** LOW — standalone file, no dependents once `OpenClawApp.kt` is updated.

**Required changes:** None — full delete.

---

### FILE 3: `OpenClawApp.kt` — MODIFY

**Lines with references:**

```kotlin
// Line ~126-128 (providers map construction)
"groq" to GroqProvider(apiKeyProvider = { settings.getGroqKey() }),
"cerebras" to CerebrasProvider(apiKeyProvider = { settings.getCerebrasKey() }),
```

**Required changes:**
1. Remove the `"groq"` and `"cerebras"` entries from the `providers` map.
2. Remove `import com.openclaw.android.llm.GroqProvider` (or wildcard import — verify).
3. Remove `import com.openclaw.android.llm.CerebrasProvider` (or wildcard import — verify).

**Risk:** LOW — mechanical deletion. The `providers` map will still have `"gemini"` and conditionally `"local-llama"`.

**Post-change providers map:**
```kotlin
val providers = mutableMapOf<String, LlmProvider>(
    "gemini" to GeminiProvider(apiKeyProvider = { settings.getGeminiKey() }),
)
```

---

### FILE 4: `llm/ModelRouter.kt` — MODIFY

**Lines with references:**

```kotlin
// Line 132 — findFastTextModel()
for (id in listOf("groq", "cerebras", "gemini")) {

// Line 144 — findCloudTextModel()
for (id in listOf("groq", "cerebras", "gemini")) {

// Line 129 — kdoc comment
/** Find a fast cloud text model, preferring Groq/Cerebras for speed. */
```

**Required changes:**
1. `findFastTextModel()`: change list to `listOf("gemini")` (groq/cerebras removed; gemini is only cloud option).
2. `findCloudTextModel()`: change list to `listOf("gemini")`.
3. Update kdoc: remove "preferring Groq/Cerebras for speed" text.
4. Update `LlmProvider.kt` kdoc line 8: `"Unified interface for all LLM providers (Gemini, Groq, Cerebras)."` → `"Unified interface for all LLM providers (Gemini, on-device LiteRT)."`

**Risk:** LOW — string constants only. No type dependencies.

---

### FILE 5: `data/SettingsRepository.kt` — MODIFY

**Lines with references:**

```kotlin
// Constants
private const val KEY_CEREBRAS = "api_key_cerebras"
private const val KEY_GROQ = "api_key_groq"

// Methods
fun getCerebrasKey(): String = prefs.getString(KEY_CEREBRAS, "") ?: ""
fun getGroqKey(): String = prefs.getString(KEY_GROQ, "") ?: ""
fun setCerebrasKey(key: String) { prefs.edit().putString(KEY_CEREBRAS, key).apply() }
fun setGroqKey(key: String) { prefs.edit().putString(KEY_GROQ, key).apply() }

// In hasAnyProvider():
getCerebrasKey().isNotBlank()
getGroqKey().isNotBlank() ||
```

**Required changes:**
1. Remove `KEY_CEREBRAS` and `KEY_GROQ` constants.
2. Remove `getCerebrasKey()`, `getGroqKey()`, `setCerebrasKey()`, `setGroqKey()` methods.
3. Fix `hasAnyProvider()`: remove the Groq/Cerebras `isNotBlank()` conditions. Result should check only `getGeminiKey().isNotBlank()`.

**Risk:** MEDIUM — `hasAnyProvider()` is called from `MainActivity` to decide whether to show onboarding. After this change, a user with only a Groq/Cerebras key configured (and no Gemini key) would be re-shown onboarding. This is the desired behavior since those providers are no longer supported.

**Post-change `hasAnyProvider()`:**
```kotlin
fun hasAnyProvider(): Boolean = getGeminiKey().isNotBlank()
    || getLocalModelEnabled()
```

---

### FILE 6: `data/PrivacyAudit.kt` — MODIFY

**Lines with references:**

```kotlin
// Inside provider when-expression (exact line TBD, pattern):
"cerebras" -> "api.cerebras.ai (Cerebras)"
"groq" -> "api.groq.com (Groq)"
```

**Required changes:**
1. Remove the `"cerebras"` branch.
2. Remove the `"groq"` branch.
3. Ensure the `else ->` or default case still handles unknown providers gracefully.

**Risk:** LOW — display-only audit output. No functional impact.

---

### FILE 7: `ui/screens/OnboardingScreen.kt` — MODIFY

**Lines with references (12 distinct references):**

```kotlin
// State
var cerebrasKey by remember { mutableStateOf(settings.getCerebrasKey()) }
var groqKey by remember { mutableStateOf(settings.getGroqKey()) }

// Provider list entries
"groq",
"Groq",
"https://console.groq.com/keys",

"cerebras",
"Cerebras",
"https://cloud.cerebras.ai/",

// Key dispatch in when-expression
"groq" -> groqKey
"cerebras" -> cerebrasKey

// Save callbacks
"groq" -> {
    groqKey = key
    settings.setGroqKey(key)
}
"cerebras" -> {
    cerebrasKey = key
    settings.setCerebrasKey(key)
}

// Model list URL dispatch
"groq" -> "https://api.groq.com/openai/v1/models"
"cerebras" -> "https://api.cerebras.ai/v1/models"
```

**Required changes:**
1. Remove `cerebrasKey` and `groqKey` state variables.
2. Remove Groq and Cerebras entries from the provider configuration list (IDs, display names, URLs).
3. Remove their `when` branches in key dispatch, save callbacks, and model list URL dispatch.
4. Ensure the provider list now contains only Gemini (and local model if present).

**Risk:** MEDIUM — the most touch-heavy single file. Must be careful not to break the surrounding loop/list structure when removing entries.

---

### FILE 8: `ui/screens/SettingsScreen.kt` — MODIFY

**Lines with references (8 distinct references):**

```kotlin
// State
var groqKey by remember { mutableStateOf(settings.getGroqKey()) }
var cerebrasKey by remember { mutableStateOf(settings.getCerebrasKey()) }

// ApiKeyField composables
ApiKeyField(
    label = "Groq API Key",
    value = groqKey,
    onValueChange = { groqKey = it },
    onSave = { settings.setGroqKey(it) },
    getKeyUrl = "https://console.groq.com/keys",
)

ApiKeyField(
    label = "Cerebras API Key",
    value = cerebrasKey,
    onValueChange = { cerebrasKey = it },
    onSave = { settings.setCerebrasKey(it) },
    getKeyUrl = "https://cloud.cerebras.ai/",
)
```

**Required changes:**
1. Remove `groqKey` and `cerebrasKey` state variables.
2. Remove both `ApiKeyField` composable calls.

**Risk:** LOW — mechanical deletion of composable calls. Surrounding layout structure is unaffected.

---

## Risk Assessment Summary

| File | Action | Risk | Notes |
|------|--------|------|-------|
| `GroqProvider.kt` | DELETE | MEDIUM | Contains shared OpenAI utilities — verify CerebrasProvider dependency before deleting |
| `CerebrasProvider.kt` | DELETE | LOW | Standalone |
| `OpenClawApp.kt` | MODIFY | LOW | Remove 2 map entries + imports |
| `ModelRouter.kt` | MODIFY | LOW | String list changes only |
| `SettingsRepository.kt` | MODIFY | MEDIUM | `hasAnyProvider()` semantics change affects onboarding gate |
| `PrivacyAudit.kt` | MODIFY | LOW | Display-only audit |
| `OnboardingScreen.kt` | MODIFY | MEDIUM | Most complex edit; 12 references spread across state + loops |
| `SettingsScreen.kt` | MODIFY | LOW | Remove 2 composable blocks + 2 state vars |

**Recommended execution order:**
1. Delete `GroqProvider.kt` and `CerebrasProvider.kt` first.
2. Modify `OpenClawApp.kt` (removes compile dependency on deleted files).
3. Modify `SettingsRepository.kt` (removes API surface used by UI screens).
4. Modify `ModelRouter.kt` (pure string changes, no dependency on above).
5. Modify `PrivacyAudit.kt` (pure string changes).
6. Modify `SettingsScreen.kt` (now safe — calls to removed SettingsRepository methods are gone).
7. Modify `OnboardingScreen.kt` (most complex, do last for stability).
8. Run `./gradlew compileDebugKotlin` to verify zero errors.

---

## Verification Checklist

After completing all changes, confirm:

- [ ] `grep -r "GroqProvider\|CerebrasProvider" app/src/` returns zero results
- [ ] `grep -r "getGroqKey\|getCerebrasKey\|setGroqKey\|setCerebrasKey" app/src/` returns zero results
- [ ] `grep -r '"groq"\|"cerebras"' app/src/main/java/` returns zero results (excluding comments)
- [ ] `./gradlew compileDebugKotlin` exits with BUILD SUCCESSFUL
- [ ] App launches to chat screen on emulator/device
- [ ] Settings screen shows no Groq or Cerebras fields
- [ ] Onboarding skips Groq/Cerebras steps
