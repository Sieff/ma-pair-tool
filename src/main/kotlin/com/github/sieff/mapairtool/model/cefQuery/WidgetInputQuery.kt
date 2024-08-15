package com.github.sieff.mapairtool.model.cefQuery

import com.github.sieff.mapairtool.model.message.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("WidgetInputQuery")
data class WidgetInputQuery(
    val message: Message,
    override val queryType: CefQueryType = CefQueryType.WIDGET_INPUT
): CefQuery()
