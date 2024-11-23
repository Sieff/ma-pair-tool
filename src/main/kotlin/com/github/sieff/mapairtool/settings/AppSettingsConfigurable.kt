package com.github.sieff.mapairtool.settings

import com.github.sieff.mapairtool.ui.settings.AppSettingsComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class AppSettingsConfigurable(val project: Project) : Configurable {

    private lateinit var settingsComponent: AppSettingsComponent

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "CPS Agent Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent.preferredFocusedComponent
    }

    @Nullable
    override fun createComponent(): JComponent {
        settingsComponent = AppSettingsComponent()
        return settingsComponent.panel
    }

    override fun isModified(): Boolean {
        val state: AppState = project.service<AppSettingsState>().state
        return settingsComponent.apiKeyText != state.apiKey || settingsComponent.studyGroup != state.studyGroup
    }


    override fun apply() {
        val newState = AppState()
        newState.apiKey = settingsComponent.apiKeyText
        newState.studyGroup = settingsComponent.studyGroup

        project.service<AppSettingsState>().updateState(newState)
    }

    override fun reset() {
        settingsComponent.apiKeyText = project.service<AppSettingsState>().state.apiKey ?: ""
        settingsComponent.studyGroup = project.service<AppSettingsState>().state.studyGroup
    }
}