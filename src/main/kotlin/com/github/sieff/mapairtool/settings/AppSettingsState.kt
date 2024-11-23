package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.components.State


@Service(Service.Level.PROJECT)
@State(name = "cpsAgent.AppSettingsState", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AppSettingsState : SimplePersistentStateComponent<AppState>(AppState())