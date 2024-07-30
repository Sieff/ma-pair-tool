package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class CompletionMessage(
    val role: String,
    val content: String
)