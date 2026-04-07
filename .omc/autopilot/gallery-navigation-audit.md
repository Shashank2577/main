# Gallery Navigation & State Management Audit

_Analyst: navigation-architecture-analyst_
_Date: 2026-04-06_
_Sources: gallery-reference.md, spec.md, screen-disposition.md, MainActivity.kt, ChatScreen.kt, ConversationHistoryScreen.kt, AgentRuntime.kt_

---

## 1. Gallery Navigation Architecture

### 1.1 How Gallery Routes (NavHost-based)

Gallery uses **Jetpack Navigation Compose** with a `NavHostController` and typed string routes. There is no bottom navigation bar.

```
GalleryNavGraph (NavHost)
  "homepage"                          → HomeScreen
  "model_list/{taskId}"              → ModelListScreen
  "route_model/{taskId}/{modelName}" → per-model task screen (AgentChatScreen, etc.)
  "model_manager"                    → GlobalModelManager
  "benchmark/{modelName}"            → BenchmarkScreen
```

Navigation transitions use `EaseOutExpo` slide animations:
```kotlin
// Enter: slide left (forward nav)
slideIntoContainer(SlideDirection.Left, animationSpec = tween(500, easing = EaseOutExpo))
// Exit: slide right (back nav)
slideOutOfContainer(SlideDirection.Right, animationSpec = tween(500, easing = EaseOutExpo))
// Modal screens: slide up
slideIntoContainer(SlideDirection.Up)
slideOutOfContainer(SlideDirection.Down)
```

### 1.2 Gallery Overlay Hierarchy

```
HomeScreen
  ModalNavigationDrawer (hamburger icon → SpaceDrawer)
    Scaffold
      GalleryTopAppBar (settings icon, models icon)
      Content: task category grid (LazyRow per category)

per-model screen (e.g. AgentChatScreen)
  ModelPageAppBar
    Back button
    Task title + icon
    ModelPickerChip (center)  ← tap → ModalBottomSheet
    Config button             ← tap → PerTaskSettingsSheet (ModalBottomSheet)
    Reset session button
  ChatPanel (LazyColumn + input)
```

Key overlay rules:
- **Drawer** and **bottom sheets** do not conflict — drawer is scrim-dismiss and closes before a sheet would open
- `ModalBottomSheet(skipPartiallyExpanded = true)` — sheets are either fully open or closed, no half-expanded state
- Only **one sheet** can be open at a time; Gallery does not compose multiple sheets simultaneously
- No explicit sheet manager / mutex — each sheet is driven by a separate `showX: Boolean` state var in the host composable

---

## 2. OpenClaw Current Navigation Architecture

### 2.1 Existing Integer Tab State Machine

```kotlin
// MainActivity.kt:181
var currentTab by remember { mutableIntStateOf(3) } // Start on Chat

// 17 tabs:
// 0=Dashboard, 1=Tasks, 2=Notes, 3=Chat, 4=Discover,
// 5=Settings, 6=Calendar, 7=Travel, 8=Insights, 9=Briefing,
// 10=Team, 11=Files, 12=Voice, 13=Habits, 14=Decisions,
// 15=Memory, 16=Reminders

AnimatedContent(targetState = currentTab) { tab ->
    when (tab) {
        0  -> DashboardScreen(onNavigateToChat = { currentTab = 3 }, ...)
        3  -> ChatScreen(onOpenDrawer = { scope.launch { drawerState.open() } }, ...)
        5  -> SettingsScreen(onBack = { currentTab = 3 }, ...)
        11 -> FileBrowserScreen(...)
        12 -> VoiceConversationScreen(onDismiss = { currentTab = 3 }, ...)
        15 -> MemoryScreen(onNavigateToChat = { currentTab = 3 }, ...)
        ...
    }
}
```

Navigation is callback-only: no NavController, no route strings, no back stack.

### 2.2 Existing Drawer (ModalNavigationDrawer)

```kotlin
ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet {
            ConversationHistoryPanel(
                conversations = ...,
                activeConversationId = ...,
                onNewConversation = { scope.launch { agentRuntime.startNewConversation(); drawerState.close(); currentTab = 3 } },
                onSelectConversation = { id -> scope.launch { agentRuntime.loadConversation(id); drawerState.close(); currentTab = 3 } },
                onDeleteConversation = { ... },
                onRenameConversation = { ... },
                onSearchConversations = { ... },
            )
        }
    }
)
```

**Current drawer content:** conversation history + search only.
**Missing:** space switcher, nav links to Settings / Files / Memory / Voice.

### 2.3 Existing ChatScreen State Variables

```kotlin
// ChatScreen.kt — current local state (no ViewModel, no Hilt)
var inputText by remember { mutableStateOf("") }
var showVoiceMode by remember { mutableStateOf(false) }
var pendingMedia by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
var showModelPicker by remember { mutableStateOf(false) }    // model picker sheet
var showExportDialog by remember { mutableStateOf(false) }
var showChatSettings by remember { mutableStateOf(false) }   // per-chat settings sheet
var showCrashDialog by remember { mutableStateOf(false) }
var isOnline by remember { mutableStateOf(true) }
```

`showModelPicker` and `showChatSettings` already exist as state vars — the sheet wiring just needs the composable bodies and trigger points.

### 2.4 AgentRuntime State

```kotlin
// AgentRuntime.kt — StateFlows exposed to composables
val state: StateFlow<AgentState>   // Idle | Running | Error
val events: StateFlow<List<AgentEvent>>  // message list (capped at 500)

// Mutable fields (set from MainActivity)
var systemPrompt: String
var preferredModelId: String?      // null = auto-select via ModelRouter
private var _activeSpaceId: String?

// Key read properties
val activeSpaceName: String?       // derived from _activeSpaceId + SpaceManager
val activeModelName: String?       // derived from preferredModelId + ModelRouter
```

No ViewModel. AgentRuntime is created once in `OpenClawApp` (application singleton), passed down as a constructor parameter to composables.

---

## 3. Data Flow Diagrams

### 3.1 Current Navigation Data Flow

```
OpenClawApp (singleton)
  agentRuntime: AgentRuntime
  conversationManager: ConversationManager
  modelRouter: ModelRouter
  spaceManager: SpaceManager
        │
        ▼
MainActivity
  MainApp()
    currentScreen: AppScreen (SPLASH|ONBOARDING|MAIN)
        │
        ▼
  MainContent(app)
    currentTab: Int  ←──────────────────────────────────┐
    drawerState: DrawerState                            │
        │                                              │
    ModalNavigationDrawer                              │
        │  drawer: ConversationHistoryPanel            │
        │    onSelectConversation → agentRuntime.loadConversation() + currentTab=3
        │                                              │
    AnimatedContent(currentTab)                        │
        │                                              │
    ChatScreen(runtime, onNavigateToSettings = { currentTab=5 }) ──┘
    SettingsScreen(onBack = { currentTab=3 })
    FileBrowserScreen(...)
    VoiceConversationScreen(onDismiss = { currentTab=3 })
    MemoryScreen(onNavigateToChat = { currentTab=3 })
    [13 other screens — to be deleted]
```

### 3.2 Target Navigation Data Flow (Post-Refactor)

```
OpenClawApp (singleton)  [unchanged]
        │
        ▼
MainActivity
  MainApp()
    currentScreen: AppScreen (SPLASH|ONBOARDING|MAIN)
        │
        ▼
  MainContent(app)
    currentScreen: MainScreen  ← sealed class: Chat|Settings|Files|Voice|Memory
    drawerState: DrawerState
        │
    ModalNavigationDrawer
        │  drawer: ModalDrawerSheet
        │    SpaceSwitcher        ← NEW
        │    [New Conversation button]
        │    ConversationHistoryPanel  ← existing
        │    ─────
        │    Memory   → currentScreen = MainScreen.Memory
        │    Files    → currentScreen = MainScreen.Files
        │    Settings → currentScreen = MainScreen.Settings
        │
    AnimatedContent(currentScreen)
        │
    MainScreen.Chat →
        ChatScreen
          TopBar:
            HamburgerIcon → drawerState.open()
            Title (conversation name)
            ModelPickerChip → showModelPicker=true
                              ModalBottomSheet(model list)
            SettingsIcon → showChatSettings=true
                           ModalBottomSheet(per-chat settings)
          MessageList (LazyColumn, events from AgentRuntime.events)
          QuickActionChips (LazyRow, 8 chips)  ← NEW
          InputBar (BasicTextField + voice + image attach)

    MainScreen.Settings → SettingsScreen(onBack = navigate(Chat))
    MainScreen.Files    → FileBrowserScreen(...)
    MainScreen.Voice    → VoiceConversationScreen(onDismiss = navigate(Chat))
    MainScreen.Memory   → MemoryScreen(onNavigateToChat = navigate(Chat))
```

### 3.3 Conversation State Flow

```
ConversationManager (Room DAO wrapper)
  allConversations: Flow<List<ConversationEntity>>   → collected in MainContent
  activeConversationId: String?                       → passed to drawer panel

AgentRuntime
  loadConversation(id) → sets active conversation, loads message history → _events updated
  startNewConversation() → creates new ConversationEntity, clears _events

ChatScreen observes:
  runtime.state  (collectAsState) → drives loading indicator
  runtime.events (collectAsState) → drives LazyColumn message list

Drawer panel triggers:
  onSelectConversation(id) → agentRuntime.loadConversation(id) + drawerState.close() + navigate(Chat)
  onNewConversation()      → agentRuntime.startNewConversation() + drawerState.close() + navigate(Chat)
```

---

## 4. Compatibility Analysis: Gallery Patterns vs OpenClaw

### 4.1 NavHost — Do NOT Adopt

| Factor | Detail |
|--------|--------|
| Gallery uses | `NavHostController` with typed string routes |
| OpenClaw has | `var currentTab: Int` → sealed class `MainScreen` (5 variants) |
| Compatibility | **Not compatible and not needed.** NavHost adds back-stack, route serialization, and argument passing complexity that OpenClaw doesn't need for 5 screens. |
| Recommendation | **Keep sealed-class state machine.** It is simpler, has no third-party dependency, and maps directly to the 5-destination design. |
| Effort if migrated | 12–16 hours (rewriting composable signatures, wiring NavController, argument passing, back handler) for zero user-visible benefit. |

### 4.2 ModalNavigationDrawer — Already Compatible

| Factor | Detail |
|--------|--------|
| Gallery uses | `ModalNavigationDrawer` wrapping `Scaffold` on HomeScreen |
| OpenClaw has | `ModalNavigationDrawer` with `DrawerState` in `MainContent` — exact same pattern |
| Compatibility | **Fully compatible.** Same API, same structure. |
| Delta work | Add `SpaceSwitcher` composable above `ConversationHistoryPanel`. Add three nav links (Memory, Files, Settings) at drawer bottom. ~3–4 hours. |

### 4.3 ModelPickerChip + ModalBottomSheet — Adopt UI, Not ViewModel

| Factor | Detail |
|--------|--------|
| Gallery uses | `ModelPickerChip` composable → taps open `ModalBottomSheet(skipPartiallyExpanded=true)` driven by `showPicker: Boolean` state var in the host composable |
| OpenClaw already has | `var showModelPicker by remember { mutableStateOf(false) }` in ChatScreen |
| Compatibility | **High.** The state hook already exists. Only the composable body needs to be built. |
| Incompatibility | Gallery's chip reads model list from `ModelManagerViewModel` (Hilt). OpenClaw has no Hilt. Must replace with `modelRouter.getAvailableModels()` passed as a parameter. |
| Effort | 4–5 hours (create `ModelPickerChip.kt`, wire to existing `modelRouter`, populate `ModalBottomSheet` with model list, add chip to ChatScreen TopBar). |

### 4.4 PerChatSettingsSheet — Adopt Gallery's ModalBottomSheet Pattern

| Factor | Detail |
|--------|--------|
| Gallery uses | Config button in `ModelPageAppBar` → `ModalBottomSheet` for per-task parameters (temperature, top-k, max tokens) |
| OpenClaw already has | `var showChatSettings by remember { mutableStateOf(false) }` in ChatScreen |
| Compatibility | **High.** Same pattern — state var already exists, only the sheet body needs to be created. |
| New data contract | `PerChatSettings` data class (defined in spec.md); stored in `SettingsRepository` keyed by `conversationId`. |
| Effort | 3–4 hours (create `PerChatSettingsSheet.kt`, wire temperature/model sliders, persist via `SettingsRepository`). |

### 4.5 QuickActionChips — New Component, No Gallery Equivalent

| Factor | Detail |
|--------|--------|
| Gallery has | None (Gallery is task-centric, not feature-centric). |
| OpenClaw needs | Horizontal `LazyRow` of `QuickActionChip` items above the input bar |
| Pattern | Same `FilterChip` / `SuggestionChip` Material3 pattern already used in `SuggestedPrompts.kt` |
| Compatibility | **High.** Extend `SuggestedPrompts.kt` or create a new `QuickActionChips.kt` file with the `QuickActionChip` data class from spec.md. |
| Effort | 2–3 hours (create component, define 8 default chips, add to ChatScreen layout, wire navigation callbacks for Voice/Files/Memory chips). |

### 4.6 Sheet Conflict Resolution (Drawer + Multiple Sheets)

Gallery's implicit rule is: **one overlay at a time**. The pattern works because:
1. Drawer is controlled by `DrawerState` which disables the main content when open (scrim)
2. Sheets are controlled by independent `Boolean` state vars; only one can be `true` at a time by design

**OpenClaw should adopt the same pattern:**
- `showModelPicker`, `showChatSettings` are already independent Boolean vars
- Add: when `drawerState.isOpen` do not show sheets (implicit due to scrim)
- No explicit mutex needed — the drawer's scrim swallows taps before they reach sheet triggers

---

## 5. State Management Patterns to Adopt

### 5.1 Conversation Persistence — Already Correct

OpenClaw's Room-backed `ConversationManager` + `AgentRuntime.loadConversation()` is architecturally superior to Gallery's in-memory approach. **No changes needed.**

```kotlin
// Already working correctly:
val conversations by app.conversationManager.allConversations.collectAsState(initial = emptyList())
// Drawer observes this Flow and re-renders on any conversation CRUD operation
```

### 5.2 Model Selection State — Needs a Single Source of Truth

Current gap: `AgentRuntime.preferredModelId` is a plain `var`, not a `StateFlow`. ChatScreen cannot reactively observe model changes.

**Adopt Gallery's pattern:** make the selected model observable.

```kotlin
// AgentRuntime.kt — add:
private val _preferredModelId = MutableStateFlow<String?>(null)
val preferredModelId: StateFlow<String?> = _preferredModelId.asStateFlow()

fun setPreferredModel(id: String?) {
    _preferredModelId.value = id
}
```

Then in ChatScreen's TopBar, `ModelPickerChip` collects `runtime.preferredModelId` to show the current model name reactively.

Effort: ~1 hour.

### 5.3 Space/Context Switching — Add to Drawer

Current state: `AgentRuntime.setActiveSpace(spaceId)` exists and works. `spaceManager.getSpaces()` returns the list.

Gap: no UI surface for space selection. The drawer `ConversationHistoryPanel` has no space switcher above it.

**Adopt:** `SpaceSwitcher` composable at top of `ModalDrawerSheet` — a horizontally scrolling row of Space chips. Tapping a chip calls `agentRuntime.setActiveSpace(id)` and closes the drawer.

```kotlin
// SpaceSwitcher.kt (new component):
@Composable
fun SpaceSwitcher(
    spaces: List<Space>,
    activeSpaceId: String?,
    onSelectSpace: (String?) -> Unit,
)
```

Effort: 2–3 hours.

### 5.4 PerChatSettings Storage — In-Memory with SharedPrefs Persistence

As defined in spec.md, `PerChatSettings` is stored in `SettingsRepository` under key `per_chat_{conversationId}`. This is the right approach — no new Room table needed.

Gap: `SettingsRepository` has no `getPerChatSettings()` / `setPerChatSettings()` methods yet.

Effort: ~1 hour to add the two methods + JSON serialization.

---

## 6. Recommended Implementation Approach for Unified Chat

### Phase 1: Navigation Skeleton (4–6 hours)

1. **Replace integer tab system** with `sealed class MainScreen` (5 variants) in `MainContent`
   - File: `MainActivity.kt` lines 181–388
   - Replace `var currentTab by remember { mutableIntStateOf(3) }` with `var currentScreen by remember { mutableStateOf<MainScreen>(MainScreen.Chat) }`
   - Replace all `when (tab)` branches with `when (currentScreen)` — keep only Chat, Settings, Files, Voice, Memory cases
   - Delete all 13 removed screen cases

2. **Extend the drawer** (`ModalDrawerSheet` in `MainContent`):
   - Add `SpaceSwitcher` at top
   - Add separator + nav links: Memory, Files, Settings at bottom
   - Pass `navigate: (MainScreen) -> Unit` lambda into drawer

3. **Update ChatScreen signature** — replace `onNavigateToDashboard` with `onNavigate: (MainScreen) -> Unit`

### Phase 2: New Components (8–10 hours)

4. **ModelPickerChip.kt** — adapted from Gallery's `ui/common/ModelPickerChip.kt`
   - Replace `ModelManagerViewModel` with `modelRouter: ModelRouter` parameter
   - Replace `ModelDownloadStatus` with `LiteRTProvider.downloadState`
   - Uses `ModalBottomSheet(skipPartiallyExpanded = true)` — same as Gallery
   - Add to `ChatScreen` TopBar center slot

5. **QuickActionChips.kt** — no Gallery equivalent; adapt from `SuggestedPrompts.kt` pattern
   - `LazyRow` of `SuggestionChip` with 8 predefined chips
   - Navigation chips (Voice, Files, Memory) call `onNavigate(MainScreen.X)`
   - Prompt chips inject text or auto-send via `runtime.sendMessage()`

6. **PerChatSettingsSheet.kt** — adapted from Gallery's config bottom sheet
   - Temperature slider, max tokens field, model override selector
   - Persists via `SettingsRepository.setPerChatSettings(conversationId, settings)`

7. **SpaceSwitcher.kt** — no Gallery equivalent; new component
   - Horizontal chip row using `spaceManager.getSpaces()`
   - Active space highlighted with primary color

### Phase 3: State Management Cleanup (2–3 hours)

8. **Make `preferredModelId` a `StateFlow`** in `AgentRuntime` (see 5.2 above)
9. **Add `getPerChatSettings()` / `setPerChatSettings()`** to `SettingsRepository`
10. **Add `purgePerChatSettings(conversationId)`** call to `deleteConversation()` in `ConversationManager`

---

## 7. Effort Summary

| Task | Component | Hours | Risk |
|------|-----------|-------|------|
| Replace integer tab with sealed class | `MainActivity.kt` | 3–4 | Medium — many `when` branches |
| Extend drawer (SpaceSwitcher + nav links) | `MainActivity.kt` + new `SpaceSwitcher.kt` | 3–4 | Low |
| Delete 13 screen files + clean callbacks | Multiple files | 2–3 | Medium — dangling imports |
| `ModelPickerChip.kt` | New component | 4–5 | Low — Gallery reference exists |
| `QuickActionChips.kt` | New component | 2–3 | Low |
| `PerChatSettingsSheet.kt` | New component | 3–4 | Low |
| `preferredModelId` StateFlow in AgentRuntime | `AgentRuntime.kt` | 1 | Low |
| `PerChatSettings` in `SettingsRepository` | `SettingsRepository.kt` | 1 | Low |
| **Total** | | **19–25 hours** | |

---

## 8. Anti-Patterns to Avoid

| Anti-Pattern | Why | What to Do Instead |
|-------------|-----|-------------------|
| Migrating to NavHost | Unnecessary complexity for 5 screens; no back-stack needed; would break all composable signatures | Keep sealed-class state machine |
| Copying Gallery ViewModels | Gallery uses `@HiltViewModel`; OpenClaw has no Hilt — copy will not compile | Adapt UI only; pass `AgentRuntime` / `ModelRouter` as parameters |
| Multiple sheets open simultaneously | Gallery never does this; Android `ModalBottomSheet` stacking is undefined | One `Boolean` state var per sheet; enforce single-sheet-open invariant |
| `skipPartiallyExpanded = false` on sheets | Partial expand creates ambiguous UX for model picker | Always use `skipPartiallyExpanded = true` for action sheets |
| Firebase analytics calls from Gallery code | OpenClaw has no Firebase | Strip all `firebaseAnalytics?.logEvent(...)` when adapting Gallery composables |

---

## 9. Key Files Summary

| File | Action | What Changes |
|------|--------|-------------|
| `MainActivity.kt` | Modify | Replace `currentTab: Int` with `currentScreen: MainScreen`; delete 13 screen cases; extend drawer |
| `ChatScreen.kt` | Modify | Add `ModelPickerChip` to TopBar; add `QuickActionChips` row; update `onNavigateToDashboard` → `onNavigate` |
| `AgentRuntime.kt` | Modify | Make `preferredModelId` a `StateFlow`; add `setPreferredModel()` |
| `SettingsRepository.kt` | Modify | Add `getPerChatSettings()` / `setPerChatSettings()` / `purgePerChatSettings()` |
| `ConversationHistoryScreen.kt` | Keep | No changes needed — already production-quality |
| `ModelPickerChip.kt` | Create | Adapted from Gallery `ui/common/ModelPickerChip.kt` |
| `QuickActionChips.kt` | Create | New; adapts `SuggestedPrompts.kt` pattern |
| `PerChatSettingsSheet.kt` | Create | Adapted from Gallery config bottom sheet |
| `SpaceSwitcher.kt` | Create | New; no Gallery equivalent |
