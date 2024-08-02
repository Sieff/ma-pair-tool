package com.github.sieff.mapairtool.ui.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
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

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()
            browser = JBCefBrowser()
            val cefClient = browser.jbCefClient.cefClient


            // Set up message router
            val routerConfig = CefMessageRouterConfig()
            val router = CefMessageRouter.create(routerConfig)
            cefClient.addMessageRouter(router)

            val msgRouter = CefMessageRouter.create()
            msgRouter.addHandler(object: CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: CefQueryCallback?
                ): Boolean {
                    println(request);

                    return true;
                }
            }, true)

            cefClient.addMessageRouter(msgRouter)


            // Set up a load handler to handle browser events
            cefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                    println("Page Loaded")
                }
            })

            browser.loadURL("localhost:3000")

            add(browser.component, BorderLayout.CENTER)
        }
    }
}
