package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class RequestMessage (
    val content: String,
    val role: String
)