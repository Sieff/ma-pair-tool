package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class CompletionRequest (
    val model: String,
    val messages: List<RequestMessage>
)