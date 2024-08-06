package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestToolWindowQuery")
data class RequestToolWindowQuery(
    override val queryType: CefQueryType
): CefQuery()
