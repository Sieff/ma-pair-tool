package com.github.sieff.mapairtool.ui.toolWindow.textInput

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JButton

class TextInput(project: Project): JBPanel<TextInput>() {
    private val inputService = project.service<InputHandlerService>()

    init {
        layout = BorderLayout()
        val textField = JBTextField(Bundle.message("inputLabel"))
        val submitButton = JButton(Bundle.message("submitMessage")).apply {
            addActionListener {
                inputService.handleInput(textField.getText())
            }
        }

        add(textField, BorderLayout.CENTER)
        add(submitButton, BorderLayout.EAST)
    }

}