package com.github.sieff.mapairtool.util

import com.github.sieff.mapairtool.model.cefQuery.*
import com.github.sieff.mapairtool.model.dataPacket.ColorScheme
import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.UserTelemetryInformation
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

class CefQueryHandler(project: Project): CefMessageRouterHandlerAdapter() {
    private val inputHandlerService = project.service<InputHandlerService>()
    private val cefBrowserService = project.service<CefBrowserService>()
    private val appSettingsState = project.service<AppSettingsState>()
    private val chatMessageService = project.service<ChatMessageService>()
    private val logWriterService = project.service<LogWriterService>()

    override fun onQuery(
        browser: CefBrowser?,
        frame: CefFrame?,
        queryId: Long,
        request: String?,
        persistent: Boolean,
        callback: CefQueryCallback?
    ): Boolean {
        if (request == null) {
            return false
        }

        val query = CefQuerySerializer.json.decodeFromString<CefQuery>(request)
        when(query.queryType) {
            CefQueryType.INPUT -> onInput(request)
            CefQueryType.WIDGET_INPUT -> onWidgetInput(request)
            CefQueryType.QUICK_REACTION_INPUT -> onQuickReactionInput(request)
            CefQueryType.REQUEST_TOOL_WINDOW_FOCUS -> onRequestToolWindowFocus()
            CefQueryType.REQUEST_MESSAGES -> onRequestMessages()
            CefQueryType.INPUT_CHANGED_EVENT -> onInputChangedEvent()
            CefQueryType.RESET_CONVERSATION -> onResetConversation()
            CefQueryType.REQUEST_COLOR_SCHEME -> onRequestColorScheme()
            CefQueryType.REQUEST_STUDY_GROUP -> onRequestStudyGroup()
        }

        return true
    }

    private fun onInput(request: String) {
        val query = CefQuerySerializer.json.decodeFromString<InputQuery>(request)
        inputHandlerService.handleInput(query.message)
    }

    private fun onWidgetInput(request: String) {
        val query = CefQuerySerializer.json.decodeFromString<WidgetInputQuery>(request)
        inputHandlerService.handleInput(query.message)
    }

    private fun onQuickReactionInput(request: String) {
        val query = CefQuerySerializer.json.decodeFromString<QuickReactionInputQuery>(request)
        inputHandlerService.handleInput(query.message)
    }

    private fun onRequestToolWindowFocus() {
        cefBrowserService.requestToolWindowFocus()
    }

    private fun onRequestMessages() {
        cefBrowserService.sendCurrentState()
    }

    private fun onInputChangedEvent() {
        UserTelemetryInformation.lastChatInputEdit = getCurrentTime()
    }

    private fun onResetConversation() {
        logWriterService.logReset()
        chatMessageService.resetMessages()
        ConversationInformation.reset()
        UserTelemetryInformation.reset()
    }

    private fun onRequestColorScheme() {
        if (EditorColorsManager.getInstance().isDarkEditor) {
            cefBrowserService.updateColorScheme(ColorScheme.DARK)
        } else {
            cefBrowserService.updateColorScheme(ColorScheme.LIGHT)
        }
    }

    private fun onRequestStudyGroup() {
        cefBrowserService.updateStudyGroup(appSettingsState.state.studyGroup)
    }
}