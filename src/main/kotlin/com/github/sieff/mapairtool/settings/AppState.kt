package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.BaseState

class AppState: BaseState() {
    var apiKey: String = ""
    var studyGroup by property(0)
}