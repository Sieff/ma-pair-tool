package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.components.State


@Service(Service.Level.PROJECT)
@State(name = "com.github.sieff.mapairtool.settings.AppSettingsState", storages = [Storage("cpsAgentSettings.xml")])
class AppSettingsState : SimplePersistentStateComponent<AppState>(AppState())