package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object CefQuerySerializer {
    private val module = SerializersModule {
        polymorphic(CefQuery::class) {
            subclass(InputQuery::class, InputQuery.serializer())
            subclass(WidgetInputQuery::class, WidgetInputQuery.serializer())
            subclass(QuickReactionInputQuery::class, QuickReactionInputQuery.serializer())
            subclass(RequestMessagesQuery::class, RequestMessagesQuery.serializer())
            subclass(RequestToolWindowFocusQuery::class, RequestToolWindowFocusQuery.serializer())
            subclass(InputChangedEventQuery::class, InputChangedEventQuery.serializer())
            subclass(ResetConversationQuery::class, ResetConversationQuery.serializer())
            subclass(RequestColorSchemeQuery::class, RequestColorSchemeQuery.serializer())
            subclass(RequestStudyGroupQuery::class, RequestStudyGroupQuery.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }
}