package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.Serializable

@Serializable
sealed class CefQuery {
    abstract val queryType: CefQueryType
}