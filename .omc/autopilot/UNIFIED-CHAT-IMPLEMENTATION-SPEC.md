# UNIFIED CHAT IMPLEMENTATION SPEC (Final)

_Synthesized from 7 audits by spec-synthesizer | Date: 2026-04-07_
_Sources: gallery-components-audit, gallery-theme-audit, gallery-navigation-audit, gallery-llm-audit, gallery-streaming-audit, gallery-tools-audit, gallery-voice-audit_

---

## Executive Summary

- **Project scope**: Unified Chat screen with 17 features across 2 phases
- **Phase 1 (Baseline)**: 4–6 weeks, ~76–100h estimated effort, 2 developers
- **Phase 2 (Features & Polish)**: 4–6 weeks, remaining features
- **Gallery extraction**: ~40% code reuse, ~60% new or adapted
- **Architecture verdict**: OpenClaw's architecture is production-quality and closely mirrors Gallery's patterns. Primary work is **extraction** (moving inline composables to files), **wiring** (connecting sheet state to AgentRuntime), and **creation** of two missing components (QuickActionChips, SpaceSwitcher). No ViewModel or Hilt migration required.

---

## PHASE 1: BASELINE UNIFIED CHAT (4–6 weeks)

---

### Components to Extract from Gallery

| Component | Gallery Source | OpenClaw Action | Conflicts to Fix First | Effort |
|-----------|---------------|-----------------|------------------------|--------|
| `ModelPickerChip` | `ui/common/ModelPickerChip.kt` — pill `Row(CircleShape)`, `surfaceContainerHigh` bg, `AnimatedVisibility(CircularProgressIndicator)` on init, `ModalBottomSheet(skipPartiallyExpanded=true)` on tap | Extract center-slot clickable from `ChatScreen.kt` into standalone `ModelPickerChip.kt`. Replace `ModelManagerViewModel` (Hilt) with `modelRouter: ModelRouter` param. Wire download state from `LiteRTProvider`. | None | 4h |
| `ThinkingIndicator` | `ui/common/chat/MessageBodyThinking.kt` — `AnimatedVisibility(expandVertically/shrinkVertically)`, left border via `drawBehind(strokeWidth=2.dp)`, auto-expands while `inProgress=true` | Extract private function `ThinkingIndicator` from `ChatScreen.kt` L910–961 into standalone `ThinkingIndicator.kt`. Upgrade to Gallery's `expandVertically`/`drawBehind` pattern. | Dead branch `isInitializing=false` referencing commented-out `AgentEvent.InitializingModel` — remove dead branch | 1h |
| `RotationalLoader` | `ui/common/RotationalLoader.kt` — 2×2 grid of icons, outer rotation `CubicBezierEasing(0.5,0.16,0,0.71)` 2000ms, inner scale `EaseInOut` 1000ms, scale 1.0→0.4 | Copy directly as `RotationalLoader.kt`. Wire icon resources (four_circle, circle, double_circle, pantegon — create simple vector drawables or use Material icons). | None | 0.5h |
| `MessageBubbleShape` | `ui/common/chat/MessageBubbleShape.kt` — custom `Shape` class, 24dp radius, hard corner (0dp) on top-right for user bubble, top-left for agent bubble | Copy verbatim. Apply to `UserBubble` and `AssistantBubble` in `MessageBubble.kt`. | None | 0.5h |
| Auto-scroll 90px threshold | `ui/common/chat/ChatPanel.kt` — `LazyColumn` auto-scroll only when `< 90px` from bottom | Add to existing `ChatScreen.kt` `LazyColumn` scroll logic. Currently uses `animateScrollToItem` without threshold guard. | None | 0.5h |
| Loading placeholder → first token swap | `LlmChatViewModel.kt` — `ChatMessageLoading` added immediately on send, removed on first token arrival | Adopt pattern in `AgentRuntime.kt` — add `AgentEvent.Preparing` emitted immediately on `sendMessage()`, remove on first stream chunk. | None | 1h |
| `nestedScroll` hide-on-scroll | `ChatPanel.kt` — `NestedScrollConnection` hides input bar when scrolling up | Add `NestedScrollConnection` to `ChatScreen.kt` `LazyColumn`. Additive — no structural change. | None | 1h |

---

### Components from OpenClaw (Keep / Modify)

| Component | File | Status | Modification Needed |
|-----------|------|--------|---------------------|
| `MessageBubble` | `MessageBubble.kt` | Keep — production quality | Fix `ToolResultBubble` to use `MarkdownText` instead of raw `Text(Monospace)` — 5-line change |
| `MarkdownText` | `MarkdownText.kt` | Keep as-is — MORE capable than Gallery | None — Mermaid support via WebView is unique capability |
| `ConversationHistoryPanel` | `ConversationHistoryScreen.kt` | Keep as-is — production quality | None |
| `WelcomeHero` | `ChatScreen.kt` L859–902 | Keep as-is — well designed | None |
| `SuggestedPrompts` | `SuggestedPrompts.kt` | Keep as-is for empty state | Note: distinct from `QuickActionChips` (different purpose, different position) |
| `MediaPreviewStrip` | `MediaPreview.kt` | Keep `MediaPreview.kt` version (LazyRow + Coil) — delete duplicate in `ChatScreen.kt` | Fix hardcoded dark colors (`ElectricViolet`, `DarkGlass`) with `MaterialTheme.colorScheme` tokens |
| `VoiceInputButton` | `VoiceInputButton.kt` | Keep — Phase 2 waveform polish | None for Phase 1 |
| `VoiceConversationScreen` | `VoiceConversationScreen.kt` | Keep — MORE advanced than Gallery (full conversation loop) | None |
| `GlassCard` / `ClayCard` | `ClayCard.kt` | Keep — remove dead parameter | Remove unused `backgroundOpacity` param from `GlassCard` and `glassSurface` (0.5h) |
| Theme system | `ui/theme/*.kt` | Keep — intentional iOS-style design | Do NOT adopt Gallery's purple palette or dynamic color |
| `AgentRuntime` | `AgentRuntime.kt` | Modify | Convert `preferredModelId: String?` from plain `var` to `StateFlow<String?>` — ~8 lines diff |
| `SettingsRepository` | `SettingsRepository.kt` | Modify | Remove Groq/Cerebras getters; add `getPerChatSettings(conversationId)`, `savePerChatSettings()`, `purgePerChatSettings(conversationId)` |
| `MainActivity` / `MainContent` | `MainActivity.kt` | Modify | Replace `var currentTab: Int` (17 tabs) with `var currentScreen: MainScreen` (sealed class, 5 variants) |
| `OpenClawApp` | `OpenClawApp.kt` | Modify | Remove Groq/Cerebras provider registration (delete 2 map entries) |

---

### New Components to Create

| Component | File | Purpose | Dependencies | Effort |
|-----------|------|---------|--------------|--------|
| `ModelPickerChip.kt` | `ui/components/ModelPickerChip.kt` | Pill-shaped model selector chip in TopBar. On tap: `ModalBottomSheet(skipPartiallyExpanded=true)` with model list. Shows `CircularProgressIndicator` when LiteRT model is loading. | `ModelRouter.getAvailableModels()`, `AgentRuntime.preferredModelId` (StateFlow), `LiteRTProvider.downloadState` | 4h |
| `QuickActionChips.kt` | `ui/components/QuickActionChips.kt` | Persistent horizontal `LazyRow` of 8 action chips above `InputBar`. Prompt chips auto-send. Navigation chips (Voice, Files, Memory) call `onNavigate(MainScreen.X)`. | `MainScreen` sealed class, `AgentRuntime.sendMessage()` | 4h |
| `SpaceSwitcher.kt` | `ui/components/SpaceSwitcher.kt` | Horizontal chip row at top of `ModalDrawerSheet`. Tapping a space chip calls `agentRuntime.setActiveSpace(id)` and closes drawer. | `SpaceManager.getSpaces()`, `AgentRuntime.setActiveSpace()` | 3h |
| `PerChatSettingsSheet.kt` | `ui/components/PerChatSettingsSheet.kt` | `ModalBottomSheet` with temperature slider, topK/topP sliders, max tokens field, model override selector. Persists via `SettingsRepository.savePerChatSettings()`. | `SettingsRepository`, `AgentRuntime.chatSettings`, `conversationId` | 5h |
| `ThinkingIndicator.kt` | `ui/components/ThinkingIndicator.kt` | Extracted from `ChatScreen.kt` L910–961. Collapsable panel, auto-expands while `inProgress=true`, left border via `drawBehind`. | None | 1h |
| `InputBar.kt` | `ui/components/InputBar.kt` | Extracted from `ChatScreen.kt` `CleanInputBar` composable. Row: `[Attach] [Settings] [BasicTextField] [Voice|Send|Stop]`. Add `onVoiceMode` callback. | `VoiceInputButton`, `MediaPreviewStrip` | 2h |

**Total new component effort: 19h**

---

### Bugs to Fix Before Starting

Critical bugs that will block work if not fixed first:

| # | Bug | File(s) | Severity | Fix | Effort |
|---|-----|---------|----------|-----|--------|
| 1 | `MediaItem` defined twice — `data class` in `ChatScreen.kt` L60 and `sealed class` in `MediaPreview.kt` L231 | `ChatScreen.kt`, `MediaPreview.kt` | **CRITICAL** | Delete `data class MediaItem` from `ChatScreen.kt`. Update `uriToContentPart()` and any callers to construct sealed subtype (`MediaItem.Image(uri)`, etc.). Keep `MediaPreview.kt` sealed class as canonical. | 0.5h |
| 2 | `MediaPreviewStrip` defined twice — `ChatScreen.kt` L1137 (Row-based) and `MediaPreview.kt` L48 (LazyRow + Coil) with incompatible signatures | `ChatScreen.kt`, `MediaPreview.kt` | **CRITICAL** | Delete `Row`-based version from `ChatScreen.kt`. Use `MediaPreview.kt` as canonical. Update all call sites. | 0.5h |
| 3 | `ToolResultBubble` uses raw `Text(fontFamily=Monospace)` instead of `MarkdownText` — tool output with `**bold**` / `###` headers renders as literal symbols | `MessageBubble.kt` ~L193 | **HIGH** | Replace `Text(fontFamily=Monospace)` with `MarkdownText(markdown=displayText)`. Remove monospace font. Keep truncation/expand logic. | 0.25h |
| 4 | `PerChatSettingsSheet` content is commented out with `// TODO` — `AgentRuntime.chatSettings` field not yet wired | `ChatScreen.kt` L616–717, `AgentRuntime.kt` | **MEDIUM** | Implement `chatSettings: ChatSettings` mutable field on `AgentRuntime`. Uncomment `ChatSettingsSheetContent`. Wire sliders to runtime. | Part of PerChatSettingsSheet task (5h) |
| 5 | `ThinkingIndicator` has dead branch `isInitializing=false` referencing commented-out `AgentEvent.InitializingModel` | `ChatScreen.kt` L910–961 | **LOW** | Remove dead branch or add `InitializingModel` to `AgentEvent`. Either path clears the warning. | 0.25h |
| 6 | `SpeechRecognizer` created in `remember {}` inside composable — destroyed on config change (screen rotation during recording) | `VoiceInputButton.kt` | **LOW** | Move `SpeechRecognizer` lifecycle to a retained scope (or `ViewModel`). Fixes rare but reproducible bug. | 1–2h (Phase 2 OK) |

---

### Exact Design Specifications

#### Layout Metrics (dp)

| Element | Value | Source |
|---------|-------|--------|
| TopBar / ModelSelector height | 54dp | Gallery `dimens.xml` `model_selector_height` |
| Chat bubble corner radius | 24dp | Gallery `dimens.xml` `chat_bubble_corner_radius` |
| Chat bubble hard corner (user, top-right) | 0dp | Gallery `MessageBubbleShape.kt` |
| Chat bubble hard corner (agent, top-left) | 0dp | Gallery `MessageBubbleShape.kt` |
| Thinking panel horizontal padding | 12dp | Gallery `MessageBodyThinking.kt` |
| Thinking panel vertical padding | 8dp | Gallery `MessageBodyThinking.kt` |
| Thinking left-border stroke width | 2dp | Gallery `MessageBodyThinking.kt` |
| CollapsablePanel spinner size | 16dp | Gallery `MessageBodyCollapsableProgressPanel.kt` |
| CollapsablePanel spinner stroke width | 2dp | Gallery `MessageBodyCollapsableProgressPanel.kt` |
| Touch target minimum (buttons) | 48×48dp | Material3 / WCAG |
| Touch target minimum (chips) | 44×36dp | Material3 |
| VoiceInputButton diameter | 40dp | OpenClaw `VoiceInputButton.kt` |
| InputBar max text field lines | 5 | OpenClaw `ChatScreen.kt` |
| MediaPreview thumbnail size | 72dp | OpenClaw `MediaPreview.kt` |
| QuickActionChips height | 36dp | Material3 `SuggestionChip` default |
| AppShapes extraSmall | 6dp | OpenClaw `AppShapes` |
| AppShapes small | 10dp | OpenClaw `AppShapes` |
| AppShapes medium | 16dp | OpenClaw `AppShapes` |
| AppShapes large | 22dp | OpenClaw `AppShapes` |
| AppShapes extraLarge | 28dp | OpenClaw `AppShapes` |

---

#### Color Palette — Gallery Reference (for custom color tokens if adopted)

OpenClaw's existing palette is intentionally kept (iOS-style teal brand). The below Gallery colors are provided as reference for any custom color tokens added (e.g., chat bubble background, warning/error containers).

**Gallery Light Theme — Message Bubble Colors**

| Token | Hex | RGB | Usage |
|-------|-----|-----|-------|
| `agentBubbleBgColor` | `#E9EEF6` | rgb(233, 238, 246) | AI message bubble background |
| `userBubbleBgColor` | `#32628D` | rgb(50, 98, 141) | User message bubble background |
| `linkColor` | `#32628D` | rgb(50, 98, 141) | Hyperlinks |
| `successColor` | `#3D860B` | rgb(61, 134, 11) | Success state |
| `recordButtonBgColor` | `#EE675C` | rgb(238, 103, 92) | Record button |
| `waveFormBgColor` | `#AAAAAA` | rgb(170, 170, 170) | Audio waveform |
| `warningContainerColor` | `#FEF7E0` | rgb(254, 247, 224) | Warning container |
| `warningTextColor` | `#E37400` | rgb(227, 116, 0) | Warning text |
| `errorContainerColor` | `#FCE8E6` | rgb(252, 232, 230) | Error container |
| `errorTextColor` | `#D93025` | rgb(217, 48, 37) | Error text |

**Gallery Dark Theme — Message Bubble Colors**

| Token | Hex | RGB |
|-------|-----|-----|
| `agentBubbleBgColor` | `#1B1C1D` | rgb(27, 28, 29) |
| `userBubbleBgColor` | `#1F3760` | rgb(31, 55, 96) |
| `linkColor` | `#9DCAFC` | rgb(157, 202, 252) |
| `warningContainerColor` | `#554C33` | rgb(85, 76, 51) |
| `warningTextColor` | `#FCC934` | rgb(252, 201, 52) |
| `errorContainerColor` | `#523A3B` | rgb(82, 58, 59) |
| `errorTextColor` | `#EE675C` | rgb(238, 103, 92) |

**Gallery Full M3 Light ColorScheme (for reference)**

| Token | Hex | RGB |
|-------|-----|-----|
| primary | `#0B57D0` | rgb(11, 87, 208) |
| onPrimary | `#FFFFFF` | rgb(255, 255, 255) |
| primaryContainer | `#D3E3FD` | rgb(211, 227, 253) |
| secondary | `#00639B` | rgb(0, 99, 155) |
| secondaryContainer | `#C2E7FF` | rgb(194, 231, 255) |
| tertiary | `#146C2E` | rgb(20, 108, 46) |
| background | `#FFFFFF` | rgb(255, 255, 255) |
| surface | `#FFFFFF` | rgb(255, 255, 255) |
| surfaceContainerLowest | `#FFFFFF` | rgb(255, 255, 255) |
| surfaceContainerLow | `#F8FAFD` | rgb(248, 250, 253) |
| surfaceContainer | `#F0F4F9` | rgb(240, 244, 249) |
| surfaceContainerHigh | `#E9EEF6` | rgb(233, 238, 246) |
| surfaceContainerHighest | `#DDE3EA` | rgb(221, 227, 234) |
| outline | `#747775` | rgb(116, 119, 117) |
| outlineVariant | `#C4C7C5` | rgb(196, 199, 197) |

**Gallery Full M3 Dark ColorScheme (for reference)**

| Token | Hex | RGB |
|-------|-----|-----|
| primary | `#A8C7FA` | rgb(168, 199, 250) |
| background | `#131314` | rgb(19, 19, 20) |
| surface | `#131314` | rgb(19, 19, 20) |
| surfaceContainerLowest | `#0E0E0E` | rgb(14, 14, 14) |
| surfaceContainerLow | `#1B1B1B` | rgb(27, 27, 27) |
| surfaceContainer | `#1E1F20` | rgb(30, 31, 32) |
| surfaceContainerHigh | `#282A2C` | rgb(40, 42, 44) |
| surfaceContainerHighest | `#333537` | rgb(51, 53, 55) |
| outline | `#8E918F` | rgb(142, 145, 143) |

---

#### Typography

**Gallery** uses **Nunito** font (custom, bundled, 8 weights) with M3 baseline sizes. **OpenClaw** uses `FontFamily.Default` (Roboto) with iOS-inspired tighter sizes.

**Recommendation for Phase 1**: Keep OpenClaw's existing typography — its iOS-inspired scale is appropriate for chat density. Nunito is a Phase 2 polish option.

| Style | Gallery (sp) | OpenClaw (sp) | Recommendation |
|-------|-------------|---------------|----------------|
| displayLarge | 57 | 34 Bold | Keep OpenClaw |
| headlineLarge | 32 | 22 Bold | Keep OpenClaw |
| titleLarge | 22 | 17 SemiBold | Keep OpenClaw |
| titleMedium | 16 W500 | 16 Medium | Same |
| bodyLarge | 16 W400 | 17 Normal | Keep OpenClaw |
| bodyMedium | 14 W400 | 15 Normal | Keep OpenClaw |
| bodySmall | 12 W400 | 13 Normal | Keep OpenClaw |
| labelSmall | 11 W500 | 11 Medium | Same |

**Gallery custom variants (available for adoption if needed)**:
- `homePageTitleStyle`: 48sp, Medium, letterSpacing -1.0 — for hero/splash
- `emptyStateTitle`: 37sp, W400 — for empty screen headings
- `emptyStateContent`: 16sp, W400, line height 22sp

---

#### Animations

All animation values extracted from Gallery source files:

| Animation | Value | Source |
|-----------|-------|--------|
| RotationalLoader outer rotation duration | 2000ms | `RotationalLoader.kt` |
| RotationalLoader outer easing | `CubicBezierEasing(0.5f, 0.16f, 0f, 0.71f)` | `RotationalLoader.kt` |
| RotationalLoader inner scale duration | 1000ms | `RotationalLoader.kt` |
| RotationalLoader inner scale easing | `EaseInOut` | `RotationalLoader.kt` |
| RotationalLoader inner scale range | 1.0 → 0.4 | `RotationalLoader.kt` |
| Loading icon alpha range | 0.3 → 1.0 | `MessageBodyLoading.kt` |
| Loading icon alpha duration | 1000ms, `LinearEasing`, `RepeatMode.Reverse` | `MessageBodyLoading.kt` |
| Thinking panel expand/collapse | `expandVertically()` / `shrinkVertically()`, default spring ~400ms | `MessageBodyThinking.kt` |
| CollapsablePanel expand/collapse | `expandVertically()` / `shrinkVertically()`, default spring | `MessageBodyCollapsableProgressPanel.kt` |
| CollapsablePanel title transition | `slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()` | `MessageBodyCollapsableProgressPanel.kt` |
| First-init overlay enter | `fadeIn() + scaleIn(initialScale=0.9f)` | `ChatPanel.kt` |
| First-init overlay exit | `fadeOut() + scaleOut(targetScale=0.9f)` | `ChatPanel.kt` |
| Navigation slide forward | `tween(500, easing=EaseOutExpo)`, `SlideDirection.Left` | `GalleryNavGraph.kt` |
| Navigation slide back | `tween(500, easing=EaseOutExpo)`, `SlideDirection.Right` | `GalleryNavGraph.kt` |
| Audio amplitude tween | 100ms | `AudioAnimation.kt` |
| Image viewer slide-in | `slideInVertically { fullHeight } + fadeIn()` | `ChatView.kt` |
| Auto-scroll bottom threshold | < 90px from viewport end | `ChatPanel.kt` |
| Model init poll interval | 100ms | `LlmChatViewModel.kt` |
| Model init initial delay | 500ms | `LlmChatViewModel.kt` |
| Image limit banner duration | 3000ms | `ChatPanel.kt` |

---

#### Touch Targets

| Element | Minimum Size | Enforcement |
|---------|-------------|-------------|
| All buttons | 48×48dp | Material3 `minimumInteractiveComponentSize()` |
| Chips (QuickActionChips, SpaceSwitcher) | 44×36dp | `SuggestionChip` / `FilterChip` default height |
| Scroll gutter | ≥8dp | Standard scrollbar padding |

---

#### Error Messages (Exact Copy)

| Scenario | Message | Location |
|----------|---------|----------|
| Model not loaded / initializing | "Connecting to model..." | `RotationalLoader` overlay (first-init) |
| LiteRT model loading | AnimatedContent switching between `ChatPanel` and `ModelDownloadStatusInfoPanel` | `ChatPanel.kt` pattern |
| Session re-initialized after error | `ChatMessageWarning("Session re-initialized")` | Gallery error recovery pattern |
| Tool execution timeout | "Tool execution timed out" | OpenClaw `AgentRuntime.kt` (30s `withTimeoutOrNull`) |
| Voice STT unavailable | `MicOff` icon + disabled state | `VoiceInputButton.kt` existing |
| Error bubble Go-to-Settings | Make `TextButton` calling `onNavigateToSettings()` | `ErrorBubble` in `MessageBubble.kt` Phase 1 fix |

---

#### Empty States

| State | Content |
|-------|---------|
| New conversation (empty chat) | `WelcomeHero` — rounded card with vertical gradient, "Meet {modelName}" headline + subtitle |
| Below WelcomeHero | `SuggestedPrompts` — 6 hardcoded suggestions (Plan, Explain, Design, Write, Organize, Research) as horizontally scrollable `SuggestionChipCard` composables |

---

### Integration Checklist

#### Files to Create

| File | Lines (est.) | Source | Notes |
|------|-------------|--------|-------|
| `ui/components/ModelPickerChip.kt` | ~80 | Gallery `ModelPickerChip.kt` adapted | Replace Hilt ViewModel with `modelRouter` param |
| `ui/components/ModelPickerSheet.kt` | ~60 | Extracted from `ChatScreen.kt` L560–613 | `ModalBottomSheet` body for model list |
| `ui/components/QuickActionChips.kt` | ~120 | New — inspired by `SuggestedPrompts.kt` pattern | 8 predefined chips, staggered `AnimatedVisibility` |
| `ui/components/SpaceSwitcher.kt` | ~95 | New | Horizontal chip row for drawer |
| `ui/components/PerChatSettingsSheet.kt` | ~140 | Gallery config sheet adapted + ChatScreen stub | Temperature/topK/topP sliders, accelerator chips, tool toggle |
| `ui/components/ThinkingIndicator.kt` | ~60 | Extracted from `ChatScreen.kt` L910–961 | Gallery `expandVertically` + `drawBehind` border |
| `ui/components/InputBar.kt` | ~130 | Extracted from `ChatScreen.kt` `CleanInputBar` | Add `onVoiceMode` callback |
| `ui/components/RotationalLoader.kt` | ~50 | Gallery `RotationalLoader.kt` adapted | Replace icon resources with available Material icons |

#### Files to Modify

**`MainActivity.kt`** — Replace integer tab navigation with sealed class
- **Change**: `var currentTab by remember { mutableIntStateOf(3) }` → `var currentScreen by remember { mutableStateOf<MainScreen>(MainScreen.Chat) }`
- **Change**: `AnimatedContent(currentTab) { tab -> when(tab) { ... } }` → `AnimatedContent(currentScreen) { screen -> when(screen) { ... } }`
- **Change**: Keep only 5 `when` branches: `MainScreen.Chat`, `MainScreen.Settings`, `MainScreen.Files`, `MainScreen.Voice`, `MainScreen.Memory`
- **Change**: Delete all 13 removed screen cases
- **Add**: `sealed class MainScreen { object Chat; object Settings; object Files; object Voice; object Memory }`
- **Add**: `SpaceSwitcher` at top of `ModalDrawerSheet`
- **Add**: Nav links at bottom of drawer: Memory, Files, Settings → `currentScreen = MainScreen.X`
- **Estimated diff**: ~120 lines changed

**`ChatScreen.kt`** — Wire new components, fix duplicate composables
- **Add**: `ModelPickerChip` to `CleanTopBar` center slot — wired to `runtime.preferredModelId` StateFlow
- **Add**: `QuickActionChips` row above `CleanInputBar` — chips call `onNavigate(MainScreen.X)` or `runtime.sendMessage()`
- **Add**: `showModelPicker = true` trigger from `ModelPickerChip` tap — already has state var
- **Add**: `showChatSettings = true` trigger from `SettingsIcon` — already has state var
- **Wire**: `PerChatSettingsSheet` composable replacing commented-out content in `showChatSettings` branch
- **Fix**: Delete duplicate `MediaItem` data class (L60) — keep `MediaPreview.kt` sealed class
- **Fix**: Delete duplicate `MediaPreviewStrip` Row-based version (L1137) — keep `MediaPreview.kt` version
- **Add**: `onNavigate: (MainScreen) -> Unit` callback replacing `onNavigateToDashboard`
- **Estimated diff**: ~200 lines changed

**`MessageBubble.kt`** — Fix ToolResultBubble rendering
- **Fix**: Replace `Text(fontFamily = Monospace)` with `MarkdownText(markdown = displayText)` in `ToolResultBubble` (~L193)
- **Fix**: Remove monospace font application — `MarkdownText` handles code blocks natively
- **Add**: Tool name → icon mapping in `ToolCallBubble` (`task_manager` → `CheckCircle`, `travel_manager` → `Flight`, `web_search` → `Search`, etc.)
- **Add**: "Go to Settings" `TextButton` in `ErrorBubble` calling `onNavigateToSettings()`
- **Estimated diff**: ~20 lines changed

**`AgentRuntime.kt`** — Make `preferredModelId` reactive
- **Change**: `var preferredModelId: String? = null` → `private val _preferredModelId = MutableStateFlow<String?>(null)` + `val preferredModelId: StateFlow<String?> = _preferredModelId.asStateFlow()`
- **Add**: `fun setPreferredModel(id: String?) { _preferredModelId.value = id }`
- **Add**: `AgentEvent.Preparing` emitted immediately on `sendMessage()`, removed on first stream chunk (loading placeholder pattern)
- **Estimated diff**: ~15 lines changed

**`SettingsRepository.kt`** — Remove dead providers, add per-chat settings
- **Delete**: `getGroqKey()`, `getCerebrasKey()`, `getGroqModel()`, all Groq/Cerebras getters (~10 methods)
- **Add**: `getPerChatSettings(conversationId: String): PerChatSettings`
- **Add**: `savePerChatSettings(conversationId: String, settings: PerChatSettings)`
- **Add**: `purgePerChatSettings(conversationId: String)` — called from `ConversationManager.deleteConversation()`
- **Update**: `hasAnyProvider()` logic to check Gemini only
- **Estimated diff**: ~30 lines changed

**`OpenClawApp.kt`** — Remove dead providers
- **Delete**: Groq provider map entry (lines ~124–126)
- **Delete**: Cerebras provider map entry (lines ~127–128)
- **Keep**: `GeminiProvider` + `LiteRTProvider`
- **Estimated diff**: 4 lines deleted

**`MediaPreview.kt`** — Fix hardcoded dark colors
- **Fix**: Replace `ElectricViolet`, `DarkGlass` with `MaterialTheme.colorScheme` tokens in audio/file thumbnails
- **Estimated diff**: ~8 lines changed

**`ConversationManager.kt`** (or equivalent) — Cascade purge per-chat settings
- **Add**: Call `settingsRepository.purgePerChatSettings(conversationId)` inside `deleteConversation()`
- **Estimated diff**: ~3 lines changed

#### Files to Delete

| File | Reason |
|------|--------|
| `GroqProvider.kt` | Provider removed — dead code |
| `CerebrasProvider.kt` | Provider removed — dead code |
| `DashboardScreen.kt` | Screen removed per disposition |
| `TasksScreen.kt` | Screen removed per disposition |
| `NotesScreen.kt` | Screen removed per disposition |
| `DiscoverScreen.kt` | Screen removed per disposition |
| `CalendarScreen.kt` | Screen removed per disposition |
| `TravelScreen.kt` | Screen removed per disposition |
| `InsightsScreen.kt` | Screen removed per disposition |
| `BriefingScreen.kt` | Screen removed per disposition |
| `TeamScreen.kt` | Screen removed per disposition |
| `HabitsScreen.kt` | Screen removed per disposition |
| `DecisionsScreen.kt` | Screen removed per disposition |

#### Breaking Changes

**1. Integer tab navigation (17 tabs) → sealed class `MainScreen` (5 screens)**
- All `onNavigateTo*` callbacks on deleted screens become dead code — remove 30+ callback references
- URL deep-linking: not applicable (none exists in OpenClaw currently)
- Migration path: no user-visible change — users land on `MainScreen.Chat` as before

**2. Groq/Cerebras providers removed**
- Users with existing API keys: keys remain in `SharedPreferences` untouched (can be re-added in Phase 2 via custom provider)
- Migration path: show deprecation `Snackbar` on first launch if Groq/Cerebras keys are found in `SharedPreferences`

**3. `preferredModelId` now reactive (`StateFlow`)**
- Any code assuming synchronous read via `runtime.preferredModelId` must switch to `collectAsState()` or first value
- Audit confirms no code uses this as a synchronous value outside composables — no breakage expected

#### Migration Path

| Asset | Action |
|-------|--------|
| Existing conversations | No changes — Room schema unchanged |
| Existing spaces | No changes — JSON/Room storage intact |
| Existing model preferences | Keep in `SharedPreferences` |
| User model downloads (LiteRT `.task` files) | Still available in original location |
| Groq/Cerebras API keys | Kept in `SharedPreferences` with deprecation notice |

---

### LLM Integration Notes

Gallery is **100% on-device** (LiteRT LM, no cloud). OpenClaw uses Gemini API (cloud) + LiteRT (local). No Gallery LLM code is reusable directly.

**Adoptable Gallery LLM patterns (architecture, not code):**

| Pattern | Adopt | Notes |
|---------|-------|-------|
| `LlmModelHelper` interface (5-method contract) | Yes | Clean abstraction for multi-provider routing |
| `ResultListener` typealias: `(partial: String, done: Boolean, thinking: String?) -> Unit` | Yes | Maps to OpenClaw's streaming callback |
| Config system: `ConfigKeys` (MAX_TOKENS, TOPK, TOPP, TEMPERATURE, ACCELERATOR) | Yes | Direct input to `PerChatSettingsSheet` |
| `inProgress` + `preparing` dual-flag for loading states | Yes | `preparing=true` between send and first token; `inProgress=true` between first token and done |
| Error recovery: cleanup → reinitialize → `WarningMessage("Session re-initialized")` | Yes | Already partially in OpenClaw |
| Single-active-model lifecycle (cleanup before switching) | Yes | Already in `ModelDownloadManager` |
| Channel-based tool dispatch: `_actionChannel: Channel<AgentAction>` | Yes (Phase 2) | Decouples tool results from UI rendering |

**Config defaults (from Gallery `model_allowlist.json` / `LlmChatModelHelper.kt`):**

| Parameter | Default | Range |
|-----------|---------|-------|
| MAX_TOKENS | 1024 (Gallery) / 4096 (Gemma-3n) | 256–8192 |
| TOPK | 64 | 1–100 |
| TOPP | 0.95 | 0.0–1.0 |
| TEMPERATURE | 1.0 | 0.0–2.0 |
| ACCELERATOR | GPU | CPU / GPU / NPU |
| ENABLE_THINKING | false | boolean |

---

### Success Metrics

#### Code Quality
- [ ] Zero compiler errors / warnings
- [ ] All imports organized (no unused)
- [ ] Code style consistent with existing OpenClaw conventions
- [ ] No Hilt dependencies imported (`@HiltViewModel`, `@Inject` never used)
- [ ] All Gallery Firebase analytics calls stripped (`firebaseAnalytics?.logEvent(...)`)

#### Functional
- [ ] Unified chat screen loads on app startup (lands on `MainScreen.Chat`)
- [ ] Left drawer accessible via hamburger icon, swipe gesture
- [ ] Space switcher visible at drawer top, space selection updates active space
- [ ] Model picker chip shows current model name reactively (StateFlow)
- [ ] Model picker sheet opens with full model list, model switch persists
- [ ] Quick action chips visible above input bar, prompt chips send correctly
- [ ] Navigation chips (Voice, Files, Memory) navigate to correct screens
- [ ] Per-chat settings sheet opens, sliders update values, values persist across restart
- [ ] Tool results render with markdown formatting (bold, headers, lists visible)
- [ ] Error bubbles show "Go to Settings" button
- [ ] Groq/Cerebras deprecation notice shown if keys exist
- [ ] All 13 deleted screens inaccessible (no dead navigation paths)

#### Performance
- [ ] App startup: <3s cold start on mid-range device (6GB RAM)
- [ ] Chat screen first render: <100ms
- [ ] Model switch (chip tap to sheet close): <500ms
- [ ] Message streaming: token-by-token with no perceptible lag
- [ ] Memory footprint: <200MB baseline
- [ ] Auto-scroll during streaming: smooth, no scroll-jacking when user scrolls up

#### UX
- [ ] Drawer swipe gesture smooth (no jank)
- [ ] Model picker bottom sheet animation fluid (`skipPartiallyExpanded=true`)
- [ ] Quick action chips responsive (tap feedback immediate)
- [ ] `RotationalLoader` plays during model init overlay
- [ ] ThinkingIndicator auto-expands while `inProgress=true`
- [ ] All touch targets ≥48dp
- [ ] Dark/light theme consistent across all new components

---

## PHASE 2: FEATURE COMPLETION (4–6 weeks)

### Features

| # | Feature | Effort | Dependencies | Success Criteria |
|---|---------|--------|--------------|-----------------|
| 1 | **Smart chips with live data** — `QuickActionChips` show badge counts from `TaskManager`, unread notifications, etc. | 4h | `TaskManagerTool` accessible from composable layer | Badge count visible on relevant chips; updates on task CRUD |
| 2 | **Tool result intelligent rendering** — Group consecutive tool calls into a single `CollapsableToolPanel` (Gallery `MessageBodyCollapsableProgressPanel` pattern) | 1 day | Phase 1 tool rendering fix done | Multi-step tool runs show as "N tool calls" collapsed; expand shows each step |
| 3 | **Context window display + auto-summarize** — Show token usage indicator in TopBar; auto-summarize when >80% full | 2 days | Token counting API from LLM provider | Token usage visible; summary triggered automatically |
| 4 | **Streaming UI polish** — Message entrance animations `spring(StiffnessMediumLow)`, token fade-in, staggered `EaseOutExpo` chip entrance | 2h | Phase 1 `LazyColumn` stable keys | Entrance animations play smoothly on all message types |
| 5 | **Voice mode — waveform amplitude visualization** — `onRmsChanged` → animated bars in `VoiceInputButton`; `AudioAnimation.kt` shader (API 33+) for `VoiceConversationScreen` | 2h | Phase 1 voice screen intact | Waveform responds to live microphone amplitude; fallback Canvas on API <33 |
| 6 | **Voice mode — cancel-by-slide gesture** — `CancellationException` path in `HoldToDictate` pattern; 500ms stop delay | 2h | None | Sliding mic button off cancels recording without sending |
| 7 | **Voice mode — SpeechRecognizer config-change fix** — Move `SpeechRecognizer` lifecycle out of `remember {}` | 1–2h | None | Screen rotation during recording does not crash or restart recognition |
| 8 | **Auto-generated conversation titles** — After first exchange, call LLM with "summarize in 5 words" system prompt; update `ConversationEntity.title` | 4h | `AgentRuntime`, `ConversationManager` Room DAO | Conversation titles auto-generated after first message; shown in drawer |
| 9 | **Search conversations** — Full-text search across `ConversationEntity` + `MessageEntity` via Room FTS | 1 day | Room FTS5 schema migration | Search bar in drawer finds conversations by content |
| 10 | **Suggested follow-ups** — After each assistant response, show 2–3 follow-up prompt chips | 4h | Requires extra LLM call or prompt engineering | Follow-up chips appear after each response; tapping sends prompt |
| 11 | **Message actions** — Long-press bubble → bottom sheet: Copy, Regenerate, Delete, Share | 4h | `AgentRuntime.retryLastMessage()` (exists) | Long-press on any message shows action sheet with correct options |
| 12 | **Empty state conversation starters** — Replace `SuggestedPrompts` hardcoded cards with remotely-configurable starters or LLM-generated | 3h | Optional remote config or local JSON | Fresh conversation starters shown; can be updated without app release |
| 13 | **WebView tool result type** — `AgentEvent.WebViewResult(url)` rendered in embedded WebView; expand to full screen | 2 days | At least one tool produces HTML URLs | WebView results render inline; expand button opens full-screen modal |
| 14 | **Theme override** — User-settable light/dark/auto via `DataStore`-backed `ThemeSettings` (Gallery pattern) | 0.5 day | `DataStore` already in project | Theme toggle in Settings screen; persists across restarts |
| 15 | **Nunito font** — Bundle Nunito (OFL license) from Google Fonts; replace `FontFamily.Default` | 1 day | Font files in `res/font/` | UI renders with Nunito; clearly distinguishable from Roboto |
| 16 | **Raw audio clip recording** — `AudioRecorderPanel.kt` port; `ContentPart.Audio` type; base64 encoding for Gemini | 4–5h | Multimodal audio input from provider | User can record 30s audio clip; clip sent to LLM as audio content |
| 17 | **Animated tool execution status in TopBar** — While `AgentState.ExecutingTool(name)` active, show tool name pill in TopBar subtitle | 4h | Phase 1 `AgentState` tracking | Tool name visible in TopBar while tool runs; disappears on completion |

---

## APPENDIX

### Gallery Components Reusability Matrix

| Component | Reusable | Effort | Strategy |
|-----------|----------|--------|----------|
| ChatPanel (message list container) | YES | 3h | Add `nestedScroll`, upgrade message entrance animations |
| MessageBubble / variants | YES | 2h | Extract `ThinkingIndicator`; fix `ToolResultBubble` |
| MarkdownText | YES (keep OpenClaw's — superior) | 0h | No changes |
| InputBar (`CleanInputBar`) | YES | 2h | Extract to `InputBar.kt`, add `onVoiceMode` |
| VoiceInputButton | YES | 3h (Phase 2) | Phase 1: keep as-is. Phase 2: add waveform |
| SuggestedPrompts | PARTIAL | 0h | Keep for empty state; create separate `QuickActionChips` |
| ModelPickerChip | PARTIAL | 4h | Extract + restyle + download-state wiring |
| PerChatSettingsSheet | PARTIAL | 5h | Uncomment stub + wire `AgentRuntime` |
| WelcomeHero | YES | 0h | No changes |
| MediaPreviewStrip | PARTIAL | 2h | Dedup + theme token fix |
| Theme (Color + Theme) | YES | 0h | Keep OpenClaw's intentional iOS palette |
| GlassCard utilities | YES | 0.5h | Remove dead `backgroundOpacity` param |
| RotationalLoader | YES (copy) | 0.5h | Copy from Gallery; replace icon resources |
| MessageBubbleShape | YES (copy) | 0.5h | Copy from Gallery verbatim |
| Auto-scroll 90px threshold | YES (adopt) | 0.5h | Add threshold guard to existing scroll logic |
| Loading placeholder swap | YES (adopt) | 1h | Add `AgentEvent.Preparing` to runtime |
| ThinkingIndicator | YES (extract) | 1h | Move from ChatScreen private function to standalone |
| AudioAnimation (AGSL shader) | YES (Phase 2) | 2h | Phase 2 — API 33+ with Canvas fallback |
| NavigationDrawer | YES | 3h | Add SpaceSwitcher + nav links |
| QuickActionChips | NEW (create) | 4h | No Gallery equivalent |
| SpaceSwitcher | NEW (create) | 3h | No Gallery equivalent |

---

### Animation Values (Complete Reference)

| Animation | Duration | Easing | Other |
|-----------|----------|--------|-------|
| RotationalLoader outer rotation | 2000ms | `CubicBezierEasing(0.5f, 0.16f, 0f, 0.71f)` | `RepeatMode.Restart` |
| RotationalLoader inner scale | 1000ms | `EaseInOut` | 1.0→0.4, `RepeatMode.Reverse` |
| Loading icon alpha | 1000ms | `LinearEasing` | 0.3→1.0, `RepeatMode.Reverse` |
| Thinking panel expand | default spring | — | ~400ms |
| CollapsablePanel expand | default spring | — | ~400ms |
| CollapsablePanel title slide | default spring | — | `slideInVertically + fadeIn` |
| First-init overlay enter | default tween | — | `fadeIn + scaleIn(0.9)` ~300ms |
| Nav slide forward | 500ms | `EaseOutExpo` | `SlideDirection.Left` |
| Nav slide back | 500ms | `EaseOutExpo` | `SlideDirection.Right` |
| Nav modal slide up | default | — | `SlideDirection.Up` |
| Audio amplitude smoothing | 100ms | `tween` | `Animatable.animateTo()` |
| Auto-scroll threshold | N/A | N/A | < 90px from bottom |
| Image limit banner | 3000ms | N/A | Auto-dismiss |
| Model init polling | 100ms interval | N/A | 500ms initial delay |
| Reset session retry | 200ms | N/A | |
| AGSL wave strength (idle) | N/A | N/A | `wave_strength=0.036` |
| AGSL wave speed (idle) | N/A | N/A | `wave_speed=1.2` |
| AGSL wave frequency | N/A | N/A | `wave_frequency=4.0` |
| AGSL amplitude alpha | N/A | N/A | `graphicsLayer { alpha=0.8f }` |

---

### Known Issues and Mitigations

#### Critical Bugs

| # | Issue | Severity | Mitigation |
|---|-------|----------|------------|
| 1 | `MediaItem` dual definition (data class + sealed class) | HIGH | Fix first — unblocks `InputBar` extraction |
| 2 | `MediaPreviewStrip` duplicate with incompatible signatures | HIGH | Fix together with MediaItem fix — same file pass |

#### Performance Concerns

| # | Concern | Impact | Mitigation |
|---|---------|--------|------------|
| 1 | Every streaming token causes full `ChatUiState` recomposition | Medium — Compose smart-recomposition mitigates mostly | Split `streamingContent: String` into own `StateFlow`; collect only in active streaming message |
| 2 | Token accumulation via string concatenation (`"${old}${partial}"`) creates O(n) intermediate strings | Medium — 2000-token response = 2000 String allocations | Use `StringBuilder` in streaming path; convert to `String` only on `done=true` |
| 3 | `LazyColumn` uses positional keys (`itemsIndexed` without `key {}`) | Low — recomposition of all items when thinking→text transition occurs | Add `key { message.id }` to `itemsIndexed`; requires stable IDs on all `AgentEvent` types |

#### Architecture Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Keep sealed-class navigation (not Jetpack NavHost) | NavHost adds back-stack, route serialization, argument passing complexity for zero user-visible benefit on 5 screens. 12–16h migration cost with no UX improvement. |
| 2 | Do NOT adopt Hilt / Gallery ViewModels | OpenClaw uses OpenClawApp singleton + parameter injection. Adding Hilt would require rewriting all composable signatures, `Application`, and build config. Estimated 40h for no architectural benefit. |
| 3 | Keep OpenClaw's teal brand, NOT Gallery's Google blue | Intentional product design. OpenClaw is not Gallery — different brand identity. |
| 4 | PerChatSettings in SharedPreferences, not new Room table | Lightweight; no schema migration required. JSON serialization of `PerChatSettings` data class under key `per_chat_{conversationId}`. |
| 5 | Token batching deferred to Phase 2 | Every-token recomposition is acceptable for Phase 1. Debounce/conflate optimization improves smoothness but is not a correctness issue. |

---

### Effort Summary

#### Phase 1 Totals

| Category | Hours |
|----------|-------|
| Pre-work: fix critical bugs (items 1–3) | 1.25h |
| Adaptations to existing components | 21.5h |
| New components to create | 19h |
| Navigation refactor (sealed class + drawer) | 7h |
| State management cleanup (AgentRuntime, SettingsRepository) | 4h |
| File deletions + callback cleanup | 2–3h |
| Theme / design token work | 2 days (optional Nunito: +1 day) |
| **Total Phase 1** | **~76–100h** |

#### Phase 2 Totals

| Category | Hours |
|----------|-------|
| Streaming polish + animations | 4h |
| Voice polish (waveform, cancel, config-change fix) | 5–7h |
| Auto-generated titles | 4h |
| Search conversations | 1 day |
| Suggested follow-ups | 4h |
| Message actions | 4h |
| Context window display | 2 days |
| CollapsableToolPanel | 1 day |
| Smart chips with live data | 4h |
| WebView tool results | 2 days |
| Theme override (DataStore) | 0.5 day |
| Nunito font (if not Phase 1) | 1 day |
| Raw audio clip recording | 4–5h |
| Animated tool status in TopBar | 4h |
| **Total Phase 2** | **~60–80h** |

---

**This spec is EXECUTABLE.** Every component, file, line estimate, animation value, color token, and architectural decision is documented above with its source audit. Pass to the implementation team with confidence that every detail is specified.
