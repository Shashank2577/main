# Product Specification: Unified AI Chat App

> **Based on:** Google AI Edge Gallery (`.gallery-ref/`)
> **Platform:** Android (Kotlin, Jetpack Compose, Material3)
> **Default Model:** Gemma 4 bundle (on-device)

---

## 1. Vision

Transform the Google AI Edge Gallery — a showcase app with separate disconnected sections for scribe, agents, chat, ask, image, and voice — into a unified, production-ready AI assistant app. The app is a **knowledge-workspace with an AI assistant**, not just a chat app.

One chat interface. Everything accessible contextually. Voice, images, tools, knowledge — all within a single conversational flow.

---

## 2. Core Features

### 2.1 Unified Chat Interface
The entire app is one chat screen. No tabs, no separate sections. Every capability (voice, image, tools, scribe, agents) is accessible from within the conversation.

### 2.2 Multi-Turn Conversations
- Persistent conversation history stored in Room database
- Streaming token-by-token responses with real-time display
- Markdown rendering for all AI responses (headers, bold, code blocks, lists)
- Message types: text, image, voice transcript, tool invocation, tool result, thinking indicator, error

### 2.3 Model Selection & Routing
- **Default:** Gemma 4 bundle (on-device, loads at app launch)
- **Cloud fallback:** Gemini Flash / Gemini Pro (requires API key)
- **Dynamic switching:** User can change model mid-conversation via model picker chip
- **ModelRouter:** Automatic routing between local and cloud based on capability requirements
- **Cloud escalation:** When local model cannot handle a request (e.g., complex reasoning, large context), automatically offer cloud escalation

### 2.4 Voice Input
- Toggle between text and voice input modes from the input bar
- Voice-to-text transcription displayed as user message
- Full voice conversation mode (separate screen) for hands-free back-and-forth
- Audio recording with waveform visualization

### 2.5 Media Attachments
- Attach images from camera or gallery
- Image appears as thumbnail in the message thread
- AI processes attached images (vision capabilities via multimodal models)
- File sharing from sandboxed storage

### 2.6 Knowledge Spaces
- Multiple isolated "Spaces" — each with its own conversation history, knowledge base, and system prompt
- Default space: "General"
- Users create/rename/delete spaces
- Each space has: name, emoji icon, description, custom system prompt
- Sandboxed filesystem per space (files uploaded to a space stay in that space)
- Switch spaces mid-session via drawer or space switcher

### 2.7 Tool Calling
- LLM can invoke tools by emitting structured function calls
- Tool results rendered inline in the conversation with markdown
- **Built-in tools:**
  - Trip planner
  - Task management
  - File search (within space)
  - Web search (if cloud model)
- **Extensible:** ToolRegistry allows registering new tools
- Tool invocation shows: tool name, parameters, loading state, result

### 2.8 Per-Chat Settings
- Per-conversation LLM parameter overrides:
  - Temperature (0.0 - 1.0)
  - Top-K (1 - 100)
  - Top-P (0.0 - 1.0)
  - Max tokens (configurable)
- Accessible via bottom sheet from chat screen
- Settings persist in Room database per conversation
- Preferred model ID per conversation

### 2.9 Quick Action Chips
- Horizontal scrollable row of predefined prompt shortcuts
- Chips: Summarize, Explain, Critique, Extend, Simplify (default set)
- Tapping a chip sends the corresponding prompt immediately
- Contextual: chips can change based on active space or conversation context
- Position: below top bar, above messages

### 2.10 Themes
- Dark and light theme support
- System-follows-device option
- Material3 dynamic color (Material You)

### 2.11 Onboarding
- First-launch flow: model download (Gemma 4 bundle)
- Optional: cloud API key setup (Gemini)
- Space creation introduction
- Permission requests (camera, microphone, storage)

### 2.12 Settings
- Global app settings (theme, default model, notification preferences)
- API key management (Gemini API key, encrypted storage)
- Model management (download, delete, check updates)
- About / version info
- Data management (clear conversations, export data)

---

## 3. Architecture (from Gallery Reference)

### 3.1 Tech Stack
| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material3 |
| Database | Room (SQLite) |
| On-device LLM | LiteRT-LM (Google AI Edge) |
| Networking | OkHttp + SSE (Server-Sent Events) |
| Encryption | AndroidX Security Crypto |
| Build | Gradle + KSP |
| Min SDK | 29 (Android 10) |
| Target SDK | 35 |
| Architectures | arm64-v8a, x86_64 |

### 3.2 Key Patterns from Gallery
- `ChatPanel` / `ChatView` — message list rendering with streaming
- `ChatViewModel` — conversation state management
- `MessageBubble` — individual message rendering (text, image, audio, benchmark, tool result)
- `ModelPicker` / `ModelPickerChip` — model selection UI
- `RotationalLoader` — thinking/loading indicator
- `AudioRecorderPanel` / `AudioPlaybackPanel` — voice I/O
- `MarkdownText` — rich text rendering
- `LlmModelHelper` — model loading, inference, streaming callbacks
- `DownloadRepository` — model download management
- `DataStoreRepository` — preferences and settings
- `AgentChatScreen` / `AgentTools` — tool calling and agent patterns
- `SkillManager` — extensible capability registration
- `LiveCameraView` — camera integration for image capture

### 3.3 Data Model

#### Tables (Room Database)

**conversations**
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | UUID |
| spaceId | TEXT FK | References spaces.id |
| title | TEXT | Auto-generated or user-set |
| createdAt | INTEGER | Epoch millis |
| lastMessageAt | INTEGER | For sorting |
| systemPrompt | TEXT? | Override for this conversation |

**messages**
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | UUID |
| conversationId | TEXT FK | References conversations.id |
| role | TEXT | "user", "assistant", "tool", "system" |
| content | TEXT | Message text (markdown) |
| mediaUri | TEXT? | Attached image/file URI |
| toolName | TEXT? | Tool invocation name |
| toolParams | TEXT? | JSON tool parameters |
| toolResult | TEXT? | Tool execution result |
| timestamp | INTEGER | Epoch millis |
| tokens | INTEGER? | Token count for this message |

**spaces**
| Column | Type | Notes |
|--------|------|-------|
| id | TEXT PK | UUID |
| name | TEXT | Display name |
| emoji | TEXT | Icon emoji, default folder |
| description | TEXT | User description |
| systemPrompt | TEXT? | Default system prompt for this space |
| createdAt | INTEGER | Epoch millis |
| lastUsedAt | INTEGER | For sorting |
| sortOrder | INTEGER | Manual ordering |

**per_chat_settings**
| Column | Type | Notes |
|--------|------|-------|
| conversationId | TEXT PK FK | References conversations.id |
| temperature | REAL | Default 0.7 |
| topK | INTEGER | Default 40 |
| topP | REAL | Default 0.95 |
| maxTokens | INTEGER | Default 4096 |
| preferredModelId | TEXT? | Model override |
| enabledTools | TEXT? | Comma-separated tool names |

---

## 4. Screen Descriptions

> **Purpose:** These descriptions define what each screen contains, its behavior, and its purpose — without visual design. Use these to create designs in Stitch, then provide the designs back for implementation.

---

### Screen 1: Chat Screen (Main Screen)

**This is the app.** The user spends 90%+ of their time here.

#### What's on screen (top to bottom):

**Top Bar:**
- Left: Hamburger menu icon (opens navigation drawer)
- Center: Current conversation title (truncated if long) — tappable to rename
- Right: Model picker chip showing current model name (e.g., "Gemma 4") — tappable to open model selection bottom sheet

**Quick Action Chips (horizontal scroll row):**
- Below the top bar, above messages
- 5 chips: Summarize, Explain, Critique, Extend, Simplify
- Scrollable horizontally if more than screen width
- Each chip has a small icon + label
- Tapping sends the prompt immediately with the current conversation context

**Message Area (scrollable, takes remaining vertical space):**
- Messages alternate between user (right-aligned, accent color) and assistant (left-aligned, surface color)
- Assistant messages render markdown (bold, italic, headers, code blocks, lists)
- Tool invocation messages: show tool name + parameters in a distinct card style
- Tool result messages: show result with markdown in a bordered card
- Thinking indicator: animated loader with "Thinking..." text while model processes
- Image messages: thumbnail preview, tappable to full-screen
- Voice transcript messages: text with a small microphone icon badge
- Streaming: assistant messages appear token-by-token, cursor/typing indicator visible during generation
- Empty state (new conversation): welcome message + suggestion chips

**Input Bar (bottom, fixed):**
- Text input field with placeholder "Ask me anything..."
- Attachment button (opens camera/gallery picker)
- Voice/mic toggle button (switches between text and voice input mode)
- Send button (appears when text is entered, replaces mic button)
- When in voice mode: input field replaced by waveform visualization + stop button

#### Overlays accessible from this screen:

- **Navigation Drawer** (left swipe or hamburger tap) — see Screen 2
- **Model Picker Bottom Sheet** (model chip tap) — see Screen 3
- **Per-Chat Settings Bottom Sheet** (long-press on model chip, or from drawer settings) — see Screen 4
- **Image Preview** (tap on image message) — fullscreen image viewer with close button

#### Product Owner Perspective:
> The chat screen IS the product. Every interaction — text, voice, image, tool — happens here. The user should never feel lost or need to "find" a feature. Quick action chips reduce friction for common tasks. The model picker chip is always visible so power users can switch models instantly. The input bar must feel as natural as any messaging app — WhatsApp/iMessage level polish. Empty state with suggestion chips drives first engagement.

#### QA Perspective:
> **Critical test cases:**
> - Streaming interruption: user sends new message while assistant is still generating — previous generation should stop gracefully
> - Long messages: markdown rendering with deeply nested lists, large code blocks, tables
> - Image attachment flow: camera permission denied, gallery empty, large image (>10MB)
> - Voice mode toggle: rapid toggle between text/voice shouldn't crash recorder
> - Orientation change during streaming: message state must persist
> - Memory pressure: 50+ messages in a conversation, scrolling must remain smooth
> - Model switching mid-conversation: context must transfer or warn
> - Tool result rendering: malformed JSON, very long output, nested markdown in tool results
> - Input field: emoji support, paste from clipboard (text + images), multi-line input
> - Keyboard overlap: input bar must stay above keyboard, messages scroll to latest

#### CEO Perspective:
> This screen competes with Google Gemini, ChatGPT, and Apple Intelligence. It must feel faster and more capable than cloud-only apps because it runs locally. The "magic moment" is when the user asks something, sees the thinking indicator, and gets a streaming response — all without internet. Quick action chips are the hook that makes users think "this is more than a chatbot." The model picker chip is the power-user differentiator — no other app makes model switching this frictionless. Voice mode must work flawlessly or it damages trust in the entire product.

---

### Screen 2: Navigation Drawer

**Slides in from the left over the chat screen.** Semi-transparent overlay behind it dims the chat.

#### What's on screen (top to bottom):

**Header:**
- App name/logo
- Current space indicator (emoji + name, e.g., "Work")

**Conversations Section:**
- "New Chat" button (prominent, at top)
- List of recent conversations in current space, sorted by last message time
- Each item shows: conversation title + last message preview + timestamp
- Swipe to delete a conversation
- Tapping a conversation loads it in the chat screen and closes drawer

**Spaces Section (separated by divider):**
- Label: "Spaces"
- List of user spaces with emoji + name
- Current space highlighted
- Tapping a space switches to it (loads that space's conversations)
- "+ Create Space" item at bottom

**Footer:**
- Settings link
- Voice Conversation mode link
- File Browser link

#### Product Owner Perspective:
> The drawer is the organizational backbone. Users who create Spaces become power users — they use the app daily. The "New Chat" button must be the most prominent element. Conversation list should feel like a messaging app's chat list. Space switching must be instant — no loading screens. The drawer should be swipe-dismissable (swipe left to close).

#### QA Perspective:
> **Critical test cases:**
> - Drawer opens/closes smoothly with gesture and tap
> - Conversation delete: swipe gesture, confirmation dialog, undo option
> - 100+ conversations: scrolling performance, lazy loading
> - Space with 0 conversations: empty state shown
> - Create space: name validation (empty, too long, duplicate), emoji picker
> - Rapid space switching: no stale data from previous space
> - Drawer state preserved on orientation change
> - Deep link: open app from notification, correct conversation loads

#### CEO Perspective:
> Spaces are the retention mechanism. A chat app with history is useful. A chat app with organized knowledge spaces is essential. The drawer is where users see the value of persistence — their conversations, their spaces, their organized knowledge. It should feel like a premium file manager meets a messaging app. If users never create a second space, we've failed to communicate the value.

---

### Screen 3: Model Picker Bottom Sheet

**Slides up from the bottom over the chat screen.** Drag handle at top for dismissal.

#### What's on screen:

**Header:**
- "Select Model" title
- Drag handle bar

**Model List:**
- Each model shows:
  - Model name (e.g., "Gemma 4", "Gemini 2.0 Flash")
  - Badge: "Local" (green) or "Cloud" (blue)
  - Model size (e.g., "2B", "7B")
  - Download status: downloaded (checkmark), downloading (progress bar), not downloaded (download icon)
  - Currently active model: highlighted/selected indicator
- Groups: "On-Device Models" section, then "Cloud Models" section

**Actions:**
- Tapping a downloaded model switches to it immediately
- Tapping a not-downloaded model starts download (progress shown inline)
- "Manage Models" link at bottom (goes to settings > model management)

#### Product Owner Perspective:
> Model switching must be a one-tap operation for downloaded models. The on-device vs cloud distinction is critical — users need to understand what runs locally (private, offline) vs cloud (faster for complex tasks, requires internet). Download progress must be clear because model files are 1-4GB. The active model indicator prevents confusion about which model is responding.

#### QA Perspective:
> **Critical test cases:**
> - Switch model mid-conversation: does the next response use the new model?
> - Download interrupted (airplane mode, app backgrounded): resume or clean state
> - Disk space insufficient for download: clear error message
> - Cloud model selected but no API key configured: redirect to API key setup
> - Cloud model selected but no internet: fallback message
> - Multiple rapid model switches: no race conditions
> - Model loading time: show loading indicator, don't block UI

#### CEO Perspective:
> This is where we differentiate. No mainstream AI app makes model switching this accessible. The local/cloud visual distinction tells a privacy story: "Your data stays on your device with local models." The download UX must be Apple-quality — progress bars, pause/resume, size warnings. Users who discover they can run AI without internet become evangelists.

---

### Screen 4: Per-Chat Settings Bottom Sheet

**Slides up from the bottom.** For power users who want to tune model behavior.

#### What's on screen:

**Header:**
- "Chat Settings" title
- Drag handle bar

**Settings Controls:**
- **Temperature** slider (0.0 - 1.0, default 0.7)
  - Label: "Creativity" with current value
  - Left: "Precise" / Right: "Creative"
- **Top-K** slider (1 - 100, default 40)
  - Label: "Top-K" with current value
- **Top-P** slider (0.0 - 1.0, default 0.95)
  - Label: "Top-P" with current value
- **Max Tokens** numeric input (default 4096)
  - Label: "Max Response Length"

**Actions:**
- "Save" button (persists to database for this conversation)
- "Reset to Defaults" button
- Settings apply to the next message in this conversation only

#### Product Owner Perspective:
> This is an advanced feature — most users will never open it. But the users who do are power users and developers who will champion the app. The labels should use human-friendly names ("Creativity" not just "Temperature"). The sheet should not overwhelm — four controls max. Defaults must be sensible so casual users who accidentally open this don't break their experience.

#### QA Perspective:
> **Critical test cases:**
> - Slider precision: temperature 0.0 and 1.0 edge values work
> - Save persists across app restart
> - Reset clears to actual defaults, not last saved
> - Max tokens: invalid input (0, negative, extremely large)
> - Settings from one conversation don't leak to another
> - UI: sliders respond to touch accurately on small screens

#### CEO Perspective:
> Per-chat settings are a developer/researcher feature. They signal "this is a serious tool, not a toy." Keep them accessible but not prominent. The fact that settings persist per-conversation is the insight — users can have a "creative writing" chat at temperature 0.9 and a "code review" chat at 0.2 simultaneously.

---

### Screen 5: Voice Conversation Screen

**Full-screen overlay or separate screen for hands-free voice interaction.**

#### What's on screen:

**Background:**
- Ambient animated visualization (waveform or pulsing orb) showing AI state:
  - Listening (user speaking): waveform animating with audio input
  - Processing (thinking): subtle pulsing animation
  - Speaking (AI responding): waveform animating with output audio
  - Idle (waiting): gentle breathing animation

**Center:**
- Large visual indicator of current state
- Transcript text appearing in real-time (user speech + AI response)

**Controls (bottom):**
- Large microphone button (tap to start/stop speaking)
- End conversation button
- Mute button
- Text mode toggle (return to chat screen)

**Top:**
- Current model name
- Conversation title
- Back/close button

#### Product Owner Perspective:
> Voice mode is the "wow" demo feature. When someone says "show me the app," you open voice mode and have a conversation with your phone — no typing, no tapping. The visualization must feel alive and responsive to audio. The transcript provides accountability — users can see exactly what was heard and said. This is also an accessibility feature for users who can't easily type.

#### QA Perspective:
> **Critical test cases:**
> - Microphone permission denied: graceful fallback with permission request
> - Background noise: does speech recognition handle noisy environments?
> - Long pause: does the app correctly detect end of speech vs natural pause?
> - Bluetooth audio: does voice mode work with Bluetooth headsets/car systems?
> - Phone call interruption: voice mode pauses and resumes correctly
> - Screen off: does voice mode continue when screen turns off?
> - Transcript accuracy: compare transcript to actual speech
> - Concurrent audio: what happens if music is playing?

#### CEO Perspective:
> Voice mode is the future of mobile AI. Every major player (Gemini Live, ChatGPT Voice, Siri) is investing here. Our advantage: it runs locally, so it works without internet, with lower latency, and with complete privacy. The visualization quality directly impacts perceived intelligence — a beautiful, responsive animation makes the AI feel smarter. This is the feature that gets shared on social media.

---

### Screen 6: Settings Screen

**Standard settings page accessible from the navigation drawer.**

#### What's on screen:

**Sections:**

**General:**
- Theme: Light / Dark / System
- Language (future)
- Default model selection

**API Keys:**
- Gemini API Key: masked input field + test button
- Encrypted storage indicator (lock icon)
- "Keys stored locally with encryption" note

**Models:**
- List of available models with download status
- Downloaded models: size on disk, delete button
- Available models: download button with size estimate
- "Check for Updates" button

**Data:**
- "Export Conversations" (JSON)
- "Clear All Conversations" (with confirmation)
- "Clear Cache"
- Storage usage indicator

**About:**
- App version
- Model versions
- Open source licenses
- "Based on Google AI Edge Gallery" attribution

#### Product Owner Perspective:
> Settings should be boring and reliable. Users go here to fix something or configure something specific — they don't browse. API key setup must have a "test connection" button so users know it works. Model management is the most-visited section — downloading/deleting models is a storage-management task. The export feature matters for users who want to move data between devices.

#### QA Perspective:
> **Critical test cases:**
> - API key validation: invalid key, expired key, rate-limited key
> - Theme switching: immediate, no restart required
> - Model deletion while model is in use: warning dialog
> - Export: conversations with images, tool results, voice transcripts
> - Clear all: irreversible action, double confirmation
> - Storage calculation accuracy
> - Settings persist across app updates

#### CEO Perspective:
> The settings screen signals trust. Encrypted key storage, data export, clear storage metrics — these are "enterprise-ready" features that make users comfortable using the app for sensitive work. The model management section is where users realize they're running AI locally — showing "2.1 GB on device" makes the local-AI value proposition tangible.

---

### Screen 7: Onboarding Flow

**First-launch experience. 3-4 screens maximum.**

#### Screens:

**1. Welcome:**
- App name + tagline
- Hero illustration/animation showing the unified chat concept
- "Get Started" button

**2. Model Setup:**
- "Download your AI model"
- Gemma 4 shown as recommended with size estimate
- Download progress bar
- "Skip for now" option (but model is required for local use)
- WiFi recommendation notice

**3. Permissions:**
- Camera (for image attachments)
- Microphone (for voice input)
- Storage (for knowledge spaces)
- Each permission explained: why it's needed, what happens if denied
- All optional — app works without them, just with reduced features

**4. Optional Cloud Setup:**
- "Add cloud AI for complex tasks"
- Gemini API key input
- "Skip — use local models only" prominently available
- Brief explanation: local = private + offline, cloud = more capable for complex tasks

#### Product Owner Perspective:
> Onboarding must get users to a working chat in under 2 minutes. The model download is the bottleneck (1-4GB) — start it immediately and let users explore while it downloads. Don't block the app on permissions — ask lazily when features are first used. The cloud setup should feel optional, not required. Users who skip cloud should still have a great experience.

#### QA Perspective:
> **Critical test cases:**
> - Download interrupted (app killed, phone restarted): resume from where it left off
> - No internet during onboarding: clear message, retry button
> - All permissions denied: app still functions for text chat
> - Onboarding completed flag: never show again, even after app update
> - Back button behavior: can go back to previous onboarding screen
> - Low storage: warning before download, don't crash mid-download

#### CEO Perspective:
> The first 60 seconds determine if the user keeps the app. The download wait is our biggest risk — we need to make it feel worth waiting for. Consider: show a demo/preview chat while the model downloads, so users see the value before they have it. The "no internet needed after setup" message is the key differentiator to communicate early.

---

### Screen 8: File Browser Screen

**Accessible from drawer. Shows files within the current space's sandboxed storage.**

#### What's on screen:

**Top Bar:**
- Back arrow
- Current space name + "Files" title
- Search icon
- Sort/filter options

**File List:**
- Grid or list view toggle
- Files grouped by type or date
- Each file shows: thumbnail/icon, name, size, date added
- Tap to preview, long-press for actions (share, delete, rename)

**Actions:**
- "Add Files" FAB (floating action button) — import from device storage
- Drag-and-drop from other apps (if supported)

**Empty State:**
- "No files in this space"
- Explanation: "Files you share in conversations appear here"
- "Add Files" button

#### Product Owner Perspective:
> File browser makes spaces tangible — it's where "knowledge base" becomes real. Files uploaded in a conversation automatically appear here. Users should be able to browse, search, and manage their space's files independently of conversations. This transforms the app from a chat app into a knowledge workspace.

#### QA Perspective:
> **Critical test cases:**
> - Large files (100MB+): upload progress, memory management
> - Unsupported file types: graceful handling with message
> - File deletion: removes from all conversations that reference it?
> - Storage permissions on Android 13+ (scoped storage)
> - File preview: PDFs, images, text files, code files
> - Search: filename search, content search (future)

#### CEO Perspective:
> The file browser is what makes us a "workspace" not just a "chatbot." It's the bridge between "I chatted about a document" and "I can find that document again." For enterprise/prosumer users, this is essential. Keep it simple — we're not building Google Drive, we're building an AI-organized filing cabinet.

---

## 5. Navigation Architecture

Based on the exploration in `navigation-explorer.html`, four patterns were evaluated:

| Pattern | Pros | Cons | Verdict |
|---------|------|------|---------|
| A: Left Drawer | Matches Gemini, clean, familiar | Requires gesture/tap to discover | **Recommended** |
| B: Bottom Sheets | Mobile-friendly, no gestures | More buttons in top bar | Alternative |
| C: Bottom Tabs | Always visible, easy discovery | Breaks unified chat concept | Rejected |
| D: Top Dropdowns | Minimal UI footprint | Not mobile-optimized | Rejected |

**Decision: Left Drawer (Option A)** — matches the Gemini aesthetic, keeps chat screen clean, familiar pattern for Android users. The drawer contains conversations, spaces, and navigation links.

---

## 6. Implementation Phases

### Phase 1: Foundation (this phase)
- Project scaffold (new Android project based on Gallery)
- Core chat screen with streaming
- Navigation drawer with conversation history
- Model loading and selection (Gemma 4 + Gemini cloud)
- Quick action chips
- Dark/light theme
- Basic onboarding (model download)

### Phase 2: Intelligence Layer
- Tool calling framework (ToolRegistry)
- Per-chat settings with persistence
- Knowledge spaces (multi-space support)
- File browser
- Voice conversation mode
- Media attachments (camera/gallery)
- Cloud escalation logic

### Phase 3: Polish & Production
- Smart chips with live data
- Auto-generated conversation titles
- Search conversations
- Suggested follow-up messages
- Message actions (copy, regenerate, delete, share)
- Performance optimization
- Accessibility audit

---

## 7. Gallery Reference Map

Key files in `.gallery-ref/Android/src/` to reference during implementation:

| Feature | Gallery File | What to Extract |
|---------|-------------|----------------|
| Chat UI | `ui/common/chat/ChatPanel.kt`, `ChatView.kt` | Message list rendering, streaming display |
| Message rendering | `ui/common/chat/ChatMessage.kt` | Message data model, content types |
| Model picker | `ui/common/ModelPickerChip.kt`, `ModelPicker.kt` | Model selection UI pattern |
| Thinking indicator | `ui/common/RotationalLoader.kt` | Loading animation |
| Markdown | `ui/common/MarkdownText.kt` | Rich text rendering |
| Audio recording | `ui/common/chat/AudioRecorderPanel.kt` | Voice input |
| Audio playback | `ui/common/chat/AudioPlaybackPanel.kt` | Voice output |
| LLM inference | `runtime/LlmModelHelper.kt` | Model loading, token streaming |
| Agent tools | `customtasks/agentchat/AgentTools.kt` | Tool calling patterns |
| Skill management | `customtasks/agentchat/SkillManagerViewModel.kt` | Extensible capabilities |
| Model data | `data/Model.kt`, `data/Tasks.kt` | Model metadata, task definitions |
| Download mgmt | `data/DownloadRepository.kt` | Model download, progress tracking |
| Settings | `data/DataStoreRepository.kt` | Preferences persistence |
| Camera | `ui/common/LiveCameraView.kt` | Camera integration |
| Config | `data/Config.kt`, `data/ConfigValue.kt` | App configuration patterns |

---

## 8. Design Handoff Checklist

For each screen, the design (in Stitch) should specify:

- [ ] Light theme variant
- [ ] Dark theme variant
- [ ] Empty state
- [ ] Loading state
- [ ] Error state
- [ ] Tablet/landscape adaptation (stretch goal)
- [ ] Touch target sizes (minimum 48dp)
- [ ] Typography scale (Material3 type scale)
- [ ] Color tokens (Material3 color scheme)
- [ ] Animation/transition notes (duration, easing)
- [ ] Accessibility: contrast ratios, content descriptions

---

## 9. Non-Functional Requirements

| Requirement | Target |
|------------|--------|
| Cold start time | < 3 seconds (before model load) |
| Model load time | < 10 seconds (Gemma 4) |
| First token latency | < 500ms after model loaded |
| Chat message render | < 100ms |
| Model switch | < 500ms (already downloaded) |
| Memory usage | < 200MB (excluding model) |
| APK size | < 30MB (excluding model downloads) |
| Offline capable | Full chat + local model, no internet needed |
| Min Android | API 29 (Android 10) |

---

## 10. Open Design Questions

These need to be resolved during the design phase in Stitch:

1. **Quick action chip behavior:** Do chips disappear after first use in a conversation, or stay persistent?
2. **Conversation title:** Auto-generated from first message, or always "New Chat" until user renames?
3. **Space switching UX:** Does switching spaces start a new chat automatically, or show the last conversation in that space?
4. **Model download during onboarding:** Block the app until download completes, or allow exploration with a "model downloading" banner?
5. **Tool result cards:** Collapsible (show summary, expand for details) or always fully expanded?
6. **Voice mode transition:** Full-screen takeover or bottom sheet overlay on chat?
7. **Image in messages:** Inline thumbnail size — small (64dp), medium (120dp), or large (full-width)?
8. **Drawer gesture area:** Full left edge swipe, or only from hamburger icon?
