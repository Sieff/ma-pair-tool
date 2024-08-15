package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher


class ChatMessageServiceImpl: ChatMessageService, APublisher<ChatMessageState>() {
    private val messages: MutableList<BaseMessage> = mutableListOf()
    private var temporaryMessage: AssistantMessage? = null;

    override fun addMessage(message: BaseMessage) {
        if (message.origin == MessageOrigin.AGENT) {
            val quickReactions = mutableListOf("Yes", "No", "Maybe")
            temporaryMessage = AssistantMessage(message.origin, message.message, Emotion.HAPPY, quickReactions, false);

            messages.add(AssistantMessage(message.origin, message.message, Emotion.HAPPY, quickReactions, true))
            messages.add(AssistantMessage(message.origin, message.message, Emotion.HAPPY, quickReactions, true))
        }

        messages.add(message)
        publish(ChatMessageState(messages, temporaryMessage))
    }

    override fun getMessages(): List<BaseMessage> {
        return messages
    }

    override fun getState(): ChatMessageState {
        return ChatMessageState(messages, temporaryMessage)
    }
}
