package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


class ChatMessageServiceImpl(val project: Project): ChatMessageService, APublisher<ChatMessageState>() {
    private var messages: MutableList<BaseMessage> = mutableListOf()
    private var widgetMessage: AssistantMessage? = null

    override fun addMessage(message: BaseMessage) {
        if (message.origin == MessageOrigin.AGENT) {
            widgetMessage = message as AssistantMessage
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

    override fun resetMessages() {
        project.service<LogWriterService>().startNewLog()
        messages = mutableListOf()
        widgetMessage = null
        publish(ChatMessageState(messages, widgetMessage))
    }

    override fun removeLastMessage() {
        messages.removeAt(messages.lastIndex)
        publish(ChatMessageState(messages, widgetMessage))
    }
}
