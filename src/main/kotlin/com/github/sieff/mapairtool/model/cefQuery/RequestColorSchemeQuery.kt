package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestColorSchemeQuery")
data class RequestColorSchemeQuery(
    override val queryType: CefQueryType = CefQueryType.REQUEST_COLOR_SCHEME
): CefQuery()
