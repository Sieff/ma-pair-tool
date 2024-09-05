package com.github.sieff.mapairtool.ui.popup

import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.CefQueryHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel


class PopupComponent(project: Project, preferredSize: Dimension): Disposable, JBPanel<JBPanel<*>>() {
    private var browser: JBCefBrowser = JBCefBrowser()
    private val cefBrowserService = project.service<CefBrowserService>()

    private val logger = Logger(this.javaClass)

    init {
        layout = BorderLayout()

        browser = JBCefBrowser()
        browser.loadURL("localhost:3000/widget")
        val cefClient = browser.jbCefClient.cefClient

        logger.info("Creating Widget Browser")
        cefBrowserService.widgetBrowser = browser

        // Set up message router
        val routerConfig = CefMessageRouterConfig()
        val msgRouter = CefMessageRouter.create(routerConfig)
        msgRouter.addHandler(CefQueryHandler(project), true)
        cefClient.addMessageRouter(msgRouter)


        Disposer.register(this, browser)

        browser.component.preferredSize = preferredSize
        size = preferredSize

        val dragHandlePanel1 = getDragHandle()
        val dragHandlePanel2 = getDragHandle()

        add(browser.component, BorderLayout.CENTER)
        add(dragHandlePanel1, BorderLayout.WEST)
        add(dragHandlePanel2, BorderLayout.EAST)
    }

    private fun getDragHandle(): JPanel {
        val dragHandle = DragHandleComponent()

        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.anchor = GridBagConstraints.CENTER
        panel.add(dragHandle, gbc)

        return panel
    }

    val preferredFocusedComponent: JComponent
        get() = browser.component

    override fun dispose() {
        cefBrowserService.widgetBrowser = null
    }
}