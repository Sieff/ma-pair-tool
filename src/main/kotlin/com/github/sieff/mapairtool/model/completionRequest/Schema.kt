package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class Schema(
    val type: String,
    @Serializable(with = PropertiesSerializer::class)
    val properties: Properties,
    val required: List<String>,
)
