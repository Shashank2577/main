# Gallery Voice Input & Audio Handling — Audit

_Analyzed: 2026-04-06_
_Gallery source: /tmp/gallery-ref_
_OpenClaw source: /Users/shashanksaxena/Documents/Personal/Code/openClaw-android_

---

## Voice Modes Found in Gallery

Gallery has **three distinct voice input modes** and one **audio playback** system:

| Mode | Gallery Files | Status |
|------|--------------|--------|
| Hold-to-talk (STT) | `HoldToDictate.kt`, `HoldToDictateViewModel.kt`, `TextAndVoiceInput.kt`, `VoiceRecognizerOverlay.kt` | Production-ready |
| Tap-to-record audio clip (raw PCM) | `AudioRecorderPanel.kt`, `MessageBodyAudioClip.kt` | Production-ready |
| Audio playback of recorded clips | `AudioPlaybackPanel.kt`, `MessageBodyAudioClip.kt` | Production-ready |
| Amplitude shader animation | `AudioAnimation.kt` | Production-ready (Android 13+ only) |

**There is NO full voice conversation mode (streaming STT + LLM + TTS loop) in Gallery.**
OpenClaw's `VoiceConversationScreen.kt` is more advanced than anything in Gallery for this mode.

---

## Mode 1: Hold-to-Talk (Push-to-Talk STT)

### How it works

- **`HoldToDictate.kt`** — Composable button. Press + hold starts recognition; release stops and sends; slide off cancels.
- **`HoldToDictateViewModel.kt`** — `@HiltViewModel` wrapping `android.speech.SpeechRecognizer` directly as a `RecognitionListener`.
  - `SpeechRecognizer.ACTION_RECOGNIZE_SPEECH`, `LANGUAGE_MODEL_FREE_FORM`, `en-US` hardcoded locale.
  - `EXTRA_PARTIAL_RESULTS = true` — partial text streamed during hold.
  - `onRmsChanged` → converts dB to 0-65535 amplitude via linear scale → feeds `AudioAnimation`.
  - `stopSpeechRecognition()` has a 500ms delay before calling `stopListening()` to catch trailing audio.
  - Cancel path: `cancelSpeechRecognition()` just sets `recognizing = false`, does not call `stopListening()` (relies on `SpeechRecognizer` cleanup on destroy).
- **`TextAndVoiceInput.kt`** — Wrapper that shows either `BasicTextField` (text mode) or `HoldToDictate` (voice mode), toggled by a keyboard/mic icon. Uses `AnimatedContent` for the transition.
- **`VoiceRecognizerOverlay.kt`** — Full-screen overlay shown while holding. Displays partial text + `AudioAnimation` + "Release to send / Slide up to cancel" instructions.

### Gallery vs OpenClaw comparison

| Feature | Gallery `HoldToDictate` | OpenClaw `VoiceInputButton` |
|---------|------------------------|----------------------------|
| Hold-to-talk gesture | YES — `detectTapGestures(onPress)` | YES — same pattern |
| Partial results shown | YES — in overlay | YES — `partialResult` state |
| Amplitude visualization | YES — drives `AudioAnimation` shader | NO — not implemented |
| ViewModel for state | YES — `@HiltViewModel` | NO — inline Compose state |
| Cancel (slide off) | YES — `CancellationException` catch | NO — not implemented |
| Locale | Hardcoded `en-US` | `Locale.getDefault()` |
| 500ms stop delay | YES | NO |
| Permission handling | `rememberLauncherForActivityResult` | `rememberPermissionState` (Accompanist) |

### Reusable in OpenClaw?

**PARTIAL.** The gesture + recognition logic is near-identical. Gallery adds:
1. Real amplitude tracking via `onRmsChanged` → visual feedback
2. Cancel-by-slide gesture
3. The 500ms stop delay (reduces cut-off words)

The `HoldToDictateViewModel` uses `@HiltViewModel` — **cannot be copied directly**. Must be de-Hilted: replace with a plain class or inline the logic into the composable (as OpenClaw already does).

**Estimated integration effort: 2–3 hours**
- Lift `onRmsChanged` amplitude conversion into OpenClaw's `VoiceInputButton`
- Add cancel-by-slide gesture
- Add 500ms stop delay
- No ViewModel migration needed

---

## Mode 2: Tap-to-Record Audio Clip (Raw PCM)

### How it works

- **`AudioRecorderPanel.kt`** — Inline composable panel (not a modal). Replaces the text input area when the mic icon is tapped in `MessageInputText`.
  - Uses `android.media.AudioRecord` directly (not `MediaRecorder`).
  - Config: `SAMPLE_RATE = 16000 Hz`, `CHANNEL_IN_MONO`, `ENCODING_PCM_16BIT`.
  - Max duration: `MAX_AUDIO_CLIP_DURATION_SEC = 30s`.
  - Recording runs on `Dispatchers.IO` via coroutine; main thread stays unblocked.
  - Output: raw `ByteArray` of PCM 16-bit data (no file, no WAV header).
  - Sends the byte array via `onSendAudioClip(ByteArray)` callback → caller wraps in `ChatMessageAudioClip`.
  - UI: Close button | elapsed seconds with red dot | Mic icon (→ ArrowUpward when recording).
  - `calculatePeakAmplitude()` called per buffer chunk → drives waveform animation.
  - Auto-stops at max duration via `onMaxDurationReached` callback.

- **`ChatMessageAudioClip`** — Message type that carries `audioData: ByteArray` + `sampleRate: Int`.
- **`MessageBodyAudioClip.kt`** — Renders `AudioPlaybackPanel` for audio clip messages.

### Gallery vs OpenClaw comparison

OpenClaw has **no equivalent**. OpenClaw's voice input is STT-only (no raw audio recording, no audio clip messages).

### Is it relevant for OpenClaw?

**Depends on use case.** Gallery sends raw PCM audio to a multimodal LLM that can process audio bytes directly (Gemma-style audio-in). OpenClaw currently uses Gemini API which accepts audio as base64. The `AudioRecorderPanel` pattern is reusable IF:
- The LLM endpoint can accept raw PCM / WAV audio input
- OpenClaw adds an `AudioClip` content part type to `ContentPart`

For the current OpenClaw MVP (text + images via Gemini), this mode is **not needed**.

**Estimated integration effort: 4–5 hours** (if needed)
- Add `AudioRecorderPanel` (de-Task-themed)
- Add `ChatMessageAudioClip` equivalent to OpenClaw's message types
- Add audio `ContentPart` encoding (PCM → WAV or base64)
- Wire into `ChatScreen` input bar toggle

---

## Mode 3: Audio Playback

### How it works

- **`AudioPlaybackPanel.kt`** — Play/stop + waveform visualization + duration text.
  - Uses `android.media.AudioTrack` in `MODE_STATIC` (loads entire clip into memory).
  - Format: PCM 16-bit, mono, matches recorder's `SAMPLE_RATE`.
  - Progress tracked via `audioTrack.playbackHeadPosition` polled every 30ms.
  - Waveform: `AmplitudeBarGraph` composable — 16–48 bars (scaled to clip duration), rendered via `Canvas` with `BlendMode.SrcIn` for progress color overlay.
  - `generateAmplitudeLevels()` — processes raw PCM ByteArray into normalized bar heights.

### Relevant for OpenClaw?

Only needed if OpenClaw adopts raw audio clip recording (Mode 2). Not needed for MVP.

---

## Mode 4: AudioAnimation (Shader-based Waveform)

### How it works

- **`AudioAnimation.kt`** — Uses `android.graphics.RuntimeShader` (AGSL) for a GPU-rendered animated wave that responds to real-time amplitude.
  - **Requires Android 13 (API 33)+** — guarded by `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU`.
  - AGSL shader: renders gradient with Perlin noise-driven bumps when amplitude > 0; slow sine wave when idle.
  - Amplitude normalized: `(amplitude / 32767.0).pow(0.5)` — square root to boost low-level response.
  - Animates amplitude changes with `tween(100ms)` for smoothness.
  - Updates `iTime` uniform via `withFrameMillis` every frame.
  - Colors: yellow/green/red/blue gradient (hardcoded in shader).

### Relevant for OpenClaw?

**YES — high value for visual polish.** Can replace OpenClaw's current Canvas-based orb animation in `VoiceConversationScreen` on API 33+, with a Canvas fallback for older devices.

**Estimated integration effort: 2 hours**
- Copy `AudioAnimation.kt` into OpenClaw
- Replace hardcoded shader colors with OpenClaw's violet/cyan palette
- Add API 33 guard (already present in Gallery code)
- Feed `onRmsChanged` amplitude from `VoiceInputButton` into the animation

---

## Mode 5: Full Voice Conversation (Auto-listen loop)

### Gallery status: NOT PRESENT

Gallery has no full voice conversation mode. There is no:
- Auto-listen after TTS playback
- TTS integration
- State machine (IDLE → LISTENING → PROCESSING → SPEAKING)
- Streaming audio

### OpenClaw status: IMPLEMENTED

`VoiceConversationScreen.kt` + `VoiceInputButton.kt` implement:
- `VoiceState` enum: `IDLE / LISTENING / PROCESSING / SPEAKING`
- `SpeechRecognizer` → `AgentRuntime.sendMessage()` → TTS playback loop
- Auto-listen toggle with 500ms delay after TTS completes
- `TextToSpeech` with markdown stripping for clean audio output
- Full animated orb visualizer (Canvas-based, no shader)
- `TtsHelper` class in `VoiceInputButton.kt`

**OpenClaw is more advanced than Gallery for this mode.**

---

## Dependencies & Permissions

### Gallery voice dependencies (build.gradle)

Gallery uses only Android SDK APIs — **no external audio libraries**:
```
android.speech.SpeechRecognizer      // Hold-to-talk STT
android.media.AudioRecord            // Raw PCM recording
android.media.AudioTrack             // Playback
android.graphics.RuntimeShader       // AGSL shader (API 33+)
android.speech.tts.TextToSpeech      // (not in Gallery, but in OpenClaw)
```

### Manifest permissions needed

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<!-- Already present in OpenClaw -->
```

No `INTERNET` permission needed for on-device STT (uses Google's on-device recognizer if available).

### OpenClaw current state

Already has `RECORD_AUDIO` permission. Already uses `SpeechRecognizer` and `TextToSpeech`. **No new dependencies needed** for any Gallery voice integration.

---

## Conflicts with OpenClaw Voice Code

| Conflict Area | Details |
|--------------|---------|
| **Permission handling** | Gallery uses `rememberLauncherForActivityResult`; OpenClaw uses Accompanist `rememberPermissionState`. **No conflict** — both work; OpenClaw's approach is fine. |
| **SpeechRecognizer lifecycle** | Gallery puts recognizer in a ViewModel (survives config change); OpenClaw creates it in `remember {}` inside composable (destroyed on config change). This is a **minor bug** in OpenClaw — rotating screen during recording destroys recognizer. Fix: move to a `retained` object or `ViewModel`. |
| **Locale** | Gallery hardcodes `en-US`; OpenClaw uses `Locale.getDefault()`. OpenClaw's approach is better. |
| **TTS** | Not in Gallery. OpenClaw's `TtsHelper` class is self-contained. No conflict. |
| **AudioAnimation colors** | Gallery shader hardcodes yellow/green/red/blue. OpenClaw would need to remap to its color tokens (violet/cyan/pink). Easy fix. |

---

## Production Readiness Assessment

| Mode | Gallery | OpenClaw | Production Ready? |
|------|---------|----------|-------------------|
| Hold-to-talk STT | YES | YES (simpler) | YES — both work |
| Raw audio clip recording | YES | NO | Gallery only |
| Audio clip playback | YES | NO | Gallery only |
| AudioAnimation shader | YES (API 33+) | NO | Gallery only |
| Full voice conversation | NO | YES | OpenClaw only |

---

## Recommended Integration Approach

### Priority 1 — Immediate (high value, low effort)

**Adopt `AudioAnimation.kt` for OpenClaw's voice screen:**
- Copy file, remap shader colors to OpenClaw palette (violet/cyan)
- Replace the Canvas orb in `VoiceConversationScreen` on API 33+
- Feed `onRmsChanged` amplitude data from `HoldToDictateViewModel` pattern into it
- Effort: **2 hours**

### Priority 2 — Near-term (moderate effort)

**Upgrade `VoiceInputButton` with Gallery's amplitude + cancel patterns:**
- Add `onRmsChanged` → amplitude tracking to `VoiceInputButton`
- Add cancel-by-slide gesture (catch `CancellationException`)
- Add 500ms stop delay
- Effort: **2–3 hours**

**Fix config-change bug in OpenClaw:**
- Move `SpeechRecognizer` lifecycle out of Compose `remember` into a retained scope
- Effort: **1–2 hours**

### Priority 3 — Deferred (only if audio-in LLM is needed)

**Raw audio clip recording + playback:**
- Only needed if OpenClaw adds audio-input capability to a model
- Full `AudioRecorderPanel` + `AudioPlaybackPanel` port
- Effort: **4–5 hours**

### Do NOT port

- `HoldToDictateViewModel` as a ViewModel — OpenClaw has no Hilt, and the inline approach works fine
- `TextAndVoiceInput` keyboard/voice toggle — OpenClaw's chat input has a different UX paradigm
- Any Gallery audio-to-model pipeline — Gallery sends raw PCM to on-device models; OpenClaw uses Gemini API

---

## Summary

Gallery's voice code is **solid, production-quality STT plumbing** but covers a narrower use case (hold-to-talk feeding a chat model). OpenClaw already surpasses Gallery for the full voice conversation mode.

The single highest-value item to adopt is **`AudioAnimation.kt`** — it's self-contained, visually impressive, and slots directly into `VoiceConversationScreen` with minimal wiring. Everything else is either already covered by OpenClaw or only relevant for future audio-in model support.
