package com.github.sieff.mapairtool.ui.toolWindow

import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.ui.Frontend
import com.github.sieff.mapairtool.util.CefQueryHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import java.awt.BorderLayout


class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.project.service<AppSettingsState>()
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {
        private var browser: JBCefBrowser = JBCefBrowser()
        private val cefBrowserService = toolWindow.project.service<CefBrowserService>()

        private val logger = Logger(this.javaClass)

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()
            browser = JBCefBrowser()
            browser.loadURL("${Frontend.url}/chat")
            val cefClient = browser.jbCefClient.cefClient

            logger.info("Creating Toolwindow")

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
