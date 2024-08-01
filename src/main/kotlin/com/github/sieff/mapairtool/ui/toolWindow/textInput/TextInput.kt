package com.github.sieff.mapairtool.ui.toolWindow.textInput

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TextInput(project: Project): JBPanel<TextInput>() {
    private val inputService = project.service<InputHandlerService>()

    init {
        layout = BorderLayout()

        val textField = JBTextField("").apply {
            addActionListener {
                handleSubmit(this)
            }
        }
        textField.emptyText.text = Bundle.message("chat.input.inputLabel")

        val submitButton = JButton(Bundle.message("chat.input.submitMessage")).apply {
            addActionListener {
                handleSubmit(textField)
            }
        }
        submitButton.isEnabled = false

        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                handleTextfieldUpdate(submitButton, textField)
            }

            override fun removeUpdate(e: DocumentEvent) {
                handleTextfieldUpdate(submitButton, textField)
            }

            override fun changedUpdate(e: DocumentEvent) {
                handleTextfieldUpdate(submitButton, textField)
            }
        })

        add(textField, BorderLayout.CENTER)
        add(submitButton, BorderLayout.EAST)
    }

    fun handleTextfieldUpdate(submitButton: JButton, textField: JBTextField) {
        submitButton.isEnabled = !textField.text.equals("")
        submitButton.revalidate()
        submitButton.repaint()
    }

    fun handleSubmit(textField: JBTextField) {
        inputService.handleInput(textField.text)
        textField.text = ""
        textField.revalidate()
        textField.repaint()
    }

}