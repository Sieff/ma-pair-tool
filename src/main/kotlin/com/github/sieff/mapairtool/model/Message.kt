package com.github.sieff.mapairtool.model

import kotlinx.serialization.Serializable

@Serializable
data class Message (
    val origin: MessageOrigin,
    val message: String
)