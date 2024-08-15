package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class RequestTextInputFocusPacket(
    override val packetType: DataPacketType
): DataPacket()