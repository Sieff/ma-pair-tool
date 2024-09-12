package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object CompletionRequestSerializer {
    private val module = SerializersModule {
        polymorphic(Properties::class) {
            subclass(AssistantMessageProperties::class, AssistantMessageProperties.serializer())
            subclass(SummaryProperties::class, SummaryProperties.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
        ignoreUnknownKeys = true
    }
}
