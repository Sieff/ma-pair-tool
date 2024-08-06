package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Message")
data class Message (
    override val origin: MessageOrigin,
    override val message: String
): BaseMessage()