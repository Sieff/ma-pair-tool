package com.github.sieff.mapairtool.services.chatMessage

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.google.gson.Gson
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ChatMessageServiceImpl(project: Project): ChatMessageService, APublisher<Message>() {
    private val messages: MutableList<Message> = mutableListOf()
    private var browsers: MutableList<JBCefBrowser> = mutableListOf()
    private val gson = Gson()

    override fun publishMessage(message: Message) {
        messages.add(message)
        browsers.forEach {
            println("Sending message to browser: ${message.message}")
            it.cefBrowser.executeJavaScript("window.setData(${encodeMessagesToJson(messages)})", it.cefBrowser.url, 0)
        }
        publish(message)
    }

    override fun getMessages(): List<Message> {
        return messages
    }

    override fun addBrowser(browser: JBCefBrowser) {
        this.browsers.add(browser)
    }

    override fun removeBrowser(browser: JBCefBrowser) {
        this.browsers.remove(browser)
    }

    private fun encodeMessagesToJson(messages: List<Message>): String {
        return gson.toJson(Json.encodeToString(messages))
    }
}
