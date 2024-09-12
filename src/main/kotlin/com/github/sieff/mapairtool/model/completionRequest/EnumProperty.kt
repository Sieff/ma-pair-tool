package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class EnumProperty(
    val type: String,
    val enum: List<String>
)
