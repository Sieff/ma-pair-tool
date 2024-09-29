package com.github.sieff.mapairtool.settings

import com.github.sieff.mapairtool.util.observerPattern.publisher.APublisher
import com.github.sieff.mapairtool.util.singletonPattern.Singleton
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "com.github.sieff.mapairtool.settings.ApplicationSettingsState", storages = [Storage("MaPairToolSettings.xml")])
@Service
class AppSettingsState : PersistentStateComponent<AppSettingsState.State> {

    data class State(var apiKey: String = ""): APublisher<State>() {
        var studyGroup: Int = 0
            set(value) {
                field = value
                publish(this)
            }
    }

    private var state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    companion object : Singleton<AppSettingsState>(::AppSettingsState)
}
