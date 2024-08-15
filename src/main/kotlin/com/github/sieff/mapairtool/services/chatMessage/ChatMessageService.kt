package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.model.message.BaseMessage
import com.github.sieff.mapairtool.util.observerPattern.publisher.Publisher

interface ChatMessageService: Publisher<ChatMessageState> {
    fun addMessage(message: BaseMessage)
    fun getMessages(): List<BaseMessage>
    fun getState(): ChatMessageState
}