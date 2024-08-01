package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val index: Int,
    val message: CompletionMessage,
    val logprobs: Logprobs?,
    val finish_reason: String
)