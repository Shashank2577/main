# Gallery LLM Architecture Audit

**Source**: google-ai-edge/gallery (cloned at `/tmp/gallery-audit`, depth=1)
**Date**: 2026-04-07
**Auditor**: llm-model-analyst

---

## 1. LLM Architecture Overview

Gallery is a **100% local, on-device** LLM app. There is **no cloud/API provider**. All inference runs via:

- **LiteRT LM** (`com.google.ai.edge.litertlm`) — Google's on-device LLM runtime
- Supports **CPU, GPU, NPU** backends (hardware acceleration)
- Only `.task` and `.litertlm` file formats supported
- Models downloaded from HuggingFace or locally sideloaded

There is **no Gemini API (cloud) integration** in the Gallery Android app. The name "Gallery" refers to showcasing on-device models only.

---

## 2. Core Provider Interface

### `LlmModelHelper` (interface)
**Location**: `runtime/LlmModelHelper.kt`

This is Gallery's equivalent of OpenClaw's `ModelRouter`. It is a clean interface with 5 operations:

```kotlin
interface LlmModelHelper {
    fun initialize(context, model, supportImage, supportAudio, onDone, systemInstruction?, tools?, enableConversationConstrainedDecoding?, coroutineScope?)
    fun resetConversation(model, supportImage, supportAudio, systemInstruction?, tools?, enableConversationConstrainedDecoding?)
    fun cleanUp(model, onDone)
    fun runInference(model, input, resultListener, cleanUpListener, onError, images?, audioClips?, coroutineScope?, extraContext?)
    fun stopResponse(model)
}
```

**Key types**:
```kotlin
typealias ResultListener = (partialResult: String, done: Boolean, partialThinkingResult: String?) -> Unit
typealias CleanUpListener = () -> Unit
```

### Only One Concrete Implementation
**`LlmChatModelHelper`** (`ui/llmchat/LlmChatModelHelper.kt`) — a Kotlin `object` (singleton).

**`ModelHelperExt.kt`** routes all models to this single implementation:
```kotlin
val Model.runtimeHelper: LlmModelHelper
    get() = LlmChatModelHelper
```

**No multi-provider routing exists** — Gallery has no ModelRouter equivalent that switches between providers. All models use the same LiteRT LM backend.

---

## 3. Model Management

### Model Data Class (`data/Model.kt`)

Key capability flags stored directly on the model:
```kotlin
data class Model(
    val name: String,
    val displayName: String,
    val runtimeType: RuntimeType,       // LITERT_LM or UNKNOWN
    val isLlm: Boolean,
    val llmSupportImage: Boolean,       // multimodal capability flag
    val llmSupportAudio: Boolean,
    val llmSupportThinking: Boolean,    // chain-of-thought flag
    val llmMaxToken: Int,
    val accelerators: List<Accelerator>, // CPU, GPU, NPU
    val configs: List<Config>,          // runtime-adjustable params
    var instance: Any? = null,          // holds LlmModelInstance at runtime
    var initializing: Boolean = false,
    var configValues: Map<String, Any>, // current config state
)
```

### Model Instance (`LlmModelInstance`)
```kotlin
data class LlmModelInstance(val engine: Engine, var conversation: Conversation)
```

The `Engine` is the LiteRT LM engine. A `Conversation` object manages the context window. When conversation is reset, the old conversation is closed and a new one is created from the same engine — avoiding full model reload.

### How Models Are Loaded

1. `ModelManagerViewModel.initializeModel()` is called when user navigates to a task+model.
2. Delegates to `CustomTask.initializeModelFn()` (pluggable per task type).
3. `LlmChatModelHelper.initialize()` creates `Engine(engineConfig)` + `engine.createConversation()`.
4. Model instance stored in `model.instance`.
5. Initialization status tracked in `modelInitializationStatus` StateFlow map.

### Caching Strategy
- **No pre-loading or background loading** — model loads on-demand when selected.
- **Single model active at a time** — cleanup triggered when switching.
- `model.cleanUpAfterInit` flag prevents race: if cleanup requested during init, it runs after init completes.
- No LRU cache or model pool.

### Model Selection UI
- `GlobalModelManager` composable shows all models in a `LazyColumn`.
- Built-in and imported models shown in separate sections.
- Models sorted by `displayName`, best-for-task model pinned to top.
- If model belongs to multiple tasks, bottom sheet shown to select task.
- Memory warning shown if `model.minDeviceMemoryInGb` exceeds device RAM.

### Local vs Cloud
- **Only local on-device models**. No cloud API option.
- Models come from HuggingFace (public or gated via OAuth access token).
- Local import via file picker (`.task` or `.litertlm` files only).
- Path override field `localFileRelativeDirPathOverride` for dev/demo use.

---

## 4. Initialization Flow (Engine + Conversation)

`LlmChatModelHelper.initialize()` key logic:

```kotlin
// 1. Read per-model config values
val maxTokens = model.getIntConfigValue(ConfigKeys.MAX_TOKENS, DEFAULT_MAX_TOKEN) // default 1024
val topK      = model.getIntConfigValue(ConfigKeys.TOPK, DEFAULT_TOPK)           // default 64
val topP      = model.getFloatConfigValue(ConfigKeys.TOPP, DEFAULT_TOPP)         // default 0.95
val temperature = model.getFloatConfigValue(ConfigKeys.TEMPERATURE, 1.0f)
val accelerator = model.getStringConfigValue(ConfigKeys.ACCELERATOR, "GPU")

// 2. Map to LiteRT backend
val preferredBackend = when(accelerator) {
    "CPU" -> Backend.CPU()
    "GPU" -> Backend.GPU()
    "NPU" -> Backend.NPU(nativeLibraryDir)
}

// 3. Create engine
val engineConfig = EngineConfig(
    modelPath = model.getPath(context),
    backend = preferredBackend,
    visionBackend = if (supportImage) visionBackend else null,
    audioBackend  = if (supportAudio) Backend.CPU() else null,
    maxNumTokens  = maxTokens,
)
val engine = Engine(engineConfig)
engine.initialize()

// 4. Create conversation with sampler config
val conversation = engine.createConversation(ConversationConfig(
    samplerConfig = SamplerConfig(topK, topP, temperature),
    systemInstruction = systemInstruction,
    tools = tools,
))

model.instance = LlmModelInstance(engine, conversation)
```

**NPU special case**: `SamplerConfig` is set to `null` for NPU backend (NPU doesn't support dynamic sampling params).

---

## 5. Streaming Protocol

Gallery uses LiteRT LM's **async callback** streaming, not Kotlin Flow or coroutines directly:

```kotlin
conversation.sendMessageAsync(
    Contents.of(contents),
    object : MessageCallback {
        override fun onMessage(message: Message) {
            // Called per token/partial chunk
            resultListener(message.toString(), false, message.channels["thought"])
        }
        override fun onDone() {
            resultListener("", true, null)
        }
        override fun onError(throwable: Throwable) {
            if (throwable is CancellationException) {
                resultListener("", true, null) // treat cancel as done
            } else {
                onError("Error: ${throwable.message}")
            }
        }
    },
    extraContext ?: emptyMap(),
)
```

**Thinking channel**: `message.channels["thought"]` — separate channel for chain-of-thought tokens, enabled via `extraContext = mapOf("enable_thinking" to "true")`.

**Stop generation**: `conversation.cancelProcess()` — cooperative cancellation.

### Token Counting / Context Management
- **No explicit token counting API exposed** — Gallery does not count or display token usage.
- Context limit set once via `EngineConfig.maxNumTokens` at initialization.
- Auto-reset conversation supported: `ConfigKeys.RESET_CONVERSATION_TURN_COUNT` config (default 3 turns, up to 30).
- When context is full, LiteRT LM likely raises an error; Gallery handles via `handleError()` which cleans up and re-initializes.

---

## 6. Tool Calling Implementation

### Tool Provider Interface (LiteRT LM)
Tools are passed at conversation creation time:
```kotlin
engine.createConversation(ConversationConfig(
    tools = listOf<ToolProvider>(...),
))
```

### `AgentTools` (`customtasks/agentchat/AgentTools.kt`)
Implements `ToolSet` using annotations:
```kotlin
class AgentTools : ToolSet {
    @Tool(description = "Loads a skill.")
    fun loadSkill(
        @ToolParam(description = "...") skillName: String
    ): Map<String, String> { ... }

    @Tool(description = "Runs JS script")
    fun runJs(
        @ToolParam(description = "...") skillName: String,
        @ToolParam(description = "...") scriptName: String,
        @ToolParam(description = "...") data: String,
    ): Map<String, Any> { ... }

    @Tool(description = "Run an Android intent.")
    fun runIntent(
        @ToolParam(description = "...") intent: String,
        @ToolParam(description = "...") parameters: String,
    ): Map<String, String> { ... }
}
```

**Key patterns**:
- Tool methods use **blocking coroutines** (`runBlocking(Dispatchers.Default)`) — the LiteRT LM runtime calls tools synchronously.
- Results returned as `Map<String, Any>` (auto-serialized by LiteRT LM to JSON for the model).
- **Channel-based action dispatch**: `_actionChannel` (unbounded) sends `AgentAction` objects to the UI coroutine for side effects (show dialog, open webview, etc.).
- Secret management: if tool requires a secret (API key), suspends via `Deferred` until user provides it via dialog.

---

## 7. Multi-Provider Routing

**Gallery has no multi-provider routing.** `ModelHelperExt.kt` is a single line that always returns `LlmChatModelHelper`. There is no switching logic based on model type, network availability, or task type.

The `RuntimeType` enum has two values (`LITERT_LM`, `UNKNOWN`) but there's only one runtime implementation. The extension function ignores `RuntimeType` entirely.

---

## 8. Model Allowlist (model_allowlist.json)

Models are fetched from a remote JSON:
```json
{
  "models": [
    {
      "name": "Gemma-3n-E2B-it-int4",
      "modelId": "google/gemma-3n-E2B-it-litert-preview",
      "modelFile": "gemma-3n-E2B-it-int4.task",
      "sizeInBytes": 3136226711,
      "llmSupportImage": true,
      "defaultConfig": {
        "topK": 64, "topP": 0.95, "temperature": 1.0,
        "maxTokens": 4096, "accelerators": "cpu,gpu"
      },
      "taskTypes": ["llm_chat", "llm_prompt_lab", "llm_ask_image"]
    }
  ]
}
```

Available models (as of audit):
- Gemma-3n-E2B-it-int4 (3.1GB, image support)
- Gemma-3n-E4B-it-int4 (4.4GB, image support)
- Gemma3-1B-IT q4 (554MB)
- Qwen2.5-1.5B-Instruct q8 (1.6GB)

All are `.task` format for MediaPipe/LiteRT LM runtime.

---

## 9. Config System

Models expose runtime-adjustable configs via `List<Config>`:

| Config Key        | Type          | Default  | Triggers Re-init |
|-------------------|---------------|----------|-----------------|
| MAX_TOKENS        | Label/Slider  | 1024     | Yes             |
| TOPK              | NumberSlider  | 64       | Yes             |
| TOPP              | NumberSlider  | 0.95     | Yes             |
| TEMPERATURE       | NumberSlider  | 1.0      | Yes             |
| ACCELERATOR       | SegmentedBtn  | GPU      | Yes             |
| ENABLE_THINKING   | BooleanSwitch | false    | No              |

Config changes that have `needReinitialization=true` automatically trigger model re-init via `ModelManagerViewModel`.

---

## 10. Error Handling for Model Failures

`LlmChatViewModelBase.handleError()`:
1. Removes loading message from chat.
2. Adds `ChatMessageError` with error text.
3. Calls `cleanupModel()` then `initializeModel()` to attempt recovery.
4. Adds `ChatMessageWarning("Session re-initialized")`.

Errors from `Engine()` initialization are caught, cleaned via `cleanUpMediapipeTaskErrorMessage()` (strips MediaPipe internal stack traces), and surfaced via `onDone(errorMessage)`.

---

## 11. Reusability for OpenClaw

### What OpenClaw Can Adopt

| Pattern | Reusability | Notes |
|---------|-------------|-------|
| `LlmModelHelper` interface | **High** | Clean 5-method contract maps well to OpenClaw's `ModelRouter` |
| `ResultListener` typealias | **High** | `(partial: String, done: Boolean, thinking: String?) -> Unit` is simple and sufficient |
| `Model` data class structure | **Medium** | Capability flags (`llmSupportImage`, `llmSupportThinking`) are reusable; OpenClaw needs cloud fields added |
| `LlmModelInstance` (Engine+Conversation) | **Low** | LiteRT LM specific; OpenClaw uses Anthropic SDK |
| Config system (`ConfigKeys`, `createLlmChatConfigs()`) | **High** | Well-designed; can add cloud model params (API key, model variant) |
| `AgentTools` tool calling pattern | **Medium** | Annotation-based `@Tool`/`@ToolParam` is clean; needs adaptation for Anthropic tool schema |
| `ModelManagerViewModel` init/cleanup lifecycle | **High** | Pattern directly adoptable; single-active-model + status tracking works for OpenClaw |
| Conversation reset (close + recreate) | **High** | OpenClaw can mirror this for managing Anthropic SDK session state |
| Channel-based action dispatch for tools | **High** | Decouples tool execution from UI; directly applicable |
| Model allowlist (remote JSON) | **Medium** | Structure is good but OpenClaw needs local + cloud model sections |

### Gaps / What OpenClaw Must Build

1. **Cloud provider adapter** — `LlmModelHelper` interface must gain an `AnthropicModelHelper` implementation (or `ClaudeCloudModelHelper`) alongside the local one.
2. **ModelRouter** — `ModelHelperExt.kt` returns a single helper. OpenClaw needs conditional routing: `if (model.isCloudModel) ClaudeCloudModelHelper else LlmChatModelHelper`.
3. **Token counting** — Gallery has none. OpenClaw needs context window tracking to show usage and handle overflow gracefully.
4. **Streaming via Flow** — Gallery uses raw `MessageCallback`. OpenClaw could wrap this in `callbackFlow` for more idiomatic coroutine integration.
5. **API key management** — Gallery only handles HuggingFace tokens; OpenClaw needs Anthropic API key storage.

---

## 12. Gemini API Integration Details

**There is no Gemini API (cloud) integration in Gallery Android.** The app name is misleading. Everything is on-device via LiteRT (the runtime formerly known as TFLite).

The models are *Gemma* (open-weight, on-device) not *Gemini* (cloud API). Gallery demonstrates running Gemma models locally using LiteRT LM.

---

## 13. Phase Recommendations

### Phase 1 (Unified Chat MVP)

Adopt these Gallery patterns directly:

1. **`LlmModelHelper` interface** — use as-is as the contract for all providers. Add `AnthropicModelHelper` that implements it using the Anthropic SDK streaming API.
2. **`ResultListener` typealias** — adopt verbatim: `(partial: String, done: Boolean, thinking: String?) -> Unit`.
3. **Config system** — adopt `ConfigKeys`, `createLlmChatConfigs()`. Add `ConfigKeys.API_KEY`, `ConfigKeys.CLAUDE_MODEL_VARIANT`.
4. **`ModelManagerViewModel` lifecycle pattern** — single-active-model tracking, init/cleanup with state machine (`NOT_INITIALIZED → INITIALIZING → INITIALIZED | ERROR`).
5. **Error recovery pattern** — cleanup + reinitialize + warning message.
6. **Channel-based tool dispatch** — adopt `_actionChannel: Channel<AgentAction>` for decoupling tool results from UI.

### Phase 2 (Multi-Provider + Local Model Support)

1. **`LlmChatModelHelper` for local models** — integrate LiteRT LM when local model support added.
2. **`ModelRouter`** — extend `ModelHelperExt.kt` pattern to route between cloud and local helpers based on `model.runtimeType`.
3. **Model allowlist** — extend JSON schema to include cloud model entries with `providerType: "cloud"`.
4. **Thinking mode** — adopt `extraContext = mapOf("enable_thinking" to "true")` pattern, map to Anthropic's `thinking` parameter.
5. **Benchmark task** — Gallery's benchmark infrastructure (BenchmarkViewModel, BenchmarkScreen) is a complete example for latency/throughput measurement.

---

## 14. Effort Estimate to Adapt

| Adaptation | Effort |
|-----------|--------|
| Port `LlmModelHelper` interface | 0.5h — copy verbatim |
| Implement `AnthropicModelHelper` | 4–6h — Anthropic SDK streaming + tool result handling |
| Extend `Model` data class for cloud | 1–2h — add API key field, provider type enum |
| Port `ModelManagerViewModel` init lifecycle | 2–3h — adapt to OpenClaw's DI and ViewModel structure |
| Port Config system | 1h — copy + add cloud-specific keys |
| Port `AgentTools` tool pattern | 2–3h — adapt annotations to Anthropic tool schema |
| `ModelRouter` routing logic | 1h — extend `ModelHelperExt.kt` |
| **Total Phase 1** | **~12–16h** |

---

## Summary

Gallery is a clean, well-architected on-device LLM showcase. Its provider abstraction (`LlmModelHelper`), model lifecycle management (`ModelManagerViewModel`), and tool calling patterns are directly adoptable for OpenClaw's unified chat. The main gap is that Gallery has no cloud provider — OpenClaw must implement `AnthropicModelHelper` as a new concrete implementation of the `LlmModelHelper` interface and add routing logic. The `ResultListener` streaming pattern, config system, and error recovery patterns are production-quality and worth adopting verbatim.
