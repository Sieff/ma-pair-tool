package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class JsonSchema(
    val name: String,
    val schema: Schema,
    val strict: Boolean
)
