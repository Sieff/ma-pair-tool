package com.github.sieff.mapairtool.model.dataPacket

import com.github.sieff.mapairtool.model.message.AssistantMessage
import com.github.sieff.mapairtool.model.message.BaseMessage
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMessagesPacket(
    val messages: List<BaseMessage>,
    val widgetMessage: AssistantMessage?,
    override val packetType: DataPacketType
): DataPacket()
