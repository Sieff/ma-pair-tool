package com.github.sieff.mapairtool.ui.toolWindow

import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.github.sieff.mapairtool.util.CefQueryHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.json.Json
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.awt.BorderLayout


class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {
        private var browser: JBCefBrowser = JBCefBrowser()
        private val cefBrowserService = toolWindow.project.service<CefBrowserService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()
            browser = JBCefBrowser()
            browser.loadURL("localhost:3000/chat")
            val cefClient = browser.jbCefClient.cefClient

            cefBrowserService.toolWindowBrowser = browser

            // Set up message router
            val routerConfig = CefMessageRouterConfig()
            val msgRouter = CefMessageRouter.create(routerConfig)
            msgRouter.addHandler(CefQueryHandler(toolWindow.project), true)
            cefClient.addMessageRouter(msgRouter)

            add(browser.component, BorderLayout.CENTER)
        }
    }
}
