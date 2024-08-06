package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.*

@Serializable
enum class DataPacketType {
    @SerialName("UPDATE_MESSAGES") UPDATE_MESSAGES,
    @SerialName("UPDATE_TEMPORARY_MESSAGE") UPDATE_TEMPORARY_MESSAGE
}