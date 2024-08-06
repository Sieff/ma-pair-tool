package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.Serializable

@Serializable
sealed class BaseMessage {
    abstract val origin: MessageOrigin
    abstract val message: String
}