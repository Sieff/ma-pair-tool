package com.github.sieff.mapairtool.services.cefBrowser

import com.github.sieff.mapairtool.model.dataPacket.*
import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.services.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import javax.swing.SwingUtilities


class CefBrowserServiceImpl(
    private val project: Project
): CefBrowserService {
    private val chatMessageService = project.service<ChatMessageService>()

    private val logger = Logger(this.javaClass.simpleName)

    override var toolWindowBrowser: JBCefBrowser? = null
    override var widgetBrowser: JBCefBrowser? = null

    init {
        chatMessageService.subscribe(this)
    }

    override fun sendMessages(state: ChatMessageState) {
        val packet = UpdateMessagesPacket(state.messages, state.widgetMessage, DataPacketType.UPDATE_MESSAGES)

        logger.info("Sending messages to browser")

        sendPacketToBrowser(toolWindowBrowser, packet)

        sendPacketToBrowser(widgetBrowser, packet)
    }

    override fun notify(data: ChatMessageState) {
        sendMessages(data)
    }

    override fun sendCurrentState() {
        sendMessages(chatMessageService.getState())
    }

    override fun requestToolWindowFocus() {
        SwingUtilities.invokeLater {
            ToolWindowManager.getInstance(project).getToolWindow("Pair Tool")?.show()
            toolWindowBrowser?.component?.requestFocus()

            val packet = RequestTextInputFocusPacket(DataPacketType.REQUEST_TEXT_INPUT_FOCUS)

            sendPacketToBrowser(toolWindowBrowser, packet)
        }
    }

    private fun sendPacketToBrowser(browser: JBCefBrowser?, packet: DataPacket) {
        browser?.cefBrowser?.executeJavaScript(
            "window.sendDataPacket(${encodePacketToJson(packet)})",
            browser.cefBrowser.url,
            0
        )
    }

    private fun encodePacketToJson(packet: DataPacket): String {
        logger.debug("Packet as json: ${DataPacketSerializer.json.encodeToString(packet)}")
        return DataPacketSerializer.json.encodeToString(packet)
    }
}
