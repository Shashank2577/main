# Gallery Streaming & Real-Time Rendering Audit

**Source**: google-ai-edge/gallery (GitHub)
**Date**: 2026-04-06
**Auditor**: streaming-agent

---

## 1. Streaming Architecture Overview

Gallery uses a **token-by-token callback streaming** model driven by `LlmModelHelper.runInference()`. The runtime calls a `ResultListener` lambda on every partial token, and the ViewModel mutates a `StateFlow<ChatUiState>` on each callback. Compose reacts to state changes via `collectAsState()`, re-rendering only the message that changed.

### Core Data Flow

```
LiteRT LLM Runtime
  → ResultListener(partialResult, done, partialThinkingResult)
  → LlmChatViewModelBase.generateResponse()
      → updateLastTextMessageContentIncrementally()  // or updateLastThinkingMessageContentIncrementally()
          → _uiState.update { copy(messagesByModel = ...) }
  → ChatUiState (StateFlow)
  → ChatPanel (collectAsState)
  → LazyColumn itemsIndexed
  → MessageBodyText / MessageBodyThinking
```

---

## 2. Streaming Implementations

### 2.1 Text Token Streaming — `LlmChatViewModelBase.generateResponse()`

**File**: `ui/llmchat/LlmChatViewModel.kt`

**Mechanism**: No batching. Every token callback from the runtime immediately:
1. Removes the `ChatMessageLoading` placeholder on first token
2. Appends `partialResult` to the accumulated `ChatMessageText.content` via string concatenation: `"${lastMessage.content}${partialContent}"`
3. Calls `processLlmResponse()` which only replaces `\\n` → `\n`
4. Replaces the last message in the list with a new `ChatMessageText` instance (remove + add)
5. Calls `_uiState.update { ... }` triggering Compose recomposition

**Token batch size**: 1 (no batching — every callback triggers a full state update)

**Key behavior**: On first token, `setPreparing(false)` is called, toggling the loading state. The `inProgress` flag stays `true` until `done == true`.

**Control token filtering**: Tokens starting with `"<ctrl"` are silently dropped.

### 2.2 Thinking Token Streaming — `updateLastThinkingMessageContentIncrementally()`

**File**: `ui/common/chat/ChatViewModel.kt`

Same mechanism as text streaming but targets `ChatMessageThinking`. The thinking message is created with `inProgress = true` and updated to `inProgress = false` when:
- The first non-thinking token arrives (thinking phase ends)
- `done == true` (stream completes)

**Thinking state machine in `generateResponse()`**:
- `partialThinkingResult != null && isNotEmpty` → update/create THINKING message
- `partialThinkingResult` becomes null/empty → seal thinking, create TEXT message
- Stream done → force-seal any open THINKING message

### 2.3 Loading Placeholder — `ChatMessageLoading`

Added immediately when `generateResponse()` starts (before inference begins). Removed on first token callback. Has an `extraProgressLabel` field for tool-use status (animated via `AnimatedContent` with `fadeIn/fadeOut`).

### 2.4 Streaming Message State Map

`ChatUiState` has two separate maps:
- `messagesByModel: Map<String, MutableList<ChatMessage>>` — committed messages
- `streamingMessagesByModel: Map<String, ChatMessage>` — currently streaming message (used by agent chat for intermediate state)

The standard LLM chat path **only uses `messagesByModel`** (not `streamingMessagesByModel`). The streaming map is available as an extension point for more complex flows.

---

## 3. Animation Patterns

### 3.1 RotationalLoader (Primary Loading Indicator)

**File**: `ui/common/RotationalLoader.kt`
**Used in**: `MessageBodyLoading`, `ModelDownloadingAnimation`, first-init overlay in `ChatPanel`

**Animation composition**:
- **Outer rotation**: `CubicBezierEasing(0.5f, 0.16f, 0f, 0.71f)`, duration **2000ms**, `RepeatMode.Restart`
- **Inner scale**: `EaseInOut`, duration **1000ms**, scale 1.0 → 0.4, `RepeatMode.Reverse`
- Structure: 2×2 `LazyVerticalGrid` of icons, counter-rotating each icon against the outer rotation

**Easing**: Custom cubic bezier `(0.5, 0.16, 0, 0.71)` — produces a fast-start, slow-middle, fast-end rotation feel.

### 3.2 Loading Indicator Icon Alpha — `MessageBodyLoading`

**File**: `ui/common/chat/MessageBodyLoading.kt`

- `animateFloat` 0.3f → 1.0f, duration **1000ms**, `LinearEasing`, `RepeatMode.Reverse`
- Label: "breathing" icon alpha effect for tool-use progress label
- Uses `AnimatedContent(extraProgressLabel, fadeIn/fadeOut)` for label transitions

### 3.3 Thinking Panel — `MessageBodyThinking`

**File**: `ui/common/chat/MessageBodyThinking.kt`

- `AnimatedVisibility(visible = isExpanded)` with `expandVertically()` / `shrinkVertically()`
- **No explicit duration specified** — uses Compose default spring (~400ms)
- Auto-expands while `inProgress = true`; stays expanded until user collapses
- Left border drawn via `drawBehind` with `strokeWidth = 2.dp`

### 3.4 Collapsable Progress Panel — `MessageBodyCollapsableProgressPanel`

**File**: `ui/common/chat/MessageBodyCollapsableProgressPanel.kt`

- Panel expand/collapse: `expandVertically()` / `shrinkVertically()` — default spring
- Title transition: `slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()` — default spring
- Progress state: `CircularProgressIndicator(size=16.dp, strokeWidth=2.dp)` while `inProgress`, switches to `doneIcon` when complete

### 3.5 First-Init Overlay — `ChatPanel`

**File**: `ui/common/chat/ChatPanel.kt`

```kotlin
AnimatedVisibility(
  isFirstInitializing,
  enter = fadeIn() + scaleIn(initialScale = 0.9f),
  exit = fadeOut() + scaleOut(targetScale = 0.9f),
)
```
**No explicit duration** — uses Compose default tween (~300ms)

### 3.6 Audio Recording Animation — `AudioAnimation`

**File**: `ui/common/AudioAnimation.kt`
**Requires**: Android 13+ (TIRAMISU) for `RuntimeShader`

- Full-screen AGSL shader animation using `Canvas` + `ShaderBrush`
- Idle mode: sinusoidal wave, `wave_strength=0.036`, `wave_speed=1.2`, `wave_frequency=4.0`
- Recording mode: 1D Perlin noise driven by amplitude, scaled with `pow(amplitude, 0.5)`
- Amplitude animation: `Animatable.animateTo()` with `tween(100ms)` — very responsive
- Time uniform: `withFrameMillis` loop (frame-locked, no fixed interval)
- Slide-in entry: `spring(stiffness=Spring.StiffnessLow)` + `fadeIn(spring(stiffness=Low))`
- Exit: `fadeOut()`
- Applied alpha: `graphicsLayer { alpha = 0.8f }`

### 3.7 Image Viewer Slide-In — `ChatView`

```kotlin
enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn()
exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
```
**No explicit duration** — default spring

### 3.8 Model Download/Init Transition — `ChatView`

```kotlin
AnimatedContent(
  targetState = curModelDownloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
)
```
Switches between `ChatPanel` and `ModelDownloadStatusInfoPanel` with default crossfade.

### 3.9 Auto-scroll During Streaming — `ChatPanel`

**File**: `ui/common/chat/ChatPanel.kt`

Three `LaunchedEffect` triggers:
1. IME visibility change → `animateScrollToItem(last, offset=1000000)`
2. `messages.size` or `lastMessage.type` change → `animateScrollToItem(last)`
3. Content streaming (text change) → scroll only if within **90px of bottom** (threshold prevents scroll-jacking during manual scroll)

```kotlin
val canScroll =
  lastVisibleItem.index == messages.size - 1 &&
    lastVisibleItem.offset + lastVisibleItem.size - listState.layoutInfo.viewportEndOffset < 90
```

---

## 4. Error States During Streaming

**File**: `ui/llmchat/LlmChatViewModel.kt` — `handleError()`

On error:
1. Remove loading message
2. Add `ChatMessageError` (renders in `MessageBodyError`)
3. Clean up model → re-initialize (async)
4. Add `ChatMessageWarning("Session re-initialized")`

The `errorListener` lambda in `generateResponse()` calls `setInProgress(false)` + `setPreparing(false)` immediately, unblocking the input bar.

**Stop/cancel**: `stopResponse()` removes any pending loading message, calls `setInProgress(false)`, then delegates to `runtimeHelper.stopResponse()`.

---

## 5. Performance Characteristics

### Recomposition Analysis

| Update Frequency | Trigger | Recomposition Scope |
|---|---|---|
| Every token | `_uiState.update { copy(messagesByModel=...) }` | Full `ChatUiState` → entire `ChatPanel` |
| Message added | Same path | Same |
| inProgress change | `_uiState.update { copy(inProgress=...) }` | Same |

**Critical issue**: Every token causes the entire `ChatUiState` StateFlow to emit a new value. `ChatPanel` calls `collectAsState()` on the entire state, so the full panel recomposes on every token. Compose's smart recomposition will skip unchanged items, but the `LazyColumn` and all `itemsIndexed` lambdas are re-evaluated.

**Memory**: Each token appends to a String via concatenation (`"${existing}${partial}"`). For a 2000-token response, this creates ~2000 intermediate String objects — no StringBuilder optimization.

**No explicit `key()` in `itemsIndexed`**: Gallery uses positional keys by default in `itemsIndexed`. This means adding messages mid-list (e.g., thinking before text) will cause all subsequent items to recompose.

### Accessibility

`MessageBodyText` uses `semantics { liveRegion = LiveRegionMode.Polite }` only when `!inProgress` — preventing accessibility spam during streaming. This is a good pattern to preserve.

---

## 6. Integration Complexity Assessment

### Pattern 1: Token Streaming Core (ViewModel + StateFlow)
- **Reusability**: PARTIAL
- **What to adopt**: The `resultListener` callback signature `(String, Boolean, String?)`, the thinking/text state machine logic, the `setInProgress`/`setPreparing` dual-flag pattern
- **Modifications needed**: Replace string concatenation with StringBuilder; split `inProgress` and `streamingContent` into separate StateFlow to avoid full-panel recomposition on every token
- **Effort**: 3–4 hours

### Pattern 2: RotationalLoader
- **Reusability**: YES
- **What to adopt**: Direct copy — no modifications needed. The icon resources (`four_circle`, `circle`, `double_circle`, `pantegon`) and gradient colors from `customColors` are required.
- **Exact timings**: outer=2000ms CubicBezier(0.5,0.16,0,0.71), inner=1000ms EaseInOut
- **Effort**: 0.5 hours (copy + wire up icon resources)

### Pattern 3: Thinking Panel (Collapsable with auto-expand)
- **Reusability**: YES
- **What to adopt**: The `inProgress → auto-expand` logic is clean and directly reusable. The `expandVertically/shrinkVertically` AnimatedVisibility pattern with left-border `drawBehind` treatment.
- **Modifications needed**: None for core behavior; may want explicit duration on expand (currently default spring ~400ms)
- **Effort**: 1 hour

### Pattern 4: Collapsable Progress Panel
- **Reusability**: YES (for tool-use phases)
- **What to adopt**: Title slide animation, CircularProgressIndicator → done-icon transition, log viewer trigger
- **Modifications needed**: May need to adapt `updateCollapsableProgressPanelMessage()` logic to OpenClaw's tool dispatch model
- **Effort**: 2 hours

### Pattern 5: AudioAnimation (AGSL shader)
- **Reusability**: PARTIAL
- **What to adopt**: The amplitude normalization with `pow(x, 0.5)`, the 100ms `Animatable` tween for amplitude smoothing, the `withFrameMillis` loop pattern
- **Modifications needed**: Requires Android 13+; needs fallback for API < 33. The shader itself is self-contained GLSL.
- **Effort**: 2 hours (including fallback implementation)

### Pattern 6: Auto-scroll with 90px threshold
- **Reusability**: YES
- **What to adopt**: The `canScroll` threshold check prevents scroll-jacking when user scrolls up during streaming. Exact value: `< 90` pixels from bottom.
- **Modifications needed**: None
- **Effort**: 0.5 hours

### Pattern 7: Loading placeholder → first token swap
- **Reusability**: YES
- **What to adopt**: Show `ChatMessageLoading` immediately on send, remove on first token arrival. Clean UX — no blank state between send and first token.
- **Modifications needed**: None
- **Effort**: 0.5 hours (already implicit in ViewModel pattern)

---

## 7. Optimization Opportunities

### High Priority
1. **Decouple streaming content from full ChatUiState**: Split `streamingContent: String` into its own `StateFlow` consumed only by the active streaming message composable. This reduces recomposition from O(all messages) to O(1) per token.

2. **StringBuilder for token accumulation**: Replace `"${lastMessage.content}${partialContent}"` with a `StringBuilder` held in the ViewModel, converting to String only when `done == true` or at render time.

3. **Explicit `key()` in `itemsIndexed`**: Use `message.id` (requires adding stable IDs to `ChatMessage`) as the LazyColumn key to prevent positional recomposition when messages are inserted mid-list (e.g., thinking → text transition).

### Medium Priority
4. **Token batching for markdown render**: `processLlmResponse()` currently does a single `replace()`. For markdown parsing (MarkdownText), consider batching tokens every 50–100ms with a `debounce` or `conflate` operator to avoid re-parsing markdown on every keystroke.

5. **`@Stable` / `@Immutable` annotations**: `ChatMessage` subclasses are open classes. Marking them `@Immutable` (or using data classes with `@Stable` ViewModel) allows Compose to skip recomposition for unchanged items.

### Low Priority
6. **Amplitude animation `Animatable` leak**: In `AudioAnimation`, a new `Animatable` is created inside `LaunchedEffect(amplitude)` on every amplitude change. This is correct (LaunchedEffect cancels prior), but the pattern could use `animateFloatAsState` instead for simpler code.

---

## 8. Exact Values Reference

| Parameter | Value | Source |
|---|---|---|
| RotationalLoader outer rotation duration | 2000ms | `RotationalLoader.kt` |
| RotationalLoader outer easing | `CubicBezierEasing(0.5f, 0.16f, 0f, 0.71f)` | `RotationalLoader.kt` |
| RotationalLoader scale duration | 1000ms | `RotationalLoader.kt` |
| RotationalLoader scale easing | `EaseInOut` | `RotationalLoader.kt` |
| RotationalLoader scale range | 1.0 → 0.4 | `RotationalLoader.kt` |
| Loading icon alpha range | 0.3 → 1.0 | `MessageBodyLoading.kt` |
| Loading icon alpha duration | 1000ms | `MessageBodyLoading.kt` |
| Loading icon alpha easing | `LinearEasing` | `MessageBodyLoading.kt` |
| Amplitude animation duration | 100ms | `AudioAnimation.kt` |
| Audio animation alpha overlay | 0.8f | `ChatPanel.kt` |
| Audio wave strength | 0.036 | `AudioAnimation.kt` AGSL |
| Audio wave speed | 1.2 | `AudioAnimation.kt` AGSL |
| Audio wave frequency | 4.0 | `AudioAnimation.kt` AGSL |
| Perlin noise amplitude drop threshold | 0.2 | `AudioAnimation.kt` |
| Auto-scroll bottom threshold | 90px | `ChatPanel.kt` |
| Model init polling interval | 100ms | `LlmChatViewModel.kt` |
| Model init initial delay | 500ms | `LlmChatViewModel.kt` |
| Image limit banner duration | 3000ms | `ChatPanel.kt` |
| Reset session retry interval | 200ms | `LlmChatViewModel.kt` |
| Token batch size | 1 (no batching) | `LlmChatViewModel.kt` |
| Thinking left-border stroke | 2.dp | `MessageBodyThinking.kt` |
| Thinking panel padding | 12.dp horizontal, 8.dp vertical | `MessageBodyThinking.kt` |
| CollapsablePanel spinner size | 16.dp, strokeWidth=2.dp | `MessageBodyCollapsableProgressPanel.kt` |

---

## 9. Recommended Approach for Phase 1

### Must-Have Patterns
1. **RotationalLoader** — copy directly, used as the universal loading state indicator
2. **Loading placeholder → first token swap** — critical for perceived performance
3. **Auto-scroll with 90px threshold** — prevents scroll-jacking, must use exact value
4. **Token streaming ViewModel pattern** — `resultListener` callback → `updateLastTextMessageContentIncrementally()`
5. **Thinking panel with auto-expand** — for models with thinking mode

### Should-Have (with optimization)
6. **Collapsable progress panel** — for tool-use transparency
7. **inProgress + preparing dual-flag** — `preparing=true` between send and first token, `inProgress=true` between first token and done

### Optimization Before Shipping
- Add `key()` to `itemsIndexed` using stable message IDs
- Split streaming content into its own StateFlow (most impactful performance win)
- Add `@Immutable` to `ChatMessage` subclasses

### Skip for Phase 1
- **AudioAnimation AGSL shader** — Android 13+ only, not core chat functionality; implement in Phase 2 with proper API level gating
- `streamingMessagesByModel` map — the standard path doesn't use it; defer until agent multi-model streaming is needed

---

## 10. Files Audited

| File | Purpose |
|---|---|
| `ui/llmchat/LlmChatViewModel.kt` | Core streaming logic, thinking state machine |
| `ui/common/chat/ChatViewModel.kt` | State management, incremental update methods |
| `ui/common/chat/ChatMessage.kt` | Message type hierarchy |
| `ui/common/chat/ChatPanel.kt` | LazyColumn rendering, auto-scroll, animations |
| `ui/common/chat/ChatView.kt` | Top-level chat scaffold, image viewer |
| `ui/common/chat/MessageBodyText.kt` | Text rendering with accessibility LiveRegion |
| `ui/common/chat/MessageBodyThinking.kt` | Thinking collapsable with auto-expand |
| `ui/common/chat/MessageBodyLoading.kt` | Loading indicator with RotationalLoader |
| `ui/common/chat/MessageBodyCollapsableProgressPanel.kt` | Progress panel animations |
| `ui/common/RotationalLoader.kt` | Primary loader animation |
| `ui/common/AudioAnimation.kt` | AGSL shader audio visualization |
| `ui/common/chat/ModelDownloadingAnimation.kt` | Download progress UI |
| `runtime/LlmModelHelper.kt` | Inference interface, ResultListener signature |
| `common/Utils.kt` | `processLlmResponse()` implementation |
