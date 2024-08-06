package com.github.sieff.mapairtool.util

import com.github.sieff.mapairtool.model.cefQuery.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

class CefQueryHandler(project: Project): CefMessageRouterHandlerAdapter() {
    private val inputHandlerService = project.service<InputHandlerService>()
    private val cefBrowserService = project.service<CefBrowserService>()

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
            CefQueryType.REQUEST_TOOL_WINDOW -> onRequestToolWindow()
            CefQueryType.REQUEST_MESSAGES -> onRequestMessages()
        }

        return true
    }

    private fun onInput(request: String) {
        val query = CefQuerySerializer.json.decodeFromString<InputQuery>(request)
        inputHandlerService.handleInput(query.message)
    }

    private fun onWidgetInput(request: String) {
        val query = CefQuerySerializer.json.decodeFromString<WidgetInputQuery>(request)
        inputHandlerService.handleWidgetInput(query.message)
    }

    private fun onRequestToolWindow() {
    }

    private fun onRequestMessages() {
        cefBrowserService.sendCurrentState()
    }
}