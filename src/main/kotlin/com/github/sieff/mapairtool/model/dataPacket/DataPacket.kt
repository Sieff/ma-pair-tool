package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
sealed class DataPacket {
    abstract val packetType: DataPacketType
}