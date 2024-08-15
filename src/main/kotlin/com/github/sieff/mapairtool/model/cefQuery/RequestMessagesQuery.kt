package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestMessagesQuery")
data class RequestMessagesQuery(
    override val queryType: CefQueryType = CefQueryType.REQUEST_MESSAGES
): CefQuery()
