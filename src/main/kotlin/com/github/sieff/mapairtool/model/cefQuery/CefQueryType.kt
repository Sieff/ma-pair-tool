package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CefQueryType {
    @SerialName("INPUT") INPUT,
    @SerialName("WIDGET_INPUT") WIDGET_INPUT,
    @SerialName("REQUEST_TOOL_WINDOW") REQUEST_TOOL_WINDOW,
    @SerialName("REQUEST_MESSAGES") REQUEST_MESSAGES
}