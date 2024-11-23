package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.BaseState

class AppState: BaseState() {
    var apiKey by string()
    var studyGroup by property(0)
}