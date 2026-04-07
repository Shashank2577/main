package com.openclaw.ai.runtime

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolParameter
import com.openclaw.ai.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeminiModelHelper"
private const val GEMINI_API_BASE = "https://generativelanguage.googleapis.com"

@Singleton
class GeminiModelHelper @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : LlmModelHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Keyed by model id: mutable list of {role, parts} JSONObjects
    private val conversationHistory: MutableMap<String, MutableList<JSONObject>> = mutableMapOf()

    // Active calls keyed by model id for cancellation
    private val activeCalls: MutableMap<String, Call> = mutableMapOf()

    override fun initialize(
        context: Context,
        model: ModelInfo,
        supportImage: Boolean,
        supportAudio: Boolean,
        onDone: (String) -> Unit,
        systemInstruction: String?,
        tools: List<ToolDefinition>,
        coroutineScope: CoroutineScope?,
    ) {
        conversationHistory[model.id] = mutableListOf()
        Log.d(TAG, "Initialized cloud model '${model.id}'")
        onDone("")
    }

    override fun resetConversation(
        model: ModelInfo,
        supportImage: Boolean,
        supportAudio: Boolean,
        systemInstruction: String?,
        tools: List<ToolDefinition>,
    ) {
        conversationHistory[model.id] = mutableListOf()
        Log.d(TAG, "Reset conversation for model '${model.id}'")
    }

    override fun cleanUp(model: ModelInfo, onDone: () -> Unit) {
        activeCalls.remove(model.id)?.cancel()
        conversationHistory.remove(model.id)
        Log.d(TAG, "Cleaned up model '${model.id}'")
        onDone()
    }

    override fun stopResponse(model: ModelInfo) {
        activeCalls.remove(model.id)?.cancel()
        Log.d(TAG, "Stopped response for model '${model.id}'")
    }

    override fun runInference(
        model: ModelInfo,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        onError: (message: String) -> Unit,
        images: List<Bitmap>,
        audioClips: List<ByteArray>,
        coroutineScope: CoroutineScope?,
        extraContext: Map<String, String>?,
    ) {
        val scope = coroutineScope ?: CoroutineScope(Dispatchers.IO)
        scope.launch(Dispatchers.IO) {
            val apiKey = settingsRepository.getGeminiApiKey()
            if (apiKey.isNullOrBlank()) {
                onError("Gemini API key is not set. Please add it in Settings.")
                return@launch
            }

            val history = conversationHistory.getOrPut(model.id) { mutableListOf() }

            // Build parts for the new user turn
            val userParts = JSONArray()
            for (image in images) {
                val bytes = image.toJpegByteArray()
                val encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                userParts.put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject()
                            .put("mime_type", "image/jpeg")
                            .put("data", encoded)
                    )
                )
            }
            if (input.trim().isNotEmpty()) {
                userParts.put(JSONObject().put("text", input))
            }

            val userTurn = JSONObject()
                .put("role", "user")
                .put("parts", userParts)
            history.add(userTurn)

            val requestBody = buildRequestBody(model, history)
            val url = "$GEMINI_API_BASE/v1beta/models/${model.id}:streamGenerateContent" +
                "?alt=sse&key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val call = client.newCall(request)
            activeCalls[model.id] = call

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activeCalls.remove(model.id)
                    if (call.isCanceled()) {
                        Log.i(TAG, "Request cancelled for model '${model.id}'")
                        resultListener("", true, null)
                    } else {
                        Log.e(TAG, "Network error for model '${model.id}'", e)
                        onError("Network error: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    activeCalls.remove(model.id)
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: ""
                        val errorMsg = when (response.code) {
                            401 -> "Invalid API key. Please check your Gemini API key in Settings."
                            429 -> "Rate limit exceeded. Please wait and try again."
                            500, 503 -> "Gemini server error (${response.code}). Please try again."
                            else -> "API error ${response.code}: $errorBody"
                        }
                        Log.e(TAG, "HTTP ${response.code} for model '${model.id}': $errorBody")
                        onError(errorMsg)
                        return
                    }

                    val fullModelText = StringBuilder()
                    try {
                        val source = response.body?.source() ?: run {
                            onError("Empty response body")
                            return
                        }

                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (line.startsWith("data: ")) {
                                val json = line.removePrefix("data: ").trim()
                                if (json == "[DONE]") break
                                if (json.isBlank()) continue

                                try {
                                    val obj = JSONObject(json)
                                    val candidates = obj.optJSONArray("candidates") ?: continue
                                    val candidate = candidates.optJSONObject(0) ?: continue
                                    val content = candidate.optJSONObject("content") ?: continue
                                    val parts = content.optJSONArray("parts") ?: continue

                                    for (i in 0 until parts.length()) {
                                        val part = parts.optJSONObject(i) ?: continue
                                        val text = part.optString("text", "")
                                        if (text.isNotEmpty()) {
                                            fullModelText.append(text)
                                            resultListener(text, false, null)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to parse SSE chunk: $json", e)
                                }
                            }
                        }

                        // Append model response to history
                        if (fullModelText.isNotEmpty()) {
                            val modelTurn = JSONObject()
                                .put("role", "model")
                                .put(
                                    "parts",
                                    JSONArray().put(JSONObject().put("text", fullModelText.toString()))
                                )
                            history.add(modelTurn)
                        }

                        resultListener("", true, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading SSE stream", e)
                        onError("Error reading response: ${e.message}")
                    }
                }
            })
        }
    }

    private fun buildRequestBody(model: ModelInfo, history: List<JSONObject>): JSONObject {
        val contentsArray = JSONArray()
        for (turn in history) {
            contentsArray.put(turn)
        }

        val generationConfig = JSONObject()
            .put("temperature", model.defaultTemperature)
            .put("topK", model.defaultTopK)
            .put("topP", model.defaultTopP)
            .put("maxOutputTokens", model.defaultMaxTokens)

        return JSONObject()
            .put("contents", contentsArray)
            .put("generationConfig", generationConfig)
    }

    private fun Bitmap.toJpegByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return stream.toByteArray()
    }
}
