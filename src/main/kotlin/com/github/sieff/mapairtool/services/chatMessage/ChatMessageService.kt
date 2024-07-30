package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.IPublisher

interface ChatMessageService: IPublisher<Message> {
    fun publishMessage(message: Message)
    fun getMessages(): List<Message>
}