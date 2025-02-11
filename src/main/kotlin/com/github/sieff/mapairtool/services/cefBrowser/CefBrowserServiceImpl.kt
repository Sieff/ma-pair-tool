package com.github.sieff.mapairtool.services.cefBrowser

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.dataPacket.*
import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.util.Logger
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

    private val logger = Logger(this.javaClass)

    override var toolWindowBrowser: JBCefBrowser? = null
    override var widgetBrowser: JBCefBrowser? = null

    var logStatus: Boolean = false
    var studyGroupStatus: Boolean = false
    var apiKeyStatus: Boolean = false

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
            ToolWindowManager.getInstance(project).getToolWindow("Assistant")?.show()
            toolWindowBrowser?.component?.requestFocus()

            val packet = RequestTextInputFocusPacket(DataPacketType.REQUEST_TEXT_INPUT_FOCUS)

            sendPacketToBrowser(toolWindowBrowser, packet)
        }
    }

    override fun requestToolWindow() {
        SwingUtilities.invokeLater {
            ToolWindowManager.getInstance(project).getToolWindow("Assistant")?.show()
        }
    }

    override fun updateLogStatus(status: Boolean) {
        logStatus = status
        updatePluginStatus()
    }

    override fun updateSettingsStatus(apiKeyReady: Boolean, studyGroupReady: Boolean) {
        apiKeyStatus = apiKeyReady
        studyGroupStatus = studyGroupReady
        updatePluginStatus()
    }

    fun updatePluginStatus() {
        var packet = UpdatePluginStatusPacket("", DataPacketType.UPDATE_PLUGIN_STATUS)
        if (!logStatus) {
            packet = UpdatePluginStatusPacket(Bundle.getMessage("status.noLog"), DataPacketType.UPDATE_PLUGIN_STATUS)
        }

        if (!apiKeyStatus) {
            packet = UpdatePluginStatusPacket(Bundle.getMessage("status.noApiKey"), DataPacketType.UPDATE_PLUGIN_STATUS)
        }

        if (!studyGroupStatus) {
            packet = UpdatePluginStatusPacket(Bundle.getMessage("status.noStudyGroup"), DataPacketType.UPDATE_PLUGIN_STATUS)
        }

        sendPacketToBrowser(toolWindowBrowser, packet)
    }

    override fun updateProcessingStatus(processing: Boolean) {
        val packet = UpdateProcessingStatusPacket(processing, DataPacketType.UPDATE_PROCESSING_STATUS)
        sendPacketToBrowser(toolWindowBrowser, packet)
    }

    override fun updateBundle(bundle: String) {
        val packet = UpdateBundlePacket(bundle, DataPacketType.UPDATE_BUNDLE)
        sendPacketToBrowser(toolWindowBrowser, packet)
        sendPacketToBrowser(widgetBrowser, packet)
    }

    override fun updateColorScheme(colorScheme: ColorScheme) {
        val packet = UpdateColorSchemePacket(colorScheme, DataPacketType.UPDATE_COLOR_SCHEME)
        sendPacketToBrowser(toolWindowBrowser, packet)
        sendPacketToBrowser(widgetBrowser, packet)
    }

    override fun updateStudyGroup(studyGroup: Int) {
        val packet = UpdateStudyGroupPacket(studyGroup, DataPacketType.UPDATE_STUDY_GROUP)
        sendPacketToBrowser(toolWindowBrowser, packet)
        sendPacketToBrowser(widgetBrowser, packet)
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
