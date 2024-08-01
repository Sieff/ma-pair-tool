package com.github.sieff.mapairtool.ui.popup

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class PopupComponent(project: Project): Observer<Message>, Disposable {
    val panel: JPanel
    private val button = JButton("Does nothing")
    private val label = JBLabel("")
    private val chatMessageService = project.service<ChatMessageService>()

    init {
        chatMessageService.subscribe(this)

        panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()

        panel.add(label, BorderLayout.CENTER)
        panel.add(button, BorderLayout.EAST)
    }

    val preferredFocusedComponent: JComponent
        get() = button

    override fun notify(message: Message) {
        if (message.origin == MessageOrigin.AGENT) {
            label.text = message.message
            label.revalidate()
            label.repaint()
        }
    }

    override fun dispose() {
        chatMessageService.unsubscribe(this)
    }
}