package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InputChangedEventQuery")
data class InputChangedEventQuery(
    override val queryType: CefQueryType = CefQueryType.INPUT_CHANGED_EVENT
): CefQuery()
