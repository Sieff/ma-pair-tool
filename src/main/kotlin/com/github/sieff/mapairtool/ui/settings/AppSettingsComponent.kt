package com.github.sieff.mapairtool.ui.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyField = JBTextField()
    private val studyGroupField = ComboBox<Int>(arrayOf(0, 1, 2))

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("OpenAI API Key:"), apiKeyField, 1, false)
            .addLabeledComponent(JBLabel("Study group:"), studyGroupField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = apiKeyField

    @get:NotNull
    var apiKeyText: String
        get() = apiKeyField.text
        set(newText) {
            apiKeyField.text = newText
        }

    @get:NotNull
    var studyGroup: Int
        get() = studyGroupField.selectedItem as Int
        set(newItem) {
            studyGroupField.selectedItem = newItem
        }
}