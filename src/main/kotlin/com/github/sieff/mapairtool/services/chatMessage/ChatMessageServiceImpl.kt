package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.intellij.openapi.project.Project

class ChatMessageServiceImpl(project: Project): ChatMessageService, APublisher<Message>() {
    private val messages: MutableList<Message> = mutableListOf()

    override fun publishMessage(message: Message) {
        messages.add(message)
        publish(message)
    }

    override fun getMessages(): List<Message> {
        return messages
    }
}
