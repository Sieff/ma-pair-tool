package com.github.sieff.mapairtool.ui.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyField = JBPasswordField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("OpenAI API Key:"), apiKeyField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = apiKeyField

    @get:NotNull
    var apiKeyText: String
        get() = apiKeyField.password.toString()
        set(newText) {
            apiKeyField.text = newText
        }
}