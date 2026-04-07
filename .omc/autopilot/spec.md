# OpenClaw × Google AI Edge Gallery — Technical Specification

_Generated: 2026-04-06 | Status: Approved for execution_

---

## 1. Executive Summary

OpenClaw-Android is being re-platformed from a 17-screen multi-purpose AI assistant into a **single unified chat interface** modelled on the Google AI Edge Gallery app. The new app keeps all existing tool capabilities but surfaces them exclusively through the chat canvas, quick-action chips, and a minimal navigation drawer. Groq and Cerebras cloud providers are removed; Gemini and the on-device LiteRT provider become the only two inference backends.

The result is a dramatically simpler UX (one primary screen instead of seventeen) with a smaller binary surface, easier onboarding, and a direct mapping to Gallery's proven model-centric design language.

---

## 2. Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│  MainActivity                                                 │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  AppScreen state machine: SPLASH → ONBOARDING → MAIN   │ │
│  └─────────────────────────────────────────────────────────┘ │
│                         │                                     │
│              ┌──────────▼──────────────┐                     │
│              │   MainContent           │                     │
│              │  ModalNavigationDrawer  │                     │
│              │  ┌──────────────────┐   │                     │
│              │  │  DrawerSheet     │   │                     │
│              │  │  - Space list    │   │                     │
│              │  │  - Conv history  │   │                     │
│              │  │  - Settings link │   │                     │
│              │  └──────────────────┘   │                     │
│              │                         │                     │
│              │  ┌──────────────────┐   │                     │
│              │  │  ChatScreen      │   │ ← PRIMARY SCREEN   │
│              │  │  ┌────────────┐  │   │                     │
│              │  │  │ TopBar     │  │   │                     │
│              │  │  │ ModelChip  │  │   │                     │
│              │  │  └────────────┘  │   │                     │
│              │  │  ┌────────────┐  │   │                     │
│              │  │  │ MessageList│  │   │                     │
│              │  │  └────────────┘  │   │                     │
│              │  │  ┌────────────┐  │   │                     │
│              │  │  │ QuickChips │  │   │                     │
│              │  │  └────────────┘  │   │                     │
│              │  │  ┌────────────┐  │   │                     │
│              │  │  │ InputBar   │  │   │                     │
│              │  │  │ +Voice +Img│  │   │                     │
│              │  │  └────────────┘  │   │                     │
│              │  └──────────────────┘   │                     │
│              │                         │                     │
│              │  Secondary screens      │                     │
│              │  (Settings, Files,      │                     │
│              │   VoiceFull, Memory)    │                     │
│              └─────────────────────────┘                     │
└──────────────────────────────────────────────────────────────┘

Data layer (unchanged):
  Room DB: conversations, messages, memories
  SpaceManager: file-system spaces (JSON + dirs)
  SettingsRepository: SharedPreferences
  AgentRuntime ← ModelRouter ← LlmProvider (Gemini | LiteRT)
  ToolRegistry: 30+ tools
```

---

## 3. Resolved Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Keep Groq/Cerebras or drop? | **Drop both.** Only Gemini + LiteRT. Reduces API surface, API key management, and dependency count. |
| 2 | How many screens remain? | **4 functional screens**: Chat (primary), Settings, FileBrowser, VoiceConversation. Memory and Onboarding stay as-is. |
| 3 | Does Dashboard survive? | **No.** Removed. All Dashboard quick-links become quick-action chips in the chat input area. |
| 4 | What happens to Tasks/Notes/Calendar/etc.? | **Removed as standalone screens.** Their tools remain registered and accessible via natural language or chips. |
| 5 | How is model selection surfaced? | **Pill chip in ChatScreen TopBar** (Gallery's `ModelPickerChip` pattern) — tapping opens a `ModalBottomSheet` listing Gemini and local models. |
| 6 | Voice input: dedicated screen or inline? | **Both.** Inline mic button in InputBar for short voice input. Full VoiceConversationScreen remains accessible via chip/drawer for hands-free sessions. |
| 7 | Spaces/multi-workspace: keep? | **Keep.** Space selection moves into the drawer (replaces the old DashboardScreen navigation hub). |
| 8 | Navigation pattern: tab bar or drawer? | **Drawer only.** No bottom nav bar. `ModalNavigationDrawer` is already implemented and maps directly to Gallery's drawer. |
| 9 | Onboarding: still needed? | **Yes, but simplified.** Remove Groq/Cerebras API key steps; keep Gemini key + local model setup. |
| 10 | Discover screen: keep? | **No.** Feature discovery moves to quick-action chips and a "What can I do?" help prompt. |

---

## 4. Data Schema

The existing Room schema is retained unchanged. No new tables are needed.

```sql
-- conversations (v2, no change)
CREATE TABLE conversations (
    id          TEXT PRIMARY KEY,
    title       TEXT NOT NULL,
    spaceId     TEXT,                          -- NULL = General workspace
    createdAt   INTEGER NOT NULL,
    updatedAt   INTEGER NOT NULL,
    messageCount INTEGER NOT NULL DEFAULT 0,
    isActive    INTEGER NOT NULL DEFAULT 0     -- boolean
);

-- messages (v2, no change)
CREATE TABLE messages (
    id             TEXT PRIMARY KEY,
    conversationId TEXT NOT NULL
        REFERENCES conversations(id) ON DELETE CASCADE,
    role           TEXT NOT NULL,              -- user | assistant | tool
    contentJson    TEXT NOT NULL,              -- JSON: List<ContentPart>
    toolCallId     TEXT,
    toolCallsJson  TEXT,                       -- JSON: List<ToolCallRequest>
    timestamp      INTEGER NOT NULL,
    orderIndex     INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_messages_conv ON messages(conversationId);

-- memories (v2, no change)
CREATE TABLE memories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category    TEXT NOT NULL,
    key         TEXT NOT NULL,
    value       TEXT NOT NULL,
    source      TEXT NOT NULL DEFAULT '',
    createdAt   INTEGER NOT NULL,
    updatedAt   INTEGER NOT NULL,
    accessCount INTEGER NOT NULL DEFAULT 0
);
```

**SpaceManager** continues to use the JSON+directory layout on external storage (`spaces/spaces.json` + `spaces/{id}/files|conversations|knowledge`).

**PerChatSettings** (new, in-memory only for MVP): stored in `AgentRuntime` as a mutable field; persisted per-conversation via `SettingsRepository` using the conversation ID as a key prefix.

---

## 5. Component Responsibilities

| Component | Package | Responsibility | Keep/Change/Remove |
|-----------|---------|---------------|-------------------|
| `MainActivity` | root | Theme + screen state machine | KEEP, minor |
| `MainContent` | root | Drawer + single-tab navigation | CHANGE: remove tab index 0-2, 4, 6-16 |
| `ChatScreen` | ui.screens | Primary chat UI | CHANGE: add model chip, quick-action chips, per-chat settings |
| `VoiceConversationScreen` | ui.screens | Full-screen voice mode | KEEP as-is |
| `SettingsScreen` | ui.screens | App settings | CHANGE: remove Groq/Cerebras key fields |
| `FileBrowserScreen` | ui.screens | Sandboxed file browser | KEEP as-is |
| `OnboardingScreen` | ui.screens | First-run API key setup | CHANGE: remove Groq/Cerebras steps |
| `MemoryScreen` | ui.screens | View/manage AI memories | KEEP, accessible via drawer |
| `DashboardScreen` | ui.screens | Navigation hub | **REMOVE** |
| `TasksScreen` | ui.screens | Task list UI | **REMOVE** |
| `NotesScreen` | ui.screens | Notes UI | **REMOVE** |
| `DiscoverScreen` | ui.screens | Feature gallery | **REMOVE** |
| `CalendarScreen` | ui.screens | Calendar view | **REMOVE** |
| `TravelScreen` | ui.screens | Travel planner | **REMOVE** |
| `InsightsScreen` | ui.screens | Productivity charts | **REMOVE** |
| `BriefingScreen` | ui.screens | Daily briefing | **REMOVE** |
| `TeamScreen` | ui.screens | Team members | **REMOVE** |
| `HabitsScreen` | ui.screens | Habit tracking | **REMOVE** |
| `DecisionScreen` | ui.screens | Decision log | **REMOVE** |
| `RemindersScreen` | ui.screens | Reminders | **REMOVE** |
| `SplashScreen` | root | App entry animation | KEEP |
| `ConversationHistoryPanel` | ui.screens | Drawer conversation list | KEEP, used from drawer |
| `ModelPickerChip` | ui.components | Model selection pill | **CREATE** (adapted from Gallery) |
| `QuickActionChips` | ui.components | Horizontal chip strip | **CREATE** |
| `PerChatSettingsSheet` | ui.components | Bottom sheet for per-chat params | **CREATE** |
| `GeminiProvider` | llm | Gemini cloud inference | KEEP |
| `LiteRTProvider` | llm | On-device inference | KEEP |
| `GroqProvider` | llm | Groq cloud inference | **REMOVE** |
| `CerebrasProvider` | llm | Cerebras cloud inference | **REMOVE** |
| `ModelRouter` | llm | Provider/model selection | CHANGE: remove groq/cerebras references |
| `SettingsRepository` | data | SharedPreferences facade | CHANGE: remove Groq/Cerebras key methods |
| `PrivacyAudit` | data | Network destination audit | CHANGE: remove groq/cerebras cases |
| `OpenClawApp` | root | Application; DI root | CHANGE: remove provider registrations |
| All tools (30+) | tools | Tool implementations | KEEP unchanged |

---

## 6. API Contracts

### 6.1 LlmProvider Interface (unchanged)

```kotlin
interface LlmProvider {
    val providerId: String          // "gemini" | "local-llama"
    val displayName: String
    val availableModels: List<ModelInfo>
    val supportsVision: Boolean
    val supportsToolUse: Boolean

    suspend fun chatCompletion(
        request: ChatRequest,
        onChunk: (String) -> Unit,
        onToolCall: (ToolCallRequest) -> Unit,
        onDone: (ChatResponse) -> Unit,
        onError: (Exception) -> Unit,
    )

    fun isConfigured(): Boolean
}
```

### 6.2 Tool Interface (unchanged)

```kotlin
interface Tool {
    val name: String
    val description: String
    val parameterSchema: JsonObject

    suspend fun execute(
        arguments: JsonElement,
        context: ToolContext,
    ): ToolResult
}
```

### 6.3 SpaceDao / ConversationDao (unchanged Room DAOs)

Key query signatures remain intact. See `ConversationDao.kt` for full contract.

### 6.4 PerChatSettingsDao (new — in-memory for MVP)

```kotlin
data class PerChatSettings(
    val conversationId: String,
    val preferredModelId: String? = null,      // null = auto-select
    val temperature: Double = 0.7,
    val maxTokens: Int = 4096,
    val enabledTools: List<String>? = null,    // null = all tools
    val systemPromptOverride: String? = null,  // null = global prompt
)

// Stored in SettingsRepository under key "per_chat_{conversationId}"
// Serialized as JSON via kotlinx.serialization
```

### 6.5 QuickActionChip Contract

```kotlin
data class QuickActionChip(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val prompt: String,           // injected into input or sent directly
    val autoSend: Boolean = false // true = send immediately without user edit
)

// Predefined chips (Phase 1):
// briefing, tasks, notes, calendar, web_search, voice, files, memory
```

---

## 7. Integration Points with Existing Codebase

| Integration Point | Current State | Required Change |
|------------------|--------------|----------------|
| `MainContent` tab index 0-16 | All 17 tabs wired | Remove all except tab 3 (Chat), 5 (Settings), 11 (Files), 12 (Voice), 15 (Memory) |
| `ModelRouter.findFastTextModel()` | Prefers groq → cerebras → gemini | Change preference order to: gemini → local-llama |
| `ModelRouter.findCloudTextModel()` | Uses groq/cerebras/gemini list | Remove groq/cerebras from lists |
| `OpenClawApp.providers` map | Includes groq + cerebras | Remove both entries |
| `SettingsRepository` | `getGroqKey()`, `getCerebrasKey()`, etc. | Remove 6 methods + 2 constants |
| `PrivacyAudit` | groq + cerebras cases in `when` block | Remove both cases |
| `OnboardingScreen` | Shows API key steps for groq + cerebras | Remove those steps + state variables |
| `SettingsScreen` | `ApiKeyField` rows for groq + cerebras | Remove both `ApiKeyField` composables |
| `ChatScreen` | No model picker, no quick chips | Add `ModelPickerChip` to TopBar; add `QuickActionChips` row above input |
| `ConversationHistoryPanel` | Exists, used in drawer | Add Space switcher above conversation list |

---

## 8. Feature Matrix

| Feature | MVP (Phase 1) | Phase 2+ |
|---------|--------------|----------|
| Single-screen chat (remove 13 screens) | YES | — |
| Remove Groq provider | YES | — |
| Remove Cerebras provider | YES | — |
| Model picker chip in TopBar | YES | — |
| Quick-action chips (8 predefined) | YES | User-customizable |
| Space switcher in drawer | YES | — |
| Simplified onboarding (Gemini + local only) | YES | — |
| PerChatSettings (bottom sheet) | YES (basic: model + temp) | Full tool toggles, sys prompt override |
| Full VoiceConversation screen | YES (keep as-is) | Inline streaming response |
| Memory screen (accessible via drawer) | YES (keep as-is) | — |
| Files screen (accessible via drawer) | YES (keep as-is) | — |
| Gallery-style animation transitions | Phase 2 | EaseOutExpo slide animations |
| Benchmark screen | NO | Phase 3 |
| Custom task plugins (Gallery pattern) | NO | Phase 3 |

---

## 9. Breaking Changes to Navigation/UI

1. **Tab index system removed.** `MainContent` no longer uses `var currentTab by remember { mutableIntStateOf(3) }`. Navigation becomes `var currentScreen by remember { mutableStateOf(MainScreen.CHAT) }` with a sealed class of 5 variants.

2. **DashboardScreen entirely deleted.** Any deep links or back-navigation that targeted `currentTab = 0` must be redirected to `MainScreen.CHAT`.

3. **DiscoverScreen deleted.** The `onNavigateToDiscover` callback chain is removed throughout.

4. **Screens 1, 2, 4, 6-10, 13, 14, 16 deleted.** Their `onNavigateToX` callbacks are removed from `ChatScreen`, `DashboardScreen` (deleted), and `DiscoverScreen` (deleted).

5. **Settings screen loses Groq/Cerebras sections.** Users who had those keys stored will silently lose them on upgrade (keys are in SharedPrefs and will remain inert but harmless).

6. **Onboarding flow shortened.** Steps count drops from 3 providers to 2 (Gemini, local model). Users who skipped Groq/Cerebras setup will see no difference; those who completed them will not be re-prompted.

---

## 10. Implementation Risks and Mitigations

| # | Risk | Severity | Likelihood | Mitigation |
|---|------|----------|-----------|------------|
| 1 | Deleting 13 screens orphans navigation callbacks | HIGH | HIGH | Audit all `onNavigateTo*` lambdas before deletion; grep for each screen's usage |
| 2 | Users lose Groq/Cerebras API keys silently | MEDIUM | HIGH | Add one-time migration notice in Settings ("Groq/Cerebras no longer supported"); keys remain in SharedPrefs but unused |
| 3 | `ModelRouter` fallback logic breaks when groq/cerebras removed | HIGH | MEDIUM | Rewrite fallback chain to gemini-first, test with only local model configured |
| 4 | `PrivacyAudit` silently swallows unknown provider IDs | LOW | LOW | Add an `else ->` fallback case that logs unknown provider |
| 5 | Removing screens causes dangling imports / compilation errors | MEDIUM | HIGH | Remove files incrementally; run `./gradlew compileDebugKotlin` after each batch |
| 6 | QuickActionChips overlap with input bar on small screens | MEDIUM | MEDIUM | Make chip strip horizontally scrollable (`LazyRow`); collapse when keyboard is open |
| 7 | PerChatSettings JSON in SharedPrefs grows unbounded | LOW | LOW | Purge stale entries when conversation is deleted (hook into `deleteConversation`) |
| 8 | Gallery's `ModelPickerChip` uses Hilt ViewModels — OpenClaw has no Hilt | HIGH | HIGH | Adapt the UI pattern only; do not copy Gallery's ViewModel dependencies. Wire to existing `ModelRouter` directly |
| 9 | VoiceConversationScreen loses its navigation trigger | MEDIUM | MEDIUM | Retain as a named screen; add chip + drawer entry point |
| 10 | LiteRT provider not configured on first run — ModelRouter returns null | HIGH | MEDIUM | Guard `selectBestModel` to return Gemini as default if no model configured; show onboarding nudge |

---

## 11. Metrics for Success

1. Screen count: 17 → 4 navigable destinations
2. Build passes with zero compilation errors after provider removal
3. All existing tools remain accessible via natural language in ChatScreen
4. Model picker shows at least Gemini models when key is configured
5. Onboarding completes without Groq/Cerebras steps
6. Quick-action chips are visible and functional for: briefing, tasks, notes, calendar, web search, voice, files, memory
7. Drawer shows conversation history + space switcher
8. Settings screen has no Groq/Cerebras API key fields
9. PrivacyAudit no longer references Groq or Cerebras endpoints
10. Memory and Files remain accessible (via drawer links)
11. VoiceConversationScreen still launchable from chip

---

## 12. Acceptance Criteria Mapping

| AC | Requirement | Implementation Target |
|----|------------|----------------------|
| AC-01 | App launches to chat screen | `MainContent` starts at `MainScreen.CHAT` |
| AC-02 | No bottom navigation bar | `ModalNavigationDrawer` only, no `BottomNavigation` composable |
| AC-03 | Groq removed from provider map | `OpenClawApp.providers` has no "groq" key |
| AC-04 | Cerebras removed from provider map | `OpenClawApp.providers` has no "cerebras" key |
| AC-05 | Model picker chip visible in ChatScreen TopBar | `ModelPickerChip` composable renders in TopBar row |
| AC-06 | Quick-action chips render above input | `QuickActionChips` composable in `LazyRow` above `InputBar` |
| AC-07 | Drawer shows conversation history | `ConversationHistoryPanel` inside `ModalDrawerSheet` |
| AC-08 | Drawer shows space switcher | New `SpaceSwitcher` composable above conversation list |
| AC-09 | Settings has no Groq key field | `SettingsScreen` compiles without `groqKey` state |
| AC-10 | Settings has no Cerebras key field | `SettingsScreen` compiles without `cerebrasKey` state |
| AC-11 | 13 deleted screen files do not exist | `git status` shows those files as deleted |
| AC-12 | All 30+ tools still registered | `OpenClawApp.toolRegistry` registration block unchanged |
| AC-13 | Build passes with no warnings about unused imports | `./gradlew compileDebugKotlin` exits 0 |

---

## 13. File Inventory

### CREATE (new files)

```
app/src/main/java/com/openclaw/android/ui/components/ModelPickerChip.kt
app/src/main/java/com/openclaw/android/ui/components/QuickActionChips.kt
app/src/main/java/com/openclaw/android/ui/components/PerChatSettingsSheet.kt
app/src/main/java/com/openclaw/android/ui/components/SpaceSwitcher.kt
```

### MODIFY (existing files with targeted changes)

```
app/src/main/java/com/openclaw/android/MainActivity.kt
  - Remove tab indices 0,1,2,4,6,7,8,9,10,13,14,16
  - Replace currentTab Int with MainScreen sealed class
  - Remove onNavigateTo* lambdas for removed screens

app/src/main/java/com/openclaw/android/OpenClawApp.kt
  - Remove "groq" to GroqProvider(...) entry
  - Remove "cerebras" to CerebrasProvider(...) entry
  - Remove import of GroqProvider, CerebrasProvider

app/src/main/java/com/openclaw/android/llm/ModelRouter.kt
  - Remove "groq", "cerebras" from findFastTextModel() list
  - Remove "groq", "cerebras" from findCloudTextModel() list
  - Update kdoc comments

app/src/main/java/com/openclaw/android/data/SettingsRepository.kt
  - Remove KEY_GROQ, KEY_CEREBRAS constants
  - Remove getGroqKey(), setGroqKey(), getCerebrasKey(), setCerebrasKey()
  - Update hasAnyProvider() to check only Gemini key

app/src/main/java/com/openclaw/android/data/PrivacyAudit.kt
  - Remove "groq" and "cerebras" cases from provider when-expression

app/src/main/java/com/openclaw/android/ui/screens/OnboardingScreen.kt
  - Remove groqKey / cerebrasKey state variables
  - Remove Groq/Cerebras API key steps from provider list
  - Remove Groq/Cerebras URL constants

app/src/main/java/com/openclaw/android/ui/screens/SettingsScreen.kt
  - Remove groqKey / cerebrasKey state variables
  - Remove two ApiKeyField composable calls for Groq and Cerebras

app/src/main/java/com/openclaw/android/ui/screens/ChatScreen.kt
  - Add ModelPickerChip to TopBar
  - Add QuickActionChips row above InputBar
  - Wire chip actions to existing tool/navigation callbacks

app/src/main/java/com/openclaw/android/llm/LlmProvider.kt
  - Update kdoc: remove "(Gemini, Groq, Cerebras)" reference
```

### REMOVE (delete entirely)

```
app/src/main/java/com/openclaw/android/llm/GroqProvider.kt
app/src/main/java/com/openclaw/android/llm/CerebrasProvider.kt
app/src/main/java/com/openclaw/android/ui/screens/DashboardScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/TasksScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/NotesScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/DiscoverScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/CalendarScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/TravelScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/InsightsScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/BriefingScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/TeamScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/HabitsScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/DecisionScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/RemindersScreen.kt
```

### KEEP (no changes required)

```
app/src/main/java/com/openclaw/android/ui/screens/ChatScreen.kt         (modified above)
app/src/main/java/com/openclaw/android/ui/screens/VoiceConversationScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/MemoryScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/FileBrowserScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/FileViewerScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/SettingsScreen.kt     (modified above)
app/src/main/java/com/openclaw/android/ui/screens/OnboardingScreen.kt   (modified above)
app/src/main/java/com/openclaw/android/ui/screens/ConversationHistoryScreen.kt
app/src/main/java/com/openclaw/android/ui/screens/SplashScreen.kt
app/src/main/java/com/openclaw/android/agent/AgentRuntime.kt
app/src/main/java/com/openclaw/android/agent/ConversationManager.kt
app/src/main/java/com/openclaw/android/data/db/AppDatabase.kt
app/src/main/java/com/openclaw/android/data/db/ConversationDao.kt
app/src/main/java/com/openclaw/android/data/db/ConversationEntity.kt
app/src/main/java/com/openclaw/android/data/db/MessageEntity.kt
app/src/main/java/com/openclaw/android/data/db/MemoryDao.kt
app/src/main/java/com/openclaw/android/data/db/MemoryEntity.kt
app/src/main/java/com/openclaw/android/data/SpaceManager.kt
app/src/main/java/com/openclaw/android/data/MemorySystem.kt
app/src/main/java/com/openclaw/android/llm/GeminiProvider.kt
app/src/main/java/com/openclaw/android/llm/LiteRTProvider.kt
app/src/main/java/com/openclaw/android/llm/LiteRTBridge.kt
app/src/main/java/com/openclaw/android/llm/LlmProvider.kt              (minor kdoc update)
app/src/main/java/com/openclaw/android/llm/ModelDownloadManager.kt
app/src/main/java/com/openclaw/android/llm/InferenceLog.kt
app/src/main/java/com/openclaw/android/tools/*.kt                       (all 30+ tools)
app/src/main/java/com/openclaw/android/ui/components/ClayCard.kt
app/src/main/java/com/openclaw/android/ui/components/MarkdownText.kt
app/src/main/java/com/openclaw/android/ui/components/MediaPreview.kt
app/src/main/java/com/openclaw/android/ui/components/MessageBubble.kt
app/src/main/java/com/openclaw/android/ui/components/SuggestedPrompts.kt
app/src/main/java/com/openclaw/android/ui/components/VoiceInputButton.kt
app/src/main/java/com/openclaw/android/ui/theme/*.kt
app/src/main/java/com/openclaw/android/sandbox/SandboxedFileSystem.kt
app/src/main/java/com/openclaw/android/share/ShareReceiverActivity.kt
app/src/main/java/com/openclaw/android/PermissionManager.kt
app/src/main/java/com/openclaw/android/update/AppUpdater.kt
```
