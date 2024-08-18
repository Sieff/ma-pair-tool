package com.github.sieff.mapairtool.model.dataPacket

import com.github.sieff.mapairtool.model.message.AssistantMessage
import kotlinx.serialization.Serializable

@Serializable
data class UpdateWidgetMessagePacket(
    val message: AssistantMessage,
    override val packetType: DataPacketType
): DataPacket()