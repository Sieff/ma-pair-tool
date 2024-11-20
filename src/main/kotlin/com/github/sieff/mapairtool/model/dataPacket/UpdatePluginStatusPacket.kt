package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePluginStatusPacket(
    val status: String,
    override val packetType: DataPacketType
): DataPacket()