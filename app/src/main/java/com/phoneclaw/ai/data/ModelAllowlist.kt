/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phoneclaw.ai.data

import com.google.gson.annotations.SerializedName

/**
 * Represents the allowlist of models that are allowed to be downloaded and used in the app.
 */
data class ModelAllowlist(
  @SerializedName("models") val models: List<ModelAllowlistEntry> = emptyList()
)

data class ModelDefaultConfig(
  @SerializedName("topK") val topK: Int = 64,
  @SerializedName("topP") val topP: Float = 0.95f,
  @SerializedName("temperature") val temperature: Float = 1.0f,
  @SerializedName("maxTokens") val maxTokens: Int = 512,
  @SerializedName("maxContextLength") val maxContextLength: Int = 0,
  @SerializedName("accelerators") val accelerators: String = "cpu",
  @SerializedName("visionAccelerator") val visionAccelerator: String = "GPU",
)

data class ModelAllowlistEntry(
  @SerializedName("name") val name: String,
  // New schema fields
  @SerializedName("modelId") val modelId: String = "",
  @SerializedName("modelFile") val modelFile: String = "",
  @SerializedName("description") val description: String = "",
  @SerializedName("commitHash") val commitHash: String = "",
  @SerializedName("minDeviceMemoryInGb") val minDeviceMemoryInGb: Int? = null,
  @SerializedName("defaultConfig") val defaultConfig: ModelDefaultConfig = ModelDefaultConfig(),
  // Common fields
  @SerializedName("sizeInBytes") val sizeInBytes: Long = 0L,
  @SerializedName("llmSupportImage") val llmSupportImage: Boolean = false,
  @SerializedName("llmSupportAudio") val llmSupportAudio: Boolean = false,
  @SerializedName("llmSupportThinking") val llmSupportThinking: Boolean = false,
  @SerializedName("disabled") val disabled: Boolean = false,
) {
  fun toModel(): Model {
    // Build HuggingFace download URL from modelId + commitHash + modelFile
    val downloadUrl = if (modelId.isNotEmpty() && commitHash.isNotEmpty() && modelFile.isNotEmpty()) {
      "https://huggingface.co/$modelId/resolve/$commitHash/$modelFile"
    } else {
      ""
    }

    val acceleratorList = defaultConfig.accelerators.split(",").map { it.trim() }.map {
      when (it.uppercase()) {
        "GPU" -> Accelerator.GPU
        "NPU" -> Accelerator.NPU
        else -> Accelerator.CPU
      }
    }

    val visionAcc = when (defaultConfig.visionAccelerator.uppercase()) {
      "GPU" -> Accelerator.GPU
      "NPU" -> Accelerator.NPU
      else -> Accelerator.CPU
    }

    return Model(
      name = name,
      displayName = name,
      info = description,
      url = downloadUrl,
      sizeInBytes = sizeInBytes,
      downloadFileName = modelFile.ifEmpty { "_" },
      version = commitHash.take(8).ifEmpty { "_" },
      isLlm = true,
      llmSupportImage = llmSupportImage,
      llmSupportAudio = llmSupportAudio,
      llmSupportThinking = llmSupportThinking,
      llmMaxToken = defaultConfig.maxTokens,
      accelerators = acceleratorList,
      visionAccelerator = visionAcc,
      minDeviceMemoryInGb = minDeviceMemoryInGb,
      runtimeType = if (modelFile.endsWith(".litertlm")) RuntimeType.LITERT_LM else RuntimeType.UNKNOWN,
    )
  }

  fun isCompatible(): Boolean {
    if (disabled) return false
    return true
  }
}
