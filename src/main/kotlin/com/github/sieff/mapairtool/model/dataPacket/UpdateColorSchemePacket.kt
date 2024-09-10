package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable


@Serializable
data class UpdateColorSchemePacket(
    val scheme: ColorScheme,
    override val packetType: DataPacketType
): DataPacket()