package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object CefQuerySerializer {
    private val module = SerializersModule {
        polymorphic(CefQuery::class) {
            subclass(InputQuery::class, InputQuery.serializer())
            subclass(WidgetInputQuery::class, WidgetInputQuery.serializer())
            subclass(RequestMessagesQuery::class, RequestMessagesQuery.serializer())
            subclass(RequestToolWindowFocusQuery::class, RequestToolWindowFocusQuery.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }
}