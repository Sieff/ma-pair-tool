package com.github.sieff.mapairtool.ui.popup

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class PopupComponent {
    val panel: JPanel
    private val button = JButton("Does nothing")

    init {
        panel = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        val label = JBLabel("My Popup :D")

        panel.add(label, BorderLayout.CENTER)
        panel.add(button, BorderLayout.SOUTH)
    }

    val preferredFocusedComponent: JComponent
        get() = button
}