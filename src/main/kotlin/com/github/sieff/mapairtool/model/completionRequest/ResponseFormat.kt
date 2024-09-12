package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class ResponseFormat(
    val type: String,
    val json_schema: JsonSchema
)
