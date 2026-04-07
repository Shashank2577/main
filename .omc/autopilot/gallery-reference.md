# Google AI Edge Gallery — Reference Analysis

_Cloned from: https://github.com/google-ai-edge/gallery_
_Clone path: /tmp/gallery-ref_
_Analyzed: 2026-04-06_

---

## 1. Repository Structure

```
gallery/
  Android/src/app/src/main/java/com/google/ai/edge/gallery/
    ui/
      home/           HomeScreen, SettingsDialog, SquareDrawerItem
      common/         ModelPickerChip, ModelPageAppBar, chat/*
      navigation/     GalleryNavGraph (NavHost-based routing)
      benchmark/      BenchmarkScreen, BenchmarkViewModel
      modelmanager/   GlobalModelManager, ModelManagerViewModel
    customtasks/
      agentchat/      AgentChatScreen, AgentTools, SkillManager
      mobileactions/  MobileActionsScreen
      examplecustomtask/
    data/             Task, Model, Category, ModelDownloadStatus
    common/           ProjectConfig, Utils, Types
```

---

## 2. App Organization Pattern

Gallery is **NOT a single-screen app** and uses **Jetpack Navigation Compose** (`NavHost`) as its primary routing mechanism — in contrast to OpenClaw's integer tab-index state machine.

Gallery routes:
- `homepage` — HomeScreen (task/capability grid)
- `model_list` — ModelManager (model picker for a selected task)
- `route_model/{taskId}/{modelName}` — per-model task screen
- `model_manager` — GlobalModelManager (all models)
- `benchmark/{modelName}` — BenchmarkScreen

**Key difference from OpenClaw:** Gallery is a demo/showcase app organized around **AI tasks** (LLM chat, image classification, etc.) as top-level entities. OpenClaw is a personal assistant organized around **features** (tasks, notes, calendar). The Gallery UI pattern is worth adopting; its navigation architecture is not a direct fit and should not be copied.

---

## 3. HomeScreen UI Pattern

**File:** `ui/home/HomeScreen.kt`

Gallery's HomeScreen uses a `ModalNavigationDrawer` wrapping a `Scaffold` with:
- Custom `GalleryTopAppBar` (settings icon + models icon in trailing actions)
- Main content: title hero text + horizontally-scrolling task cards organized by category
- Task cards rendered as `Card` composables in a `LazyRow` per category
- Elaborate entrance animations: staggered `AnimatedVisibility` with `EaseOutExpo` timing

**OpenClaw adoption:** The `ModalNavigationDrawer` pattern is already in OpenClaw. The staggered entrance animation pattern (constants like `TASK_CARD_ANIMATION_DELAY_OFFSET = 100`) is worth adopting for quick-action chips.

---

## 4. Chat UI Pattern

**Files:** `ui/common/chat/ChatPanel.kt`, `ChatView.kt`, `MessageBubble*.kt`

Gallery's chat UI is composed of:
- `ChatPanel` — root composable containing `LazyColumn` (messages) + input area
- `MessageInputText` — `BasicTextField` with send button, audio recorder button, and image attach button
- `AudioRecorderPanel` — expandable voice input with waveform animation
- Message types: `MessageBodyText`, `MessageBodyImage`, `MessageBodyAudioClip`, `MessageBodyLoading`, `MessageBodyThinking`, `MessageBodyError`, `MessageBodyWarning`, `MessageBodyBenchmark`
- `SnackbarHost` for transient notifications
- `nestedScroll` modifier with a `NestedScrollConnection` to hide input when scrolling up

**OpenClaw adoption notes:**
- OpenClaw's `ChatScreen.kt` already has a `LazyColumn` + `BasicTextField` pattern — structurally similar
- Gallery's `MessageBodyThinking` (animated "thinking" indicator) is worth adapting
- Gallery's `nestedScroll` hide-on-scroll input behavior is worth adding
- Gallery uses `spring(stiffness = Spring.StiffnessMediumLow)` for message appearance animations — adopt this instead of `tween`

---

## 5. ModelPickerChip Pattern

**File:** `ui/common/ModelPickerChip.kt`

This is the highest-value component to adapt. Key characteristics:

```
Row (pill shape, CircleShape, surfaceContainerHigh background)
  StatusIcon (download/init status)
  AnimatedVisibility (CircularProgressIndicator when initializing)
  Text (model display name, maxLines=1, MiddleEllipsis overflow)
  Icon (ArrowDropDown)

On tap → ModalBottomSheet (skipPartiallyExpanded = true)
  ModelPicker list (scrollable list of models for this task)
```

**OpenClaw adaptation:**
- Replace `ModelManagerViewModel` dependency with `ModelRouter.getAvailableModels()`
- Replace Gallery's `ModelDownloadStatus` with OpenClaw's `LiteRTProvider.downloadState`
- The `ModalBottomSheet` picker is a direct pattern match — OpenClaw should use this for model switching in the ChatScreen TopBar
- No Hilt dependency needed — pass `ModelRouter` directly as a parameter

---

## 6. ModelPageAppBar Pattern

**File:** `ui/common/ModelPageAppBar.kt`

Gallery's per-task top bar contains:
- Back button
- Task title + task icon
- `ModelPickerChip` (centered)
- Optional config button (opens settings bottom sheet)
- Optional reset session button

**OpenClaw adaptation:**
- ChatScreen's existing TopBar can be extended with `ModelPickerChip` in the center slot
- The "config" button maps to `PerChatSettingsSheet`
- The "reset session" button maps to "new conversation"

---

## 7. AgentChat Pattern (Custom Task)

**Files:** `customtasks/agentchat/AgentChatScreen.kt`, `AgentTools.kt`, `SkillManagerBottomSheet.kt`

Gallery's AgentChat custom task is a full agentic chat with:
- Tool/skill registration as "skills" loadable at runtime
- `SkillManagerBottomSheet` for managing available tools
- `IntentHandler` for deep-link / intent-based skill invocation
- Secrets management (`SecretEditorDialog`) for per-skill API keys

This is architecturally close to OpenClaw's `AgentRuntime` + `ToolRegistry` pattern but more dynamic (runtime skill loading via URL/file import). OpenClaw's static tool registry is simpler and appropriate for MVP.

---

## 8. Navigation Animation Patterns

**File:** `ui/navigation/GalleryNavGraph.kt`

Gallery's navigation transitions:
```kotlin
private val ENTER_ANIMATION_EASING = EaseOutExpo
private const val ENTER_ANIMATION_DURATION_MS = 500
private const val ENTER_ANIMATION_DELAY_MS = 100

// Horizontal slide: Left for enter, Right for exit
slideIntoContainer(towards = SlideDirection.Left, animationSpec = enterTween())
slideOutOfContainer(towards = SlideDirection.Right, animationSpec = exitTween())

// Vertical slide: Up for modal-like screens
slideIntoContainer(towards = SlideDirection.Up)
slideOutOfContainer(towards = SlideDirection.Down)
```

**OpenClaw adoption:** Current OpenClaw uses `fadeIn(tween(250)) togetherWith fadeOut(tween(200))`. Upgrading to EaseOutExpo slide transitions would match Gallery's polish. This is Phase 2 work.

---

## 9. Theme/Settings Patterns

**File:** `ui/home/SettingsDialog.kt`

Gallery uses a `AlertDialog`-based settings panel (not a full screen). Contains:
- Dark mode toggle
- Analytics opt-out
- Terms of service link
- App version display

**OpenClaw note:** OpenClaw's full `SettingsScreen` is more comprehensive than Gallery's dialog. Keep OpenClaw's approach.

---

## 10. Model Download / Status Pattern

**Files:** `ui/common/chat/ModelDownloadStatusInfoPanel.kt`, `ModelNotDownloaded.kt`

When a model is not yet downloaded, Gallery shows a placeholder panel with download progress instead of the chat UI. Uses `AnimatedContent` on `curDownloadStatus?.status == ModelDownloadStatusType.SUCCEEDED`.

**OpenClaw adoption:** LiteRT model download state (`ModelDownloadManager`) should drive similar gating in the model picker: show download progress inline in the bottom sheet when a local model is selected but not yet downloaded.

---

## 11. Specific Composables Worth Adapting (Direct Reuse or Close Adaptation)

| Gallery Composable | File | Adapt How |
|-------------------|------|----------|
| `ModelPickerChip` | `ui/common/ModelPickerChip.kt` | Copy UI structure; replace ViewModel wiring with ModelRouter |
| `AudioRecorderPanel` | `ui/common/chat/AudioRecorderPanel.kt` | Adapt for OpenClaw's VoiceInputButton; replace Gallery audio APIs with existing AudioRecord impl |
| `MessageBodyThinking` | `ui/common/chat/MessageBodyThinking.kt` | Adapt animated "thinking" dots for AgentRuntime streaming state |
| `ModelPageAppBar` | `ui/common/ModelPageAppBar.kt` | Adapt ChatScreen TopBar to match this layout (back/hamburger + model chip + actions) |
| `RotationalLoader` | `ui/common/RotationalLoader.kt` | Use for model initialization loading state |
| `GlitteringShapesLoader` | `ui/common/GlitteringShapesLoader.kt` | Optional: use on splash or empty chat state |
| `TextInputHistorySheet` | `ui/common/chat/TextInputHistorySheet.kt` | Adapt as prompt history bottom sheet (Phase 2) |

---

## 12. Breaking Incompatibilities

| Concern | Gallery Approach | OpenClaw Constraint | Resolution |
|---------|-----------------|---------------------|------------|
| **Hilt DI** | Gallery uses `@HiltViewModel` throughout; `hiltViewModel()` in composables | OpenClaw has no Hilt; uses manual constructor injection via `OpenClawApp` | Never copy Gallery ViewModels. Adapt UI only. Pass dependencies as composable parameters. |
| **NavHost routing** | `NavHostController` with typed routes | OpenClaw uses a `var currentTab: Int` state machine | OpenClaw's approach is simpler and sufficient. Do not migrate to NavHost in MVP. |
| **Firebase Analytics** | `firebaseAnalytics?.logEvent(...)` scattered throughout | OpenClaw has no Firebase | Strip all analytics calls when adapting Gallery code. |
| **Task/Model data layer** | Gallery's `Task` and `Model` objects carry download state, config params, benchmark results | OpenClaw has `LlmProvider` + `ModelInfo` | Map `ModelInfo` to the display model in `ModelPickerChip`; no need for Gallery's full `Model` data class. |
| **Coroutine scope** | Gallery ViewModels own coroutine scopes | OpenClaw passes `AgentRuntime` directly | Use `rememberCoroutineScope()` in composables for local async work; delegate to `AgentRuntime` for inference. |
| **`@StringRes` / resource strings** | Gallery externalizes all strings to `res/strings.xml` | OpenClaw uses hardcoded strings | Follow OpenClaw's existing pattern (hardcoded strings are fine for MVP). |
| **Compose Multiplatform** | Gallery is Android-only | OpenClaw is Android-only | No conflict. |
