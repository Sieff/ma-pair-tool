package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatMessageServiceImpl(project: Project): ChatMessageService, APublisher<Message>() {
    private val messages: MutableList<Message> = mutableListOf()
    private var browser: JBCefBrowser? = null

    override fun publishMessage(message: Message) {
        println("Sending message to browser: ${message.message}")
        messages.add(message)
        browser?.cefBrowser?.executeJavaScript("window.setData('${Json.encodeToString(messages)}')", browser?.cefBrowser?.url, 0)
        publish(message)
    }

    override fun getMessages(): List<Message> {
        return messages
    }

    override fun setBrowser(browser: JBCefBrowser) {
        this.browser = browser
    }
}
