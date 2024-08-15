package com.github.sieff.mapairtool.model.cefQuery

import com.github.sieff.mapairtool.model.message.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InputQuery")
data class InputQuery(
    val message: Message,
    override val queryType: CefQueryType = CefQueryType.INPUT
): CefQuery()
