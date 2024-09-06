package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RegenerateLastMessageQuery")
data class RegenerateLastMessageQuery(
    override val queryType: CefQueryType = CefQueryType.RESET_CONVERSATION
): CefQuery()
