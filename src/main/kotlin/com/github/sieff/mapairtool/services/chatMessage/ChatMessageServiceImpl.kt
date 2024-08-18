package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher


class ChatMessageServiceImpl: ChatMessageService, APublisher<ChatMessageState>() {
    private val messages: MutableList<BaseMessage> = mutableListOf()
    private var widgetMessage: AssistantMessage? = null;

    override fun addMessage(message: BaseMessage) {
        if (message.origin == MessageOrigin.AGENT) {
            message as AssistantMessage
            widgetMessage = AssistantMessage(message.origin, message.message, message.emotion, message.reactions, message.proactive);
        }

        messages.add(message)
        publish(ChatMessageState(messages, widgetMessage))
    }

    override fun getMessages(): List<BaseMessage> {
        return messages
    }

    override fun getState(): ChatMessageState {
        return ChatMessageState(messages, widgetMessage)
    }
}
