package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


class ChatMessageServiceImpl(val project: Project): ChatMessageService, APublisher<ChatMessageState>() {
    private val logWriterService = project.service<LogWriterService>()
    private var messages: MutableList<BaseMessage> = mutableListOf()
    private var widgetMessage: AssistantMessage? = null

    override fun addMessage(message: BaseMessage) {
        if (message.origin == MessageOrigin.AGENT) {
            if (message is AssistantMessage) {
                widgetMessage = message
            } else {
                widgetMessage = null
            }
        }

        logWriterService.logMessage(message)
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
        messages = mutableListOf()
        widgetMessage = null
        publish(ChatMessageState(messages, widgetMessage))
    }

    override fun getLastAgentMessage(): BaseMessage {
        var lastMessage: BaseMessage = Message(MessageOrigin.AGENT, "")
        for (message in messages) {
            if (message.origin == MessageOrigin.AGENT) {
                lastMessage = message
            }
        }
        return lastMessage
    }

    override fun countUnansweredMessages(): Long {
        var count: Long = 0
        for (message in messages) {
            if (message is AssistantMessage && message.proactive) {
                count++
            } else {
                count = 0
            }
        }
        return count
    }
}
