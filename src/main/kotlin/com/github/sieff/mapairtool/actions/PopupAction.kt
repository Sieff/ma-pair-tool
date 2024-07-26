package com.github.sieff.mapairtool.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JOptionPane


class PopupAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val panel: JBPanel<*> = JBPanel<JBPanel<*>>()
        panel.layout = BorderLayout()
        val label = JBLabel("My Popup :D")
        val button = JButton("Does nothing")

        panel.add(label, BorderLayout.CENTER)
        panel.add(button, BorderLayout.SOUTH)

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, button)
            .setResizable(true)
            .setMovable(true)
            .setRequestFocus(true)
            .createPopup()

        popup.showCenteredInCurrentWindow(project)
    }

    companion object {
        fun  getId(): String{
            return "com.github.sieff.mapairtool.actions.PopupAction"
        }
    }
}
