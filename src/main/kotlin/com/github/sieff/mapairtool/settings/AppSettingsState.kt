package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.components.State


@Service(Service.Level.PROJECT)
@State(name = "cpsAgent.AppSettingsState", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AppSettingsState : SimplePersistentStateComponent<AppState>(AppState()) {
    override fun loadState(state: AppState) {
        super.loadState(state)
        AppSettingsPublisher.publish(state)
    }

    fun updateState(appState: AppState) {
        state.apiKey = appState.apiKey
        state.studyGroup = appState.studyGroup
        AppSettingsPublisher.publish(state)
    }
}