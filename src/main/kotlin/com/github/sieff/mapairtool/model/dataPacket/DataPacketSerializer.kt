package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object DataPacketSerializer {
    private val module = SerializersModule {
        polymorphic(DataPacket::class) {
            subclass(UpdateMessagesPacket::class, UpdateMessagesPacket.serializer())
            subclass(UpdateTemporaryMessagePacket::class, UpdateTemporaryMessagePacket.serializer())
        }
    }

    val json: Json = Json {
        serializersModule = module
    }
}