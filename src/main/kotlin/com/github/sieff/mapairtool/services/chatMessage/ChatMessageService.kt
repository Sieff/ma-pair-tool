package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.observer.IObserver
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ChatMessageService(project: Project): IChatMessageService, APublisher<Message>() {
    private val messages: MutableList<Message> = mutableListOf()

    override fun publishMessage(message: Message) {
        messages.add(message)
        publish(message)
    }

    override fun getMessages(): List<Message> {
        return messages
    }
}
