package com.github.sieff.mapairtool.ui.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.github.sieff.mapairtool.ui.colors.Colors
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import net.miginfocom.swing.MigLayout
import java.awt.*
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.JTextPane

class ChatMessage(message: Message): JBPanel<ChatMessage>() {

    init {
        val messageTextArea = JTextPane()
        val rightToLeft = if (message.origin == MessageOrigin.USER) ",rtl" else ""
        layout = MigLayout("inset 5$rightToLeft", "[]", "[top]")

        if (message.origin == MessageOrigin.USER) {
            border = JBUI.Borders.empty(0, 100, 0,0)
        } else {
            border = JBUI.Borders.empty(0, 0, 0,100)
        }

        messageTextArea.text = message.message
        messageTextArea.isEditable = false


        messageTextArea.border = JBUI.Borders.empty(5, 10)
        messageTextArea.background = if (message.origin == MessageOrigin.AGENT) Colors.AGENT.color else Colors.USER.color

        add(messageTextArea)
    }

}