package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestToolWindowFocusQuery")
data class RequestToolWindowFocusQuery(
    override val queryType: CefQueryType = CefQueryType.REQUEST_TOOL_WINDOW_FOCUS
): CefQuery()
