package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageSimilarity(
    val similarity: Float
)
