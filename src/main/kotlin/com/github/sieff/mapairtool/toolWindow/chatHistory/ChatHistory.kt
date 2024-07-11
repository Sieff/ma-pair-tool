package com.github.sieff.mapairtool.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.services.inputHandler.ChatMessageService
import com.github.sieff.mapairtool.util.observerPattern.observer.IObserver
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import javax.swing.BoxLayout
import com.intellij.openapi.project.Project

class ChatHistory(project: Project): JBPanel<ChatHistory>(), IObserver<MutableList<Message>> {
    private var messages: MutableList<Message> = mutableListOf()
    private val messagesPanel = JBPanel<JBPanel<*>>()
    private val chatMessageService = project.service<ChatMessageService>()

    init {
        chatMessageService.subscribe(this)
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        messagesPanel.layout = BoxLayout(messagesPanel, BoxLayout.Y_AXIS)
        val scrollPane = JBScrollPane(messagesPanel)
        add(scrollPane)
    }

    override fun notify(message: MutableList<Message>) {
        messages = message

        messagesPanel.removeAll()
        for (message in messages) {
            val messageLabel = JBLabel(message.message)
            messagesPanel.add(messageLabel)
        }
        messagesPanel.revalidate()
        messagesPanel.repaint()
    }

}