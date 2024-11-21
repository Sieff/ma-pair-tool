package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("QuickReactionMessage")
data class QuickReactionMessage (
    override val origin: MessageOrigin,
    override val message: String,
    val quickReaction: Boolean,
): BaseMessage()