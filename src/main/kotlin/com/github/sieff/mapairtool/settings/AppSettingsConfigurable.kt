package com.github.sieff.mapairtool.settings

import com.github.sieff.mapairtool.ui.settings.AppSettingsComponent
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import java.util.*
import javax.swing.JComponent


class AppSettingsConfigurable : Configurable {

    private lateinit var settingsComponent: AppSettingsComponent

    private val settings: AppSettingsState
        get() = AppSettingsState.getInstance()

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "Pair Tool Settings"
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
        val state: AppSettingsState.State =
            Objects.requireNonNull(settings.state)
        return !settingsComponent.apiKeyText.equals(state.apiKey)
    }


    override fun apply() {
        val state: AppSettingsState.State =
            Objects.requireNonNull(settings.state)
        state.apiKey = settingsComponent.apiKeyText
    }

    override fun reset() {
        val state: AppSettingsState.State =
            Objects.requireNonNull(settings.state)
        settingsComponent.apiKeyText = state.apiKey
    }

}