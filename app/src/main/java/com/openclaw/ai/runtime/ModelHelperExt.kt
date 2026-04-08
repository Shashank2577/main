package com.openclaw.ai.runtime

import com.openclaw.ai.data.Model

/**
 * Extension functions for [LlmModelHelper].
 */

/**
 * Runs inference for the given [model].
 */
fun LlmModelHelper.runInference(
  model: Model,
  input: String,
  resultListener: ResultListener,
  cleanUpListener: CleanUpListener,
  onError: (message: String) -> Unit,
) {
  runInference(
    model = model,
    input = input,
    resultListener = resultListener,
    cleanUpListener = cleanUpListener,
    onError = onError,
    images = listOf(),
    audioClips = listOf(),
  )
}
