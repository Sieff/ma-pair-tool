package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AssistantMessage")
data class AssistantMessage (
    override val origin: MessageOrigin,
    override val message: String,
    val emotion: Emotion,
    val quickReactions: List<String>
): BaseMessage()