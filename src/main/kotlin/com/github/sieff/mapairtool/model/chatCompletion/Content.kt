package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class Content (
    val token: String,
    val logprob: Int,
    val bytes: List<Byte>,
    val top_logprobs: List<TopLogprob>
)