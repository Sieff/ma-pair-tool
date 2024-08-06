package com.github.sieff.mapairtool.model.cefQuery

import com.github.sieff.mapairtool.model.message.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InputQuery")
data class InputQuery(
    override val queryType: CefQueryType,
    val message: Message
): CefQuery()
