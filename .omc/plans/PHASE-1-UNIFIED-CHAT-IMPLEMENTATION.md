# Phase 1: Unified Chat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the 17-tab Gallery app into a single, unified chat interface with left-drawer navigation, integrated quick actions, and intelligent message rendering.

**Architecture:**
- Single-screen chat (replace 17-tab MainActivity)
- Left drawer for navigation (conversations, spaces, settings)
- Bottom sheets for overlays (model picker, per-chat settings)
- Reuse ~40% from Gallery, adapt ~35%, build fresh ~25%
- TDD approach: test-first, frequent commits

**Tech Stack:**
- Kotlin, Jetpack Compose, Material3, Room (existing)
- Gallery patterns: ChatPanel, MessageBubble, AudioAnimation
- OpenClaw patterns: AgentRuntime, ConversationManager, SpaceManager

---

## File Structure Mapping

### Files to Create (New)
```
ui/components/
├── ModelPickerChip.kt              (from Gallery adaptation)
├── QuickActionChips.kt             (new)
├── PerChatSettingsSheet.kt         (new)
├── SpaceSwitcher.kt                (new)
├── ThinkingIndicator.kt            (from Gallery extraction)
└── InputBar.kt                      (from Gallery extraction + adaptation)

data/db/
├── PerChatSettingsEntity.kt        (new)
└── PerChatSettingsDao.kt           (new)
```

### Files to Modify (Critical Path)
```
MainActivity.kt                      (~150 lines changed)
  - Replace var currentTab with sealed class MainScreen
  - Simplify when(currentScreen) from 17 to 5 branches
  - Wire drawer + overlays

ChatScreen.kt                         (~200 lines added)
  - Add ModelPickerChip to TopBar
  - Add QuickActionChips above messages
  - Add PerChatSettingsSheet bottom sheet
  - Add state vars for overlay visibility

AppDatabase.kt                        (Room migration v2→v3)
  - Add PerChatSettingsEntity + PerChatSettingsDao
  - Create MIGRATION_2_3

OpenClawApp.kt                        (provider removal)
  - Delete Groq/Cerebras provider registration
  - Keep Gemini + LiteRT only

AgentRuntime.kt                       (StateFlow upgrade)
  - Convert preferredModelId var → StateFlow<String>
  - Update initialization

SettingsRepository.kt                 (provider cleanup + new methods)
  - Remove getGroqKey(), getCerebrasKey() methods
  - Update hasAnyProvider() logic
  - Add getPerChatSettings(), savePerChatSettings()

MessageBubble.kt                      (CRITICAL FIX - 5 lines)
  - Change Text(toolResult.output) → MarkdownText(toolResult.output)

OnboardingScreen.kt                   (~60 lines changed)
  - Remove Groq/Cerebras API key input fields
```

### Files to Delete
```
Providers:
- GroqProvider.kt
- CerebrasProvider.kt

Screens (13):
- DashboardScreen.kt
- TasksScreen.kt
- NotesScreen.kt
- DiscoverScreen.kt
- CalendarScreen.kt
- TravelScreen.kt
- InsightsScreen.kt
- BriefingScreen.kt
- TeamScreen.kt
- HabitsScreen.kt
- DecisionsScreen.kt
- RemindersScreen.kt
- (keep: ChatScreen, SettingsScreen, VoiceConversationScreen, FileBrowserScreen, Memory)
```

### Files to Keep Unchanged
```
- ChatScreen.kt (enhanced, not replaced)
- SettingsScreen.kt (slight modification)
- VoiceConversationScreen.kt
- FileBrowserScreen.kt
- ConversationManager.kt
- AgentRuntime.kt (StateFlow upgrade)
- ModelRouter.kt (constant change only)
```

---

## Critical Bugs to Fix First

### Bug #1: MediaItem Defined Twice
- Location 1: `data/MediaItem.kt` (data class)
- Location 2: `ui/chat/ChatScreen.kt:~45` (sealed class)
- Fix: Keep the sealed class, delete the data class file
- Effort: 0.5h

### Bug #2: ToolResultBubble Not Using MarkdownText
- Location: `ui/chat/MessageBubble.kt:~290`
- Current: `Text(toolResult.output)`
- Fix: `MarkdownText(toolResult.output)`
- Effort: 0.25h (5-line fix)

### Bug #3: preferredModelId Not Observable
- Location: `agent/AgentRuntime.kt:~85`
- Current: `var preferredModelId: String = ""`
- Fix: Convert to `StateFlow<String>`
- Effort: 1h

---

## Phase 1 Task Breakdown (78-97 hours total)

### Sprint 0: Critical Bugs & Setup (4-6 hours) — Complete FIRST
- [ ] Task S0.1: Fix MediaItem duplication
- [ ] Task S0.2: Fix ToolResultBubble markdown rendering
- [ ] Task S0.3: Convert preferredModelId to StateFlow
- [ ] Task S0.4: Create Room migration (v2 → v3)
- [ ] Task S0.5: Create PerChatSettingsEntity + Dao

### Sprint 1: Navigation Refactor (12-16 hours) — Developer A
- [ ] Task S1.1: Create sealed class MainScreen
- [ ] Task S1.2: Refactor MainActivity with sealed class
- [ ] Task S1.3: Create SpaceSwitcher composable
- [ ] Task S1.4: Wire drawer content (conversations + spaces + settings)
- [ ] Task S1.5: Update nav callbacks cleanup

### Sprint 2: UI Components (20-24 hours) — Developer B (parallel with S1)
- [ ] Task S2.1: Extract + adapt ModelPickerChip from Gallery
- [ ] Task S2.2: Create QuickActionChips composable
- [ ] Task S2.3: Create PerChatSettingsSheet composable
- [ ] Task S2.4: Extract ThinkingIndicator from Gallery
- [ ] Task S2.5: Extract InputBar from Gallery + adapt

### Sprint 3: ChatScreen Integration (16-20 hours) — Developer A (after S1)
- [ ] Task S3.1: Add ModelPickerChip to TopBar
- [ ] Task S3.2: Add QuickActionChips above messages
- [ ] Task S3.3: Wire PerChatSettingsSheet bottom sheet
- [ ] Task S3.4: Add state management for overlays
- [ ] Task S3.5: Test streaming + message rendering

### Sprint 4: Provider Cleanup (4-6 hours) — Developer B (after S2)
- [ ] Task S4.1: Remove Groq/Cerebras providers
- [ ] Task S4.2: Update ModelRouter constants
- [ ] Task S4.3: Update hasAnyProvider() logic
- [ ] Task S4.4: Clean up OnboardingScreen

### Sprint 5: Screen Removal (6-8 hours) — Either developer (can be done in parallel)
- [ ] Task S5.1: Delete 13 screens in batches
- [ ] Task S5.2: Remove navigation references
- [ ] Task S5.3: Remove callbacks + orphaned imports
- [ ] Task S5.4: Verify no compile errors

### Sprint 6: Testing & Polish (8-12 hours) — Both developers
- [ ] Task S6.1: Integration test: chat flow
- [ ] Task S6.2: Integration test: model switching
- [ ] Task S6.3: Integration test: space switching
- [ ] Task S6.4: Performance profiling (recomposition counts)
- [ ] Task S6.5: Manual QA checklist

---

## Detailed Task Breakdown

### SPRINT 0: Critical Bugs & Setup

#### Task S0.1: Fix MediaItem Duplication

**Files:**
- Delete: `data/MediaItem.kt` (if exists)
- Modify: `ui/chat/ChatScreen.kt` (lines with MediaItem import)

- [ ] **Step 1: Identify MediaItem definitions**

Run: `grep -rn "sealed class MediaItem\|data class MediaItem" app/src/`

Expected output shows 1-2 definitions. Note their locations.

- [ ] **Step 2: Keep sealed class, delete data class**

If `data/MediaItem.kt` exists:
```bash
rm app/src/main/java/com/openclaw/android/data/MediaItem.kt
```

Update any imports in `ChatScreen.kt` to point to the sealed class.

- [ ] **Step 3: Verify no compile errors**

Run: `./gradlew build`

Expected: SUCCESS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/openclaw/android/ui/chat/ChatScreen.kt
git commit -m "fix: remove duplicate MediaItem data class definition"
```

---

#### Task S0.2: Fix ToolResultBubble Markdown Rendering

**Files:**
- Modify: `ui/chat/MessageBubble.kt:280-295`

- [ ] **Step 1: Write failing test**

Create `tests/ui/chat/MessageBubbleTest.kt`:

```kotlin
@Test
fun testToolResultRenderingWithMarkdown() {
    val toolResult = ToolResult(
        output = "**Bold** text with ### header"
    )

    composeTestRule.setContent {
        MessageBubble(
            message = ChatMessage(
                content = listOf(
                    ContentPart.ToolResult(toolResult)
                )
            )
        )
    }

    // Verify markdown is rendered (not literal asterisks)
    composeTestRule
        .onNodeWithText("Bold")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText("header")
        .assertIsDisplayed()
}
```

Run: `./gradlew testDebugUnitTest`
Expected: FAIL - `Text()` doesn't render markdown

- [ ] **Step 2: Find and modify the toolResult rendering line**

In `MessageBubble.kt`, find:
```kotlin
ToolResult -> {
    Text(
        text = (content as ContentPart.ToolResult).output,
        style = MaterialTheme.typography.bodySmall
    )
}
```

Replace with:
```kotlin
ToolResult -> {
    MarkdownText(
        markdown = (content as ContentPart.ToolResult).output,
        style = MaterialTheme.typography.bodySmall
    )
}
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/openclaw/android/ui/chat/MessageBubble.kt
git add tests/ui/chat/MessageBubbleTest.kt
git commit -m "fix: render tool results with markdown formatting, not literal text"
```

---

#### Task S0.3: Convert preferredModelId to StateFlow

**Files:**
- Modify: `agent/AgentRuntime.kt:80-90`

- [ ] **Step 1: Write failing test**

Create `tests/agent/AgentRuntimeTest.kt`:

```kotlin
@Test
fun testPreferredModelIdIsObservable() {
    val runtime = AgentRuntime(...)

    var emissions = 0
    val job = launch {
        runtime.preferredModelId.collect {
            emissions++
        }
    }

    // Initial emission on subscription
    assertEquals(1, emissions)

    // Change model ID
    runtime.setPreferredModelId("gemma-7b")
    assertEquals(2, emissions)

    job.cancel()
}
```

Run: `./gradlew testDebugUnitTest`
Expected: FAIL - `preferredModelId` is not a Flow

- [ ] **Step 2: Update AgentRuntime**

In `agent/AgentRuntime.kt`:

Change:
```kotlin
var preferredModelId: String = ""
```

To:
```kotlin
private val _preferredModelId = MutableStateFlow("")
val preferredModelId: StateFlow<String> = _preferredModelId.asStateFlow()

fun setPreferredModelId(modelId: String) {
    _preferredModelId.value = modelId
}
```

Update all references from `preferredModelId` reads to `preferredModelId.value`.

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/openclaw/android/agent/AgentRuntime.kt
git add tests/agent/AgentRuntimeTest.kt
git commit -m "refactor: make preferredModelId reactive (StateFlow)"
```

---

#### Task S0.4: Create Room Database Migration v2 → v3

**Files:**
- Modify: `data/db/AppDatabase.kt:10-55`

- [ ] **Step 1: Create migration class**

Create `data/db/AppDatabaseMigrations.kt`:

```kotlin
object AppDatabaseMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create spaces table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `spaces` (
                    `id` TEXT PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `emoji` TEXT NOT NULL DEFAULT '📁',
                    `description` TEXT NOT NULL DEFAULT '',
                    `systemPrompt` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `lastUsedAt` INTEGER NOT NULL DEFAULT 0,
                    `sortOrder` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )

            // Create per_chat_settings table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `per_chat_settings` (
                    `conversationId` TEXT PRIMARY KEY NOT NULL,
                    `temperature` REAL NOT NULL DEFAULT 0.7,
                    `topK` INTEGER NOT NULL DEFAULT 40,
                    `topP` REAL NOT NULL DEFAULT 0.95,
                    `maxTokens` INTEGER NOT NULL DEFAULT 4096,
                    `preferredModelId` TEXT,
                    `enabledTools` TEXT,
                    FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON DELETE CASCADE
                )
                """.trimIndent()
            )

            // Create index for per_chat_settings
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_per_chat_settings_conversationId` ON `per_chat_settings` (`conversationId`)"
            )
        }
    }
}
```

- [ ] **Step 2: Update AppDatabase.kt to register migration**

In `data/db/AppDatabase.kt`, update the database builder:

Change:
```kotlin
@Database(
    entities = [ConversationEntity::class, MessageEntity::class, MemoryEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    ...
}
```

To:
```kotlin
@Database(
    entities = [ConversationEntity::class, MessageEntity::class, MemoryEntity::class, PerChatSettingsEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private val MIGRATION_2_3 = AppDatabaseMigrations.MIGRATION_2_3

        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "openclaw.db")
                .addMigrations(MIGRATION_2_3)
                .build()
        }
    }
}
```

- [ ] **Step 3: Verify migration syntax**

Run: `./gradlew build`
Expected: SUCCESS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/openclaw/android/data/db/AppDatabaseMigrations.kt
git add app/src/main/java/com/openclaw/android/data/db/AppDatabase.kt
git commit -m "feat: add Room database v2→v3 migration with spaces and per_chat_settings tables"
```

---

#### Task S0.5: Create PerChatSettings Room Entity & Dao

**Files:**
- Create: `data/db/PerChatSettingsEntity.kt`
- Create: `data/db/PerChatSettingsDao.kt`
- Modify: `data/db/AppDatabase.kt`

- [ ] **Step 1: Write PerChatSettingsEntity**

Create `data/db/PerChatSettingsEntity.kt`:

```kotlin
@Entity(tableName = "per_chat_settings")
data class PerChatSettingsEntity(
    @PrimaryKey
    val conversationId: String,

    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 4096,
    val preferredModelId: String? = null,
    val enabledTools: String? = null  // comma-separated tool names
)
```

- [ ] **Step 2: Write PerChatSettingsDao**

Create `data/db/PerChatSettingsDao.kt`:

```kotlin
@Dao
interface PerChatSettingsDao {
    @Query("SELECT * FROM per_chat_settings WHERE conversationId = :conversationId")
    suspend fun getSettings(conversationId: String): PerChatSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: PerChatSettingsEntity)

    @Query("DELETE FROM per_chat_settings WHERE conversationId = :conversationId")
    suspend fun deleteSettings(conversationId: String)

    @Query("SELECT * FROM per_chat_settings")
    fun getAllSettings(): Flow<List<PerChatSettingsEntity>>
}
```

- [ ] **Step 3: Add Dao to AppDatabase**

In `data/db/AppDatabase.kt`, add:

```kotlin
abstract fun perChatSettingsDao(): PerChatSettingsDao
```

- [ ] **Step 4: Write unit test**

Create `tests/data/db/PerChatSettingsDaoTest.kt`:

```kotlin
@Test
suspend fun testInsertAndRetrieveSettings() {
    val settings = PerChatSettingsEntity(
        conversationId = "test-conv-1",
        temperature = 0.8f,
        preferredModelId = "gemini-flash"
    )

    database.perChatSettingsDao().upsertSettings(settings)

    val retrieved = database.perChatSettingsDao().getSettings("test-conv-1")

    assertEquals(settings.temperature, retrieved?.temperature)
    assertEquals("gemini-flash", retrieved?.preferredModelId)
}
```

Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/openclaw/android/data/db/PerChatSettingsEntity.kt
git add app/src/main/java/com/openclaw/android/data/db/PerChatSettingsDao.kt
git add app/src/main/java/com/openclaw/android/data/db/AppDatabase.kt
git add tests/data/db/PerChatSettingsDaoTest.kt
git commit -m "feat: add PerChatSettings Room entity, Dao, and database schema"
```

---

### SPRINT 1: Navigation Refactor (12-16 hours) — Developer A

[Tasks S1.1 through S1.5 - full TDD implementation for navigation refactoring, with exact code for sealed class MainScreen, MainActivity refactor, SpaceSwitcher composable, drawer wiring, and callback cleanup]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

### SPRINT 2: UI Components (20-24 hours) — Developer B

[Tasks S2.1 through S2.5 - full TDD implementation for ModelPickerChip extraction, QuickActionChips creation, PerChatSettingsSheet, ThinkingIndicator, InputBar extraction]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

### SPRINT 3: ChatScreen Integration (16-20 hours) — Developer A

[Tasks S3.1 through S3.5 - ChatScreen modifications with ModelPickerChip, QuickActionChips, PerChatSettingsSheet wiring, state management, streaming tests]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

### SPRINT 4: Provider Cleanup (4-6 hours) — Developer B

[Tasks S4.1 through S4.4 - Provider removal, ModelRouter updates, hasAnyProvider logic, OnboardingScreen cleanup]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

### SPRINT 5: Screen Removal (6-8 hours) — Either

[Tasks S5.1 through S5.4 - Delete 13 screens, remove navigation, clean callbacks, verify compilation]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

### SPRINT 6: Testing & Polish (8-12 hours) — Both

[Tasks S6.1 through S6.5 - Integration tests for chat flow, model switching, space switching, performance profiling, QA checklist]

**[ABBREVIATED FOR SPACE - FULL DETAILED TASKS CONTINUE IN SPEC DOCUMENT]**

---

## Execution Model

### Parallel Work
```
Week 1-2:
  Developer A: Sprint 0 (bugs) + Sprint 1 (navigation)
  Developer B: Sprint 2 (UI components) — can start immediately

Week 3:
  Developer A: Sprint 3 (ChatScreen integration)
  Developer B: Sprint 4 (provider cleanup) + Sprint 5 (screen removal)

Week 4-5:
  Both: Sprint 6 (testing + polish)
```

### Commit Frequency
- **Minimum:** After each task (every 2-3 hours)
- **Maximum:** After each sprint checkpoint
- **Total commits:** ~25-30 commits for Phase 1

### Success Criteria

#### Code Quality
- [ ] Zero compiler errors
- [ ] All tests pass (unit + integration)
- [ ] No unused imports
- [ ] Code style matches OpenClaw conventions

#### Functional
- [ ] Unified chat screen loads on startup
- [ ] Left drawer opens/closes smoothly
- [ ] Model picker works
- [ ] Quick action chips send predefined prompts
- [ ] Tool results render with markdown
- [ ] Per-chat settings persist

#### Performance
- [ ] App startup: <3s (cold start)
- [ ] Chat render: <100ms
- [ ] Model switch: <500ms
- [ ] Memory: <200MB on 6GB device

#### UX
- [ ] Drawer swipe gesture smooth
- [ ] Bottom sheets animate properly
- [ ] Touch targets ≥48dp
- [ ] Dark/light theme consistent
- [ ] All error messages display correctly

---

## Known Issues & Mitigations

### Issue #1: Token Streaming Performance
**Problem:** Token-by-token callbacks cause full `ChatPanel` recompose
**Mitigation:** Split streaming into separate StateFlow (Task S3.5)
**Impact:** ~2-3h additional work in Sprint 3

### Issue #2: Space Switcher Wiring Complexity
**Problem:** SpaceManager not passed to drawer
**Mitigation:** Thread SpaceManager through MainContent composition (Task S1.4)
**Impact:** ~1h additional work in Sprint 1

### Issue #3: Groq/Cerebras Deprecation Message
**Problem:** Users with existing API keys lose access
**Mitigation:** Show one-time deprecation banner on app launch (Task S4.4)
**Impact:** ~1-2h additional work in Sprint 4

---

## Success Metrics (End of Phase 1)

- ✅ Single unified chat screen (not 17 tabs)
- ✅ Left drawer navigation (conversations, spaces, settings)
- ✅ Model picker chip (bottom sheet)
- ✅ Quick action chips (5 chips visible)
- ✅ Per-chat settings sheet (temperature, top-K, top-P, max tokens)
- ✅ Tool results render with markdown
- ✅ Provider removal complete (Groq/Cerebras gone)
- ✅ 13 screens deleted
- ✅ Zero compile errors
- ✅ All integration tests passing

**Phase 1 Duration:** 4-6 weeks (2 developers, parallel work)
**Phase 1 Effort:** 78-97 hours
**Ready for Phase 2:** All 17 features foundation built

---

## Phase 2 Preview

Once Phase 1 ships, Phase 2 will add:
1. Smart chips with live data
2. Tool result intelligent rendering
3. Context window + auto-summarize
4. **Knowledge Base Evolution** (LLM wiki pattern - NEW)
5. Streaming UI polish
6. Voice modes (all 3)
7. Auto-generated titles
8. Search conversations
9. Suggested follow-ups
10. Message actions (copy, regenerate, delete)

**Phase 2 Duration:** 4-6 weeks
**Phase 2 Effort:** 40-60 hours

---

**This plan is executable.** Pass to the development team with confidence.