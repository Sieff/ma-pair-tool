package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class TopLogprob (
    val token: String,
    val logprob: Int,
    val bytes: List<Byte>
)
