package com.github.sieff.mapairtool.model.dataPacket

import com.github.sieff.mapairtool.model.message.AssistantMessage
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTemporaryMessagePacket(
    val message: AssistantMessage,
    override val packetType: DataPacketType = DataPacketType.UPDATE_MESSAGES
): DataPacket()