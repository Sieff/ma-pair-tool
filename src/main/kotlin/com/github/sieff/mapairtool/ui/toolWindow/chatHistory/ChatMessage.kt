package com.github.sieff.mapairtool.ui.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.github.sieff.mapairtool.ui.colors.Colors
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JTextArea

class ChatMessage(message: Message, maxWidth: Int): JBPanel<ChatMessage>() {

    init {
        val messageTextArea = JBTextArea()
        border = JBUI.Borders.empty(5)
        layout = FlowLayout(if (message.origin == MessageOrigin.AGENT) FlowLayout.LEFT else FlowLayout.RIGHT)

        messageTextArea.text = message.message
        messageTextArea.isEditable = false
        messageTextArea.wrapStyleWord = true
        messageTextArea.lineWrap = true

        messageTextArea.border = JBUI.Borders.empty(5, 10)
        messageTextArea.background = if (message.origin == MessageOrigin.AGENT) Colors.AGENT.color else Colors.USER.color

        add(messageTextArea)
    }


}