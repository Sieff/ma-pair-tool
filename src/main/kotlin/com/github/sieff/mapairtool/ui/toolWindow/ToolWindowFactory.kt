package com.github.sieff.mapairtool.ui.toolWindow

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
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

    class MyToolWindow(toolWindow: ToolWindow) {
        private var browser: JBCefBrowser = JBCefBrowser()
        private val inputHandlerService = toolWindow.project.service<InputHandlerService>()
        private val chatMessageService = toolWindow.project.service<ChatMessageService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()
            browser = JBCefBrowser()
            browser.loadURL("localhost:3000/chat")
            val cefClient = browser.jbCefClient.cefClient

            chatMessageService.addBrowser(browser)

            // Set up message router
            val routerConfig = CefMessageRouterConfig()
            val msgRouter = CefMessageRouter.create(routerConfig)
            msgRouter.addHandler(object: CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: CefQueryCallback?
                ): Boolean {
                    println(request)
                    if (request != null) {
                        inputHandlerService.handleInput(Json.decodeFromString<Message>(request).message)
                    }

                    return true;
                }
            }, true)

            cefClient.addMessageRouter(msgRouter)

            add(browser.component, BorderLayout.CENTER)
        }
    }
}
