package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBundlePacket(
    val locale: String,
    override val packetType: DataPacketType
): DataPacket()