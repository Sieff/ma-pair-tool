package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("ProactiveMessage")
data class ProactiveMessage(
    override val origin: MessageOrigin,
    override val message: String,
    val emotion: Emotion,
    val reactions: List<String>,
    val proactive: Boolean,
    val thought: String,
    val necessity: Int
): BaseMessage() {
    fun toAssistantMessage(): AssistantMessage {
        return AssistantMessage(origin, message, emotion, reactions, proactive)
    }
}