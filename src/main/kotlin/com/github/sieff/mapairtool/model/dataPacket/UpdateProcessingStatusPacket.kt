package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProcessingStatusPacket(
    val processing: Boolean,
    override val packetType: DataPacketType
): DataPacket()