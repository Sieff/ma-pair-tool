package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLogStatusPacket(
    val success: Boolean,
    override val packetType: DataPacketType
): DataPacket()