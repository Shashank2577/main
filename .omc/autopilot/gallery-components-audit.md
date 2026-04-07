# Gallery UI Components Audit
_Analyst: ui-components-analyst | Date: 2026-04-06_

---

## Methodology

All OpenClaw source files were read directly. Gallery implementation details are drawn from `gallery-reference.md` (cloned 2026-04-06) and the spec. Effort estimates are in developer-hours assuming a single mid-senior Android/Compose engineer.

---

## Component-by-Component Audit

---

### 1. ChatPanel (message list container)

**Gallery pattern:** `ChatPanel.kt` — `LazyColumn` (messages) + input row + `SnackbarHost`. Uses `nestedScroll` modifier with a custom `NestedScrollConnection` that hides the input bar when scrolling up. Message entrance uses `spring(stiffness = Spring.StiffnessMediumLow)`.

**OpenClaw current:** `ChatScreen.kt` lines 414–483. A `LazyColumn` inside a `Box(weight(1f))` with `rememberLazyListState`. No `nestedScroll`. Uses `animateScrollToItem` on events list change. Has scroll-to-bottom `IconButton` overlay. Message animations use `animateContentSize` in individual bubbles, not `AnimatedVisibility` at the list level.

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | Add `nestedScroll`/`NestedScrollConnection` to hide input on scroll-up. Upgrade message entrance to `AnimatedVisibility(spring(StiffnessMediumLow))`. Both are additive; no structural change needed. |
| **Effort** | 3h |
| **Conflicts** | None. OpenClaw `LazyColumn` + `Scaffold` + `SnackbarHost` already mirrors Gallery's structure exactly. |

---

### 2. MessageBubble / MessageBody variants

**Gallery pattern:** Multiple `MessageBody*.kt` composables — `MessageBodyText`, `MessageBodyImage`, `MessageBodyAudioClip`, `MessageBodyLoading`, `MessageBodyThinking`, `MessageBodyError`, `MessageBodyWarning`, `MessageBodyBenchmark`. Each is a standalone composable dispatched from the parent chat item composable.

**OpenClaw current:** `MessageBubble.kt` — single entry point `MessageBubble(event: AgentEvent)` dispatching to 8 internal sub-composables: `UserBubble`, `AssistantBubble`, `ToolCallBubble`, `ToolResultBubble`, `ErrorBubble`, `StreamChunkBubble`, `ModelBadge`, `EscalationBadge`. All use `MaterialTheme.colorScheme` tokens — fully theme-aware. Long-press copy to clipboard on user + assistant bubbles. Tool result bubble has expand/collapse for truncated output.

| Field | Value |
|-------|-------|
| **Reusable** | YES — architecture matches Gallery's dispatch pattern |
| **Adaptation needed** | Gallery's `MessageBodyThinking` uses animated bouncing dots with `spring` physics; OpenClaw's `ThinkingIndicator` (in `ChatScreen.kt` L910–961) already has 3-dot infinite animation using `EaseInOutSine` + `RepeatMode.Reverse` — but it is a private function in `ChatScreen`, not a standalone composable. Extract to a standalone `ThinkingIndicator.kt` in `ui/components/`. Gallery's `MessageBodyBenchmark` is not needed (no benchmark feature). |
| **Effort** | 2h (extract + refine ThinkingIndicator) |
| **Conflicts** | None. OpenClaw's `AgentEvent` type hierarchy already covers all needed message variants. |

---

### 3. MarkdownText

**Gallery pattern:** Gallery uses a third-party Markdown library (compose-markdown or similar). Simple inline rendering — bold, italic, code spans, code blocks, links.

**OpenClaw current:** `MarkdownText.kt` — fully custom Kotlin implementation. Supports: bold (`**`), italic (`*`), inline code (`` ` ``), fenced code blocks (` ``` `) with language label, headings H1–H3, unordered + ordered lists, bare URLs, `[text](url)` links, and **Mermaid diagrams via WebView**. Theme-aware (all colors from `MaterialTheme.colorScheme`). Uses `ClickableText` for link handling via `LocalUriHandler`. Custom `parseMarkdownBlocks` + `parseInlineMarkdown` functions — no external dependency.

| Field | Value |
|-------|-------|
| **Reusable** | YES — OpenClaw's is MORE capable than Gallery's |
| **Adaptation needed** | None. Keep as-is. The Mermaid diagram support via WebView is a unique capability Gallery doesn't have. |
| **Effort** | 0h |
| **Conflicts** | None. |

---

### 4. InputBar (text + send + attach + voice)

**Gallery pattern:** `MessageInputText` — `BasicTextField`, send button, audio recorder button, image attach button. `AudioRecorderPanel` is an expandable bottom panel with waveform animation that appears on mic tap. Gallery hides the input bar on scroll-up via `NestedScrollConnection`.

**OpenClaw current:** `CleanInputBar` composable in `ChatScreen.kt` L999–1129. Row layout: `[Attach(+)] [Settings(Tune)] [BasicTextField(weight=1)] [Voice|Send|Stop]`. `BasicTextField` with custom placeholder, `maxLines=5`, `SolidColor` cursor. Voice via `VoiceInputButton` (hold-to-record with `SpeechRecognizer`). Stop button when `isRunning`. Send button when text is non-empty. The `onSettings` slot opens a `ModalBottomSheet` for per-chat LLM overrides.

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | (1) Gallery's `AudioRecorderPanel` uses a waveform amplitude animation driven by `onRmsChanged` — OpenClaw's `VoiceInputButton` only scales the button. Worth adding a live waveform or animated equalizer bars in a Phase 2 polish pass. (2) Extract `CleanInputBar` from `ChatScreen.kt` into its own file `ui/components/InputBar.kt` for cleanliness. |
| **Effort** | 2h (extraction + optional waveform polish) |
| **Conflicts** | `MediaItem` sealed class is defined twice: once in `ChatScreen.kt` (data class form, L60) and once in `MediaPreview.kt` (sealed class form, L231). This internal conflict must be resolved before extraction — deduplicate to `MediaPreview.kt`'s sealed class. |

---

### 5. VoiceInputButton

**Gallery pattern:** `AudioRecorderPanel.kt` — expandable panel that slides up from input area, shows animated waveform bars driven by microphone amplitude (`onRmsChanged` callback), records via `MediaRecorder`, and sends the audio file to the LLM. Gallery does not use `SpeechRecognizer` (STT) — it sends raw audio.

**OpenClaw current:** `VoiceInputButton.kt` — 40dp circular button using Android `SpeechRecognizer` (cloud STT, returns text). Hold-to-record with `tryAwaitRelease()`. Animates scale with spring on listening. Shows `GraphicEq` icon normally, `Mic` when recording, `MicOff` when unavailable. Also includes `TtsHelper` class for TTS. No waveform animation.

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | (1) Add `onRmsChanged`-driven waveform animation (animated bars behind the button circle) for visual feedback during recording. (2) Move `TtsHelper` to a separate `TtsHelper.kt` file — it is unrelated to the input button UI. |
| **Effort** | 3h |
| **Conflicts** | None. STT approach difference (OpenClaw: SpeechRecognizer → text; Gallery: MediaRecorder → audio bytes) is intentional. OpenClaw's approach is correct for cloud LLMs (Gemini accepts text). |

---

### 6. SuggestedPrompts / QuickActionChips

**Gallery pattern:** No direct Gallery equivalent. Gallery HomeScreen uses `LazyRow` of task cards with staggered `AnimatedVisibility(EaseOutExpo)` entrance. The spec's `QuickActionChip` contract references this stagger pattern.

**OpenClaw current:** `SuggestedPrompts.kt` — horizontal `Row` with `horizontalScroll`, renders `SuggestionChipCard` composables (120dp wide cards with icon + title + subtitle). Each card has press-scale spring animation. 6 hardcoded suggestions (Plan, Explain, Design, Write, Organize, Research). Shown only when chat is empty.

| Field | Value |
|-------|-------|
| **Reusable** | PARTIAL |
| **Adaptation needed** | The spec requires `QuickActionChips` (always-visible above InputBar, distinct from `SuggestedPrompts` which is empty-state only). The two components serve different purposes: `SuggestedPrompts` = empty-state starter cards; `QuickActionChips` = persistent contextual action chips. CREATE `QuickActionChips.kt` as a new composable using Gallery's staggered `AnimatedVisibility` entrance (EaseOutExpo, 100ms offset per chip). `SuggestedPrompts` can stay as-is for the empty state. |
| **Effort** | 4h (new QuickActionChips component + stagger animation) |
| **Conflicts** | None — the two components coexist in different positions in the chat layout. |

---

### 7. ModelPickerChip (TopBar model selector)

**Gallery pattern:** `ModelPickerChip.kt` — pill-shaped `Row` with `CircleShape`, `surfaceContainerHigh` background. Contains: status icon, `AnimatedVisibility(CircularProgressIndicator)` when initializing, model name text with `MiddleEllipsis`, `ArrowDropDown` icon. On tap → `ModalBottomSheet(skipPartiallyExpanded=true)` with scrollable model list. Wired to `@HiltViewModel`.

**OpenClaw current:** `CleanTopBar` in `ChatScreen.kt` L747–851. Center slot is a `Row(weight(1f))` with clickable model name text + `ChevronRight` icon. `ModalBottomSheet` model picker at L560–613 shows "Auto" + all `modelRouter.getAvailableModels()` as `ModelPickerCard` composables. Already has: provider name, vision flag, context window size in subtitle. Already wires to `runtime.preferredModelId`.

| Field | Value |
|-------|-------|
| **Reusable** | PARTIAL |
| **Adaptation needed** | Extract the center-slot model clickable into a standalone `ModelPickerChip.kt` composable matching Gallery's pill shape (CircleShape border, surfaceContainerHigh background). Add `AnimatedVisibility(CircularProgressIndicator)` for LiteRT initialization state (OpenClaw has `ModelDownloadManager` but it is not yet wired to the chip). The `ModalBottomSheet` picker in ChatScreen can be extracted to `ModelPickerSheet.kt`. |
| **Effort** | 4h (extraction + pill styling + download-state wiring) |
| **Conflicts** | No Hilt conflict — OpenClaw passes `modelRouter` directly as a parameter (existing pattern). |

---

### 8. PerChatSettingsSheet

**Gallery pattern:** Gallery's config button on `ModelPageAppBar` opens a per-task settings bottom sheet (temperature, top-p, etc.).

**OpenClaw current:** `showChatSettings` state + stub `ModalBottomSheet` in `ChatScreen.kt` L616–640. `ChatSettingsSheetContent` composable exists (L643–717) with temperature/topK/topP sliders, accelerator `FilterChip` row, and tool-calling checkbox — but is currently commented out with a `// TODO` note. The data class `ChatSettings` is referenced but the wiring to `AgentRuntime` is not yet complete.

| Field | Value |
|-------|-------|
| **Reusable** | PARTIAL |
| **Adaptation needed** | (1) Uncomment and wire `ChatSettingsSheetContent` to `AgentRuntime`. (2) Extract to standalone `PerChatSettingsSheet.kt`. (3) Implement `AgentRuntime.chatSettings` mutable field. |
| **Effort** | 5h (wiring AgentRuntime + extraction) |
| **Conflicts** | `ChatSettings` data class must be added to `llm/` package — verify it doesn't conflict with existing `PerChatSettings` spec contract. |

---

### 9. WelcomeHero (empty chat state)

**Gallery pattern:** No direct equivalent — Gallery shows a model download prompt or benchmark entry in empty state.

**OpenClaw current:** `WelcomeHero` in `ChatScreen.kt` L859–902. Rounded card with vertical gradient (`WelcomeGradientStart` → `WelcomeGradientEnd`), "Meet {modelName}" headline, descriptive subtitle. Well-designed; no changes needed.

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | None. |
| **Effort** | 0h |
| **Conflicts** | None. |

---

### 10. MediaPreviewStrip

**Gallery pattern:** Gallery supports image attachment in `MessageInputText`. No multi-type media preview strip.

**OpenClaw current:** `MediaPreview.kt` — `MediaPreviewStrip` using `LazyRow` with 72dp thumbnails. `MediaThumbnail` handles Image (via Coil `AsyncImage`), Video (thumbnail + play overlay), Audio (gradient background + icon), File (dark glass + file-type icon). Animated remove button (spring scale). Also defined redundantly in `ChatScreen.kt` as a simpler `Row`-based version — **duplication bug**.

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | Resolve the `MediaItem` + `MediaPreviewStrip` duplication between `MediaPreview.kt` and `ChatScreen.kt`. The `MediaPreview.kt` version (sealed class, `LazyRow`, Coil) is superior and should be the canonical one. Delete the duplicate in `ChatScreen.kt`. Also: `MediaPreview.kt` uses hardcoded dark-themed colors (`ElectricViolet`, `DarkGlass`) in the audio/file thumbnails — replace with `MaterialTheme.colorScheme` tokens for proper light/dark support. |
| **Effort** | 2h (deduplication + theme token fix) |
| **Conflicts** | `MediaItem` in `ChatScreen.kt` is a data class; in `MediaPreview.kt` it is a sealed class with subtypes. `uriToContentPart` in `ChatScreen.kt` takes the data class form. Resolution: keep sealed class from `MediaPreview.kt`, update `ChatScreen.kt` to construct the correct subtype. |

---

### 11. Theme System (Color.kt, Theme.kt)

**Gallery pattern:** Gallery uses Material3 dynamic color on Android 12+ with `dynamicLightColorScheme`/`dynamicDarkColorScheme`. Falls back to a fixed Gallery palette (purple primary, teal secondary). Uses `surfaceContainerHigh` extensively.

**OpenClaw current:** `Color.kt` — comprehensive two-tier palette: brand teal (`#2AC4A0`) as tertiary, neutral gray scale as primary/secondary (iOS-inspired). Full light + dark `ColorScheme` instances. Both light and dark schemes are fully specified. `AppShapes` with `RoundedCornerShape` sizes 6→28dp. No dynamic color. `Theme.kt` correctly sets transparent status bar + system bar appearance via `WindowCompat`.

| Field | Value |
|-------|-------|
| **Reusable** | YES — no changes needed |
| **Adaptation needed** | OpenClaw's theme is intentionally iOS-style minimal (white/black, teal accent). This is a deliberate design choice from the spec. Do NOT adopt Gallery's purple palette or dynamic color. The `surfaceContainerHigh` usage in OpenClaw already matches Gallery's surface elevation pattern. |
| **Effort** | 0h |
| **Conflicts** | None. |

---

### 12. GlassCard / ClayCard utilities

**Gallery pattern:** Gallery has `GlitteringShapesLoader` and `RotationalLoader` for loading states. No glass-card utility.

**OpenClaw current:** `ClayCard.kt` provides `GlassCard` composable, `Modifier.glassSurface()`, `Modifier.glowShadow()`, `Modifier.neonBorder()`. All use `MaterialTheme.colorScheme` tokens. Clean, reusable. The `backgroundOpacity` parameter on `GlassCard` and `glassSurface` is declared but unused (dead parameter).

| Field | Value |
|-------|-------|
| **Reusable** | YES |
| **Adaptation needed** | Remove the unused `backgroundOpacity` parameter from `GlassCard` and `glassSurface`. Minor cleanup. |
| **Effort** | 0.5h |
| **Conflicts** | None. |

---

### 13. Components to CREATE (not in OpenClaw yet)

| Component | Spec Reference | Effort |
|-----------|---------------|--------|
| `ModelPickerChip.kt` (standalone pill composable) | spec §5, gallery-ref §5 | 4h |
| `QuickActionChips.kt` (persistent chip strip above InputBar) | spec §6.5 | 4h |
| `SpaceSwitcher.kt` (drawer space selector) | spec §5 | 3h |
| `PerChatSettingsSheet.kt` (extracted + wired) | spec §6.4 | 5h |
| `ThinkingIndicator.kt` (extracted from ChatScreen) | gallery-ref §11 | 1h |
| `InputBar.kt` (extracted from ChatScreen) | gallery-ref §4 | 2h |

**Total new component effort: 19h**

---

## Summary Tables

### Components by Reusability

| Component | File | Reusable | Effort |
|-----------|------|----------|--------|
| ChatPanel (message list) | ChatScreen.kt | YES | 3h |
| MessageBubble | MessageBubble.kt | YES | 2h |
| MarkdownText | MarkdownText.kt | YES | 0h |
| CleanInputBar | ChatScreen.kt | YES (extract) | 2h |
| VoiceInputButton | VoiceInputButton.kt | YES | 3h |
| WelcomeHero | ChatScreen.kt | YES | 0h |
| Theme (Color + Theme) | ui/theme/*.kt | YES | 0h |
| GlassCard utilities | ClayCard.kt | YES | 0.5h |
| ModelPickerChip (center slot) | ChatScreen.kt | PARTIAL (extract+restyle) | 4h |
| PerChatSettingsSheet | ChatScreen.kt | PARTIAL (wire AgentRuntime) | 5h |
| SuggestedPrompts | SuggestedPrompts.kt | PARTIAL (keep as-is, add QuickActionChips) | 0h |
| MediaPreviewStrip | MediaPreview.kt + ChatScreen.kt | PARTIAL (dedup + theme fix) | 2h |

### Components to Build Fresh

| Component | Reason | Effort |
|-----------|--------|--------|
| `QuickActionChips.kt` | Does not exist; spec requires persistent chip strip | 4h |
| `SpaceSwitcher.kt` | Does not exist; drawer space selection | 3h |
| `ThinkingIndicator.kt` | Exists inline in ChatScreen; must be extracted | 1h |
| `InputBar.kt` | Exists inline in ChatScreen; must be extracted | 2h |

---

## Effort Summary

| Category | Hours |
|----------|-------|
| Adaptations to existing components | 21.5h |
| New components to create | 19h |
| **Total Phase 1 UI component work** | **40.5h** |

---

## Phase 1 vs Phase 2 Recommendation

### Phase 1 (MVP — required for spec AC-01 through AC-13)

1. **Extract + restyle `ModelPickerChip`** — pill shape, `ModalBottomSheet` picker, download-state wiring. (4h)
2. **Create `QuickActionChips.kt`** — 8 predefined chips above InputBar. (4h)
3. **Extract `InputBar.kt` + `ThinkingIndicator.kt`** from ChatScreen. (3h)
4. **Add `nestedScroll` hide-on-scroll** to ChatPanel. (1h)
5. **Resolve `MediaItem` duplication** between ChatScreen + MediaPreview. (2h)
6. **Fix MediaPreview hardcoded dark colors** with theme tokens. (1h)
7. **Create `SpaceSwitcher.kt`** for drawer. (3h)
8. **Wire `PerChatSettingsSheet`** — uncomment + connect to AgentRuntime. (5h)

**Phase 1 subtotal: ~23h**

### Phase 2 (Polish)

1. Waveform animation in `VoiceInputButton` (Gallery `AudioRecorderPanel` pattern). (3h)
2. Staggered EaseOutExpo entrance animations on `QuickActionChips`. (2h)
3. `spring(StiffnessMediumLow)` message entrance animations in `LazyColumn`. (2h)
4. Remove dead `backgroundOpacity` parameter from `GlassCard`. (0.5h)
5. `TtsHelper` extraction to standalone file. (0.5h)
6. `TextInputHistorySheet` (prompt history — Gallery `TextInputHistorySheet` pattern). (4h)

**Phase 2 subtotal: ~12h**

---

## Key Conflicts and Blockers

| # | Issue | Severity | Resolution |
|---|-------|----------|------------|
| 1 | `MediaItem` defined as both `data class` (ChatScreen.kt:60) and `sealed class` (MediaPreview.kt:231) | HIGH | Delete data class in ChatScreen; update `mimeTypeToMediaItem()` to construct sealed subtype |
| 2 | `MediaPreviewStrip` defined twice (ChatScreen.kt:1137 and MediaPreview.kt:48) with incompatible signatures | HIGH | Delete ChatScreen version; use MediaPreview.kt as canonical |
| 3 | `PerChatSettingsSheet` content is commented out with TODO — `AgentRuntime.chatSettings` field not yet implemented | MEDIUM | Implement `chatSettings: ChatSettings` field on `AgentRuntime` before enabling the sheet |
| 4 | `ThinkingIndicator` has a dead branch `isInitializing = false` with commented-out `AgentEvent.InitializingModel` | LOW | Either add `InitializingModel` to `AgentEvent` or remove dead branch |
| 5 | `CleanInputBar` mixes `onSettings` (per-chat overrides) and `onAttach` (file picker) — no `onVoiceMode` for full voice screen | LOW | Add `onVoiceMode` callback when extracting `InputBar.kt` |

---

## Verdict

OpenClaw's existing UI component layer is **structurally sound and closely mirrors Gallery's architecture**. The message rendering pipeline (`MessageBubble` → `MarkdownText`), the chat layout (`LazyColumn` + `BasicTextField` + `SnackbarHost`), and the theme system are all Phase 1-ready without structural changes. The primary work is **extraction** (moving inline composables to standalone files), **wiring** (connecting `PerChatSettingsSheet` and download state to `AgentRuntime`), and **creation** of two missing components (`QuickActionChips`, `SpaceSwitcher`). No Gallery ViewModel dependencies need to be copied.
