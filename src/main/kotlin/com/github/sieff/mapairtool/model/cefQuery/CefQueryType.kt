package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CefQueryType {
    @SerialName("INPUT") INPUT,
    @SerialName("WIDGET_INPUT") WIDGET_INPUT,
    @SerialName("QUICK_REACTION_INPUT") QUICK_REACTION_INPUT,
    @SerialName("REQUEST_TOOL_WINDOW_FOCUS") REQUEST_TOOL_WINDOW_FOCUS,
    @SerialName("REQUEST_MESSAGES") REQUEST_MESSAGES,
    @SerialName("RESET_CONVERSATION") RESET_CONVERSATION,
    @SerialName("INPUT_CHANGED_EVENT") INPUT_CHANGED_EVENT,
    @SerialName("REQUEST_COLOR_SCHEME") REQUEST_COLOR_SCHEME,
    @SerialName("REQUEST_STUDY_GROUP") REQUEST_STUDY_GROUP,
}