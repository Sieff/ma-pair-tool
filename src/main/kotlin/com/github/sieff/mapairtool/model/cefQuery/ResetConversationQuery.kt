package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ResetConversationQuery")
data class ResetConversationQuery(
    override val queryType: CefQueryType = CefQueryType.RESET_CONVERSATION
): CefQuery()
