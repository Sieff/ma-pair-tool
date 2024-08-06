package com.github.sieff.mapairtool.services.cefBrowser

import com.github.sieff.mapairtool.model.dataPacket.DataPacketSerializer
import com.github.sieff.mapairtool.model.dataPacket.DataPacketType
import com.github.sieff.mapairtool.model.dataPacket.UpdateMessagesPacket
import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class CefBrowserServiceImpl(
    project: Project
): CefBrowserService {
    private val chatMessageService = project.service<ChatMessageService>()
    //private val gson = Gson()

    override var toolWindowBrowser: JBCefBrowser? = null
    override var widgetBrowser: JBCefBrowser? = null

    init {
        chatMessageService.subscribe(this);
    }

    override fun sendMessages(state: ChatMessageState) {
        val packet = UpdateMessagesPacket(state.messages, state.temporaryMessage, DataPacketType.UPDATE_MESSAGES)

        println("Sending messages to browser")
        if (toolWindowBrowser != null) {
            toolWindowBrowser!!.cefBrowser.executeJavaScript(
                "window.sendDataPacket(${encodeMessagesToJson(packet)})",
                toolWindowBrowser!!.cefBrowser.url,
                0
            )
        }
        if (widgetBrowser != null) {
            widgetBrowser!!.cefBrowser.executeJavaScript(
                "window.sendDataPacket(${encodeMessagesToJson(packet)})",
                widgetBrowser!!.cefBrowser.url,
                0
            )
        }
    }

    override fun notify(data: ChatMessageState) {
        sendMessages(data)
    }

    override fun sendCurrentState() {
        sendMessages(chatMessageService.getState())
    }

    private fun encodeMessagesToJson(packet: UpdateMessagesPacket): String {
        //return gson.toJson(DataPacketSerializer.json.encodeToString(packet))
        println("Messages as json: ${DataPacketSerializer.json.encodeToString(packet)}")
        return DataPacketSerializer.json.encodeToString(packet)
    }
}
