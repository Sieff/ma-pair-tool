package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object MessageSerializer {
    private val module = SerializersModule {
        polymorphic(BaseMessage::class) {
            subclass(AssistantMessage::class, AssistantMessage.serializer())
            subclass(Message::class, Message.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
        ignoreUnknownKeys = true
    }
}