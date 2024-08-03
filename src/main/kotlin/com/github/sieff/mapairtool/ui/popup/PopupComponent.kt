package com.github.sieff.mapairtool.ui.popup

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.github.sieff.mapairtool.ui.swing.DragHandleComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.json.Json
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel


class PopupComponent(project: Project, preferredSize: Dimension): Disposable, JBPanel<JBPanel<*>>() {
    private var browser: JBCefBrowser = JBCefBrowser()
    private val inputHandlerService = project.service<InputHandlerService>()
    private val chatMessageService = project.service<ChatMessageService>()

    init {
        layout = BorderLayout()

        browser = JBCefBrowser()
        browser.loadURL("localhost:3000/widget")
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

        Disposer.register(this, browser)

        browser.component.preferredSize = preferredSize
        size = preferredSize

        val dragHandle = DragHandleComponent()

        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.anchor = GridBagConstraints.CENTER
        panel.add(dragHandle, gbc)

        add(browser.component, BorderLayout.CENTER)
        add(panel, BorderLayout.WEST)
    }

    val preferredFocusedComponent: JComponent
        get() = browser.component

    override fun dispose() {
        chatMessageService.removeBrowser(browser)
    }
}