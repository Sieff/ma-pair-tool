package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class ArrayProperty(
    val type: String,
    val items: Property
)
