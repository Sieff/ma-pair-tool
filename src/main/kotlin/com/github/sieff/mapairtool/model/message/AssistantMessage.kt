package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AssistantMessage")
data class AssistantMessage (
    override val origin: MessageOrigin,
    val phase: Phase,
    override val message: String,
    val emotion: Emotion,
    val reactions: List<String>,
    val proactive: Boolean,
): BaseMessage()