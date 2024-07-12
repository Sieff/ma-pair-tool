package com.github.sieff.mapairtool.ui.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.services.inputHandler.ChatMessageService
import com.github.sieff.mapairtool.util.observerPattern.observer.IObserver
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import javax.swing.BoxLayout
import com.intellij.openapi.project.Project
import javax.swing.ScrollPaneConstants

class ChatHistory(project: Project): JBPanel<ChatHistory>(), IObserver<Message> {
    private var messages: MutableList<Message> = mutableListOf()
    private val messagesPanel = JBPanel<JBPanel<*>>()
    private val chatMessageService = project.service<ChatMessageService>()

    init {
        chatMessageService.subscribe(this)
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        messagesPanel.layout = BoxLayout(messagesPanel, BoxLayout.Y_AXIS)
        val scrollPane = JBScrollPane(messagesPanel)
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        add(scrollPane)
    }

    override fun notify(message: Message) {
        messages.add(message)

        val chatMessage = ChatMessage(message, width)
        messagesPanel.add(chatMessage)
        messagesPanel.revalidate()
        messagesPanel.repaint()
    }
}