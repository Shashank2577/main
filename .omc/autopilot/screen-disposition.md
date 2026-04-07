# Screen Disposition Table

_Generated: 2026-04-06_
_Source: `MainActivity.kt` tab index map + screen file inspection_

---

## Navigation Context

Current app uses an integer tab index (`var currentTab by remember { mutableIntStateOf(3) }`) inside `MainContent`. There is no bottom navigation bar — all navigation is driven programmatically via `onNavigateTo*` callbacks and a `ModalNavigationDrawer`.

After the refactor, the integer tab index is replaced with a sealed class `MainScreen` with 5 variants. The drawer becomes the primary navigation surface for secondary screens.

---

## Screen Disposition Table

| Tab | Screen Name | File | Disposition | Reason | Tool/Feature Equivalent |
|-----|-------------|------|-------------|--------|------------------------|
| 0 | Dashboard | `DashboardScreen.kt` | **REMOVE** | Navigation hub made obsolete by drawer + quick-action chips; all its 13 navigation tiles become chips or drawer entries | N/A — replaced by chip strip |
| 1 | Tasks | `TasksScreen.kt` | **REMOVE** | Full task management UI replaced by natural language via chat | `TaskManagerTool` with `action:list` / `action:create` via chat or "Tasks" quick-action chip |
| 2 | Notes | `NotesScreen.kt` | **REMOVE** | Smart note management replaced by natural language via chat | `SmartNoteTool` via chat or "Notes" quick-action chip |
| 3 | Chat | `ChatScreen.kt` | **KEEP + ENHANCE** | Primary screen; becomes the sole persistent UI surface | N/A — this IS the product |
| 4 | Discover | `DiscoverScreen.kt` | **REMOVE** | Feature discovery catalogue replaced by quick-action chips + "What can I do?" help prompt | Quick-action chips in InputBar; help chip sends discovery prompt |
| 5 | Settings | `SettingsScreen.kt` | **KEEP** | App-level settings (API keys, theme, privacy, model config) must remain a dedicated screen | Accessible via drawer "Settings" entry |
| 6 | Calendar | `CalendarScreen.kt` | **REMOVE** | Calendar view replaced by natural language queries and CalendarTool | `CalendarTool` with `action:list_events` / `action:create_event` via chat or "Calendar" quick-action chip |
| 7 | Travel | `TravelScreen.kt` | **REMOVE** | Travel planner UI replaced by natural language + TravelManagerTool | `TravelManagerTool` via chat; e.g. "Show my trips" |
| 8 | Insights | `InsightsScreen.kt` | **REMOVE** | Productivity charts replaced by natural language queries | `ProductivityInsightsTool` via chat; e.g. "Show my productivity insights" |
| 9 | Briefing | `BriefingScreen.kt` | **REMOVE** | Daily briefing replaced by "Briefing" quick-action chip that auto-runs the DailyBriefingTool | `DailyBriefingTool` triggered by "Briefing" chip (autoSend=true) |
| 10 | Team | `TeamScreen.kt` | **REMOVE** | Team member management replaced by natural language + TeamManagerTool | `TeamManagerTool` via chat; e.g. "Show team members" |
| 11 | Files | `FileBrowserScreen.kt` | **KEEP** | File browser requires a visual tree UI; cannot be replicated in chat | Accessible via drawer "Files" entry + "Files" quick-action chip |
| 12 | Voice | `VoiceConversationScreen.kt` | **KEEP** | Full-screen voice mode requires dedicated layout (waveform, hands-free) | Accessible via "Voice" quick-action chip + drawer entry |
| 13 | Habits | `HabitsScreen.kt` | **REMOVE** | Habit tracking UI replaced by natural language + HabitTrackerTool | `HabitTrackerTool` via chat; e.g. "Log water habit" / "Show habits" |
| 14 | Decisions | `DecisionScreen.kt` | **REMOVE** | Decision log replaced by natural language + DecisionLogTool | `DecisionLogTool` via chat; e.g. "Log a decision" |
| 15 | Memory | `MemoryScreen.kt` | **KEEP** | Memory browser is a read/edit UI that cannot be done via chat alone | Accessible via drawer "Memory" entry |
| 16 | Reminders | `RemindersScreen.kt` | **REMOVE** | Reminders replaced by natural language + NotificationTool | `NotificationTool` / `CalendarTool` via chat; e.g. "Remind me at 3pm" |
| — | Onboarding | `OnboardingScreen.kt` | **KEEP + SIMPLIFY** | First-run setup required; remove Groq/Cerebras steps | N/A — entry flow |
| — | Splash | `SplashScreen.kt` (inline in `MainActivity.kt`) | **KEEP** | App entry animation; already minimal | N/A — entry animation |
| — | ConversationHistory | `ConversationHistoryScreen.kt` (panel, not a full screen) | **KEEP** | Already used inside the drawer as `ConversationHistoryPanel` | Lives in `ModalDrawerSheet` |
| — | FileViewer | `FileViewerScreen.kt` | **KEEP** | Detail view for files opened from FileBrowser | Navigated from `FileBrowserScreen` |

---

## Summary Counts

| Disposition | Count | Screens |
|-------------|-------|---------|
| KEEP (unchanged) | 3 | Chat (enhanced), Settings, Files, Voice, Memory, Onboarding (simplified), Splash, ConversationHistoryPanel, FileViewer |
| KEEP + ENHANCE | 1 | ChatScreen — adds ModelPickerChip, QuickActionChips, PerChatSettingsSheet |
| KEEP + SIMPLIFY | 1 | OnboardingScreen — removes Groq/Cerebras steps |
| REMOVE | 13 | Dashboard, Tasks, Notes, Discover, Calendar, Travel, Insights, Briefing, Team, Habits, Decisions, Reminders, + (implicit) old DashboardScreen |

**Net screen count: 17 → 5 navigable destinations** (Chat, Settings, Files, Voice, Memory)

---

## Quick-Action Chip Mapping

Each removed screen that had commonly-used functionality gets a quick-action chip in the `ChatScreen` input area:

| Chip Label | Icon | AutoSend | Injected Prompt / Action |
|------------|------|----------|-------------------------|
| Briefing | `WbSunny` | true | "Give me my daily briefing" |
| Tasks | `CheckBox` | false | "Show my tasks" (user can refine) |
| Notes | `NoteAlt` | false | "Show my notes" |
| Calendar | `CalendarMonth` | false | "What's on my calendar today?" |
| Web Search | `Search` | false | "Search the web for " (cursor at end) |
| Voice | `Mic` | — | Navigate to VoiceConversationScreen |
| Files | `Folder` | — | Navigate to FileBrowserScreen |
| Memory | `Psychology` | — | Navigate to MemoryScreen |

---

## Drawer Entry Points (Post-Refactor)

```
ModalDrawerSheet
  ├── [Space switcher]  ← NEW: list of Space items from SpaceManager
  ├── [New Conversation button]
  ├── ─────────────────────
  ├── [Conversation history list]  ← existing ConversationHistoryPanel
  ├── ─────────────────────
  ├── Memory            → MainScreen.MEMORY
  ├── Files             → MainScreen.FILES
  └── Settings          → MainScreen.SETTINGS
```

---

## Sealed Class Replacement for Tab Index

```kotlin
// BEFORE (MainActivity.kt):
var currentTab by remember { mutableIntStateOf(3) }

// AFTER:
sealed class MainScreen {
    object Chat     : MainScreen()
    object Settings : MainScreen()
    object Files    : MainScreen()
    object Voice    : MainScreen()
    object Memory   : MainScreen()
}
var currentScreen by remember { mutableStateOf<MainScreen>(MainScreen.Chat) }
```

All `onNavigateTo*` callbacks in `ChatScreen` are updated to accept a `(MainScreen) -> Unit` navigate lambda instead of the current screen-specific callbacks.

---

## Files to Delete

```
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

_(12 files — DiscoverScreen is the 13th conceptual removal; it was tab 4, listed above)_
