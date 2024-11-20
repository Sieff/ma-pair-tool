package com.github.sieff.mapairtool.settings

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag

@Tag("AppState")
class AppState: BaseState() {
    @Property var apiKey: String = ""
    @Property var studyGroup: Int = 0
}