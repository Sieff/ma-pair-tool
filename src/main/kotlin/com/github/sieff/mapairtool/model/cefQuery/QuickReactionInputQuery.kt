package com.github.sieff.mapairtool.model.cefQuery

import com.github.sieff.mapairtool.model.message.QuickReactionMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("QuickReactionInputQuery")
data class QuickReactionInputQuery(
    val message: QuickReactionMessage,
    override val queryType: CefQueryType = CefQueryType.QUICK_REACTION_INPUT
): CefQuery()
