package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.IPublisher

interface IChatMessageService: IPublisher<MutableList<Message>> {
    fun publishMessage(message: Message)
}