# Gallery Tool Integration & Result Rendering Audit

_Analyzed: 2026-04-06_
_Source files: openClaw-android (actual), /tmp/gallery-ref (Gallery reference)_

---

## 1. Tool Integration Architecture

### Gallery Architecture (Reference)

Gallery's AgentChat uses a **JS-skill + intent model** — tools are JavaScript files loaded at runtime from URLs or file imports, not compiled Kotlin classes.

```
AgentTools (ToolSet)
  loadSkill(skillName)      → reads skill YAML/JS manifest, emits SkillProgressAgentAction
  runJs(skillName, script, data) → calls JS via WebView bridge, awaits result, emits CallJsAgentAction
  runIntent(intent, params) → fires Android Intent via IntentHandler
```

Tool results return `Map<String, Any>` with a `result` or `error` key. The JS bridge supports three result shapes:
- `{ result: "string" }` — plain text result
- `{ image: { base64: "..." } }` — base64 image to render inline
- `{ webview: { url: "...", iframe: bool, aspectRatio: float } }` — URL to render in embedded WebView

Action dispatch uses a `Channel<AgentAction>` — tool execution sends actions, and AgentChatScreen collects and renders them. This is Gallery's unique bridge between tool execution and UI rendering.

### OpenClaw Architecture (Current)

OpenClaw uses a **static Kotlin class** model — all tools are registered at startup as compiled `Tool` interface implementations.

```
Tool (interface)
  name: String
  description: String
  parameterSchema: JsonObject
  requiredPermissions: List<String>
  execute(arguments: JsonElement): ToolResult

ToolResult
  output: String        ← ALL results are plain strings
  isError: Boolean

ToolRegistry
  register(tool)
  get(name): Tool?
  getDefinitions(): List<ToolDefinition>  ← fed to LLM
```

`AgentRuntime` drives the tool loop:
1. LLM responds with `ToolCallRequest` objects
2. `AgentRuntime` looks up tool in `ToolRegistry`, checks permissions, executes with 30s timeout
3. Result emitted as `AgentEvent.ToolCallStart` (before) and `AgentEvent.ToolCallResult` (after)
4. Both events flow into the message list via `StateFlow<List<AgentEvent>>`

**Key OpenClaw tools registered:**
TaskManagerTool, TravelManagerTool, TeamManagerTool, CalendarTool, ContactsTool, SmsTool, EmailTool, WebSearchTool, FetchUrlTool, MemoryTool, SmartNoteTool, HabitTrackerTool, DailyBriefingTool, ProductivityInsightsTool, DecisionLogTool, NotificationTool, SettingsControlTool, AppLauncherTool, ClipboardTool, ScreenCaptureTool, ReadFileTool, WriteFileTool, ListFilesTool, SearchFilesTool, MoveFileTool, DeleteFileTool, CreateDirectoryTool, ShareFileTool, ExportChatTool, PrivacyAuditTool

**Local model constraint:** For local (LiteRT) models, only the first 8 tool definitions are sent to the LLM. This prevents context overflow on 2K–4K context windows.

---

## 2. Tool Result Output Format (Current OpenClaw)

All tools return `ToolResult(output: String, isError: Boolean)`. The output is always **plain text**, formatted as Markdown. Examples from actual tool implementations:

**TaskManagerTool** (`formatTaskList`):
```
Tasks by Project:

### ProjectName (3 tasks)
  !!! TaskTitle (pending)
   *  AnotherTask (in_progress)

### Itinerary (5 items)
= **Flight to NYC** [flight]
  Mon, Apr 7 at 8:00 AM | JFK
```
Uses `**bold**`, `###` headings, `- [ ]` checkboxes (in TravelManagerTool checklists), and emoji-free priority icons (`!!!`, ` * `, ` - `).

**TravelManagerTool** (`tripChecklist`):
```markdown
## Pre-Trip Checklist: Trip Name
...
### Travel Documents
- [ ] Passport / ID valid
- [x] Flight booking confirmed
```

**TeamManagerTool**: Similar markdown-heavy plain text output with `###` section headers.

**All tools**: No JSON output, no structured data returned to the UI layer. Everything is formatted strings.

---

## 3. Tool Result Rendering (Current OpenClaw)

### `ToolCallBubble` (AgentEvent.ToolCallStart)
A small pill composable: `Terminal` icon + tool name in `labelSmall` monospace. Shown inline before the result, low-opacity. Not expandable.

### `ToolResultBubble` (AgentEvent.ToolCallResult)
```kotlin
// Key behavior:
val needsTruncation = event.result.length > 200
// Truncates to 200 chars with "…" and "more/less" toggle
// Renders as monospace bodySmall text (11sp, 16sp line height)
// Accent color: Emerald (success) or ErrorRed (error)
// Not rendered as Markdown — raw monospace text
```

**Critical gap:** Tool results are NOT passed through `MarkdownText`. They display as raw monospace text. This means `**bold**`, `###` headings, and `- [ ]` checkboxes from tool output are shown as literal asterisks and hashes — not rendered.

### `AssistantBubble` (AgentEvent.AssistantMessage)
Uses `MarkdownText` composable, which renders:
- `**bold**`, `*italic*`, `` `inline code` ``
- ` ```code blocks` `` with language label
- `#`, `##`, `###` headings
- `- ` and `* ` list items
- `[text](url)` and bare URLs as clickable links
- ` ```mermaid` `` blocks via WebView (JSDelivr CDN)

The LLM's synthesis of tool results IS rendered as Markdown. But the raw tool output pill is not.

---

## 4. Gallery Result Rendering Patterns

### ChatMessage Type Hierarchy
Gallery has a rich type system for chat messages:
```
ChatMessageType enum:
  TEXT, LOADING, INFO, WARNING, ERROR,
  IMAGE, IMAGE_WITH_HISTORY, AUDIO_CLIP,
  CLASSIFICATION, BENCHMARK_RESULT, BENCHMARK_LLM_RESULT,
  PROMPT_TEMPLATES, WEBVIEW, COLLAPSABLE_PROGRESS_PANEL, THINKING,
  CONFIG_VALUES_CHANGE
```

Each type maps to a dedicated `MessageBody*` composable dispatched in `ChatPanel`.

### `MessageBodyWebview`
Renders a URL in an embedded `GalleryWebView` with:
- Inline display with `aspectRatio` parameter
- `AssistChip` button → full-screen `ModalBottomSheet` expansion
- Security: `allowFileAccess = false`, no DOM storage

This is used by Gallery's JS skills when a tool returns `{ webview: { url, iframe, aspectRatio } }`. The LLM can produce rich HTML content (charts, visualizations) served from a local file server within the app.

### `MessageBodyCollapsableProgressPanel`
A collapsable progress panel for skill/tool execution steps:
- Header row with title + animated expand/collapse arrow
- Expandable list of `(title, description)` items — each step the agent took
- Uses `expandVertically()` animation

This is Gallery's equivalent of OpenClaw's `ToolCallBubble` + `ToolResultBubble` but richer: it shows a sequential list of tool steps in a single collapsable panel.

### `MessageBodyError`
Centered pill with `errorContainerColor` background, renders error text with `MarkdownText`. No retry button in Gallery — error is terminal. In contrast, OpenClaw's `ErrorBubble` has a Retry button with `AgentRuntime.retryLastMessage()`.

### Format Detection
Gallery does NOT auto-detect format. The type is explicitly set by the ViewModel when constructing `ChatMessage` objects. There is no runtime logic that inspects output text to decide "this should be a WebView vs a card vs plain text."

---

## 5. Gallery Skill Management UI

`SkillManagerBottomSheet` is a full-featured `ModalBottomSheet` for runtime skill management:
- Search bar with `TextField`
- Scrollable list of skills with `Checkbox` per skill (enable/disable)
- Per-skill actions: view preview, open URL, delete, set secret (API key)
- Import skill from URL or file (`DriveFolderUpload` + `Link` icons)
- `Switch` for toggling "use selected skills" vs "all skills"

OpenClaw's equivalent is the static `ToolRegistry` — no UI for toggling tools. This is intentional for MVP simplicity.

---

## 6. Error Handling Comparison

| Aspect | Gallery | OpenClaw |
|--------|---------|----------|
| Error display | Centered `MessageBodyError` pill | `ErrorBubble` card (left-aligned) |
| Retry | None — user must re-send | `OutlinedButton` → `AgentRuntime.retryLastMessage()` |
| Tool timeout | Not in Gallery (JS skill, no timeout) | 30s `withTimeoutOrNull` per tool |
| Permission denied | Not applicable (JS model) | `PermissionManager.ensurePermissions()` → `ToolResult.error(...)` |
| Max iterations | Not explicit | `MAX_TOOL_ITERATIONS = 10` |
| Network fallback | None | Auto-fallback to local LiteRT model on `UnknownHostException` |
| Error sanitization | None | `ErrorHandler.sanitizeErrorMessage()` strips API keys, file paths |
| Crash recovery | None | `OpenClawApp.consumeLastCrash()` + `AlertDialog` in ChatScreen |

OpenClaw's error handling is **significantly more robust** than Gallery's. No changes needed here.

---

## 7. Reusability Assessment for OpenClaw Tools

### What Gallery offers that OpenClaw lacks

**A. Rich tool result rendering (HIGH VALUE)**

Gallery's `CollapsableProgressPanel` is architecturally better than OpenClaw's current dual `ToolCallBubble` + `ToolResultBubble`. In a multi-step agent run (e.g., "plan my trip" → 5 tool calls), OpenClaw shows 10 separate pill bubbles. Gallery collapses them into one expandable panel.

Adaptation path:
- Add `AgentEvent.ToolStepGroup(steps: List<ToolStep>)` or post-process event list into groups
- Render as `CollapsableToolPanel` composable: header with step count + expandable step list
- Each step: tool name + truncated result in 2-line format

**B. Markdown rendering in tool results (MEDIUM VALUE)**

Tools like `TaskManagerTool` output `**bold**` and `### headers` but `ToolResultBubble` renders them as raw monospace text. Fix: pass `event.result` through `MarkdownText` instead of `Text(fontFamily = Monospace)` in `ToolResultBubble`. This is a 5-line change.

**C. WebView result rendering (LOW VALUE for Phase 1, MEDIUM for Phase 2)**

Gallery supports `{ webview: { url } }` as a tool result type. OpenClaw could add a special tool output prefix (e.g., `[WEBVIEW:url]`) that `ToolResultBubble` detects and renders via `AndroidView(WebView)`. Not needed for MVP — no current tool returns URLs for rendering.

**D. Structured card rendering (NOT APPLICABLE)**

Gallery has `ChatMessageClassification`, `ChatMessageBenchmark` etc. — specialized card types for ML task outputs. These are not relevant to OpenClaw's assistant use case.

### What OpenClaw has that Gallery lacks
- Retry on error
- Network fallback (local model escalation)
- Permission gating
- Tool timeout with graceful degradation
- Error sanitization
- Crash recovery persistence

---

## 8. Specific Tool Output Rendering Gaps

| Tool | Current Output | Rendering Gap | Fix |
|------|---------------|---------------|-----|
| `task_manager` (list/by_project/by_person) | `**title**`, `### headers`, `!!!` priority icons | Shown as raw text in ToolResultBubble | Pass through MarkdownText |
| `travel_manager` (view_trip, checklist) | `## headers`, `- [ ]` checkboxes, `**bold**` | Same gap | Same fix |
| `team_manager` (dashboard) | `### headers`, formatted sections | Same gap | Same fix |
| `daily_briefing` | Multi-section markdown | Same gap | Same fix |
| `productivity_insights` | Stats + trend text | Acceptable as monospace | Low priority |
| `web_search` | Plain text URLs and snippets | Acceptable | None needed |
| `read_file` | File contents (may be code) | Code content looks fine in monospace | None needed |

**Root cause:** `ToolResultBubble` uses `Text(fontFamily = Monospace)` instead of `MarkdownText`. Single fix, high impact.

---

## 9. Phase 1 vs Phase 2 Recommendations

### Phase 1 (MVP — minimal effort, high impact)

**P1-T1: Fix ToolResultBubble to use MarkdownText**
- File: `MessageBubble.kt`, `ToolResultBubble` composable (line ~193)
- Change: Replace `Text(fontFamily = Monospace)` with `MarkdownText(markdown = displayText)`
- Keep the truncation logic and expand/collapse toggle
- Remove monospace font — markdown renderer handles code blocks natively
- Effort: 1 hour

**P1-T2: Add tool name → icon mapping**
- File: `MessageBubble.kt`, `ToolCallBubble` composable
- Change: Map `event.toolName` to a meaningful icon (e.g., `task_manager` → `CheckCircle`, `travel_manager` → `Flight`, `web_search` → `Search`)
- Keep pill shape and dim styling
- Effort: 2 hours (create icon map, update composable)

**P1-T3: Improve error bubble — add "Go to Settings" deep link**
- File: `MessageBubble.kt`, `ErrorBubble`
- Current: Shows "Go to Settings to fix this." as `Text`
- Change: Make it a `TextButton` that calls `onNavigateToSettings()`
- Requires threading `onNavigateToSettings` through `MessageBubble` → `ChatScreen` already has this lambda
- Effort: 1 hour

### Phase 2 (Polish)

**P2-T1: CollapsableToolPanel — group multi-step tool calls**
- New composable `CollapsableToolPanel` replacing multiple `ToolCallBubble` + `ToolResultBubble` pairs
- Post-process `displayEvents` in `ChatScreen` to group consecutive tool events into a `ToolStepGroup`
- Render as single collapsable row showing "N tool calls" with expand to see each step
- Inspired by Gallery's `MessageBodyCollapsableProgressPanel`
- Effort: 1 day

**P2-T2: WebView result type**
- Add `AgentEvent.WebViewResult(url: String)` for tools that produce HTML content
- Render via `MessageBodyWebview`-style composable
- Prereq: at least one tool needs to produce an HTML URL (e.g., `productivity_insights` could generate an HTML chart)
- Effort: 2 days (including local HTTP server or data: URI approach)

**P2-T3: Animated tool execution state in TopBar**
- While `AgentState.ExecutingTool(name)` is active, show tool name in a subtitle or progress chip in `ChatScreen` TopBar
- Gallery shows this as a pill below the AppBar in `AgentChatScreen`
- Effort: 4 hours

---

## 10. Summary Table

| Aspect | Gallery | OpenClaw Current | Gap |
|--------|---------|-----------------|-----|
| Tool model | JS skills loaded at runtime | Static Kotlin classes | Different models — OpenClaw's is simpler and appropriate for MVP |
| Tool result type | String / Image / WebView / Error | String only | WebView support missing (Phase 2) |
| Tool result rendering | Dispatch by type → dedicated composable | Raw monospace Text, truncated | Markdown not rendered (P1-T1 fix) |
| Tool progress UI | CollapsableProgressPanel (expandable list) | Pill per tool call (flat) | No grouping (Phase 2) |
| Error display | Centered pill, no retry | Left card with retry button | OpenClaw is better |
| Format detection | None (type set explicitly) | None | Not needed — type system handles it |
| Skill management UI | Full ModalBottomSheet (import/delete/toggle) | None (all tools always on) | Not needed for MVP |
| Secret/API key per tool | Per-skill secrets via DataStore | Global provider API keys in Settings | Not applicable to OpenClaw tools |
| Intent tools | Android Intent bridge | AppLauncherTool (wraps Intent) | Already covered |
| Permission gating | None | Per-tool `requiredPermissions` + `PermissionManager` | OpenClaw is better |
