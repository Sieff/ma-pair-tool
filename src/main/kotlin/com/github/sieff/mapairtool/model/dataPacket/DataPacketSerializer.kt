package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object DataPacketSerializer {
    private val module = SerializersModule {
        polymorphic(DataPacket::class) {
            subclass(UpdateMessagesPacket::class, UpdateMessagesPacket.serializer())
            subclass(UpdateWidgetMessagePacket::class, UpdateWidgetMessagePacket.serializer())
            subclass(RequestTextInputFocusPacket::class, RequestTextInputFocusPacket.serializer())
            subclass(UpdatePluginStatusPacket::class, UpdatePluginStatusPacket.serializer())
            subclass(UpdateBundlePacket::class, UpdateBundlePacket.serializer())
            subclass(UpdateColorSchemePacket::class, UpdateColorSchemePacket.serializer())
            subclass(UpdateProcessingStatusPacket::class, UpdateProcessingStatusPacket.serializer())
            subclass(UpdateStudyGroupPacket::class, UpdateStudyGroupPacket.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
    }
}