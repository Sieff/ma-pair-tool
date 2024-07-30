package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)