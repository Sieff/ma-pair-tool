package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.Publisher
import com.intellij.ui.jcef.JBCefBrowser

interface ChatMessageService: Publisher<Message> {
    fun publishMessage(message: Message)
    fun getMessages(): List<Message>
    fun addBrowser(browser: JBCefBrowser)
    fun removeBrowser(browser: JBCefBrowser)
}