package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.IPublisher

interface IChatMessageService: IPublisher<Message> {
    fun publishMessage(message: Message)
    fun getMessages(): List<Message>
}