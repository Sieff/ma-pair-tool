package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.*

@Serializable
enum class DataPacketType {
    @SerialName("UPDATE_MESSAGES") UPDATE_MESSAGES,
    @SerialName("UPDATE_WIDGET_MESSAGE") UPDATE_WIDGET_MESSAGE,
    @SerialName("REQUEST_TEXT_INPUT_FOCUS") REQUEST_TEXT_INPUT_FOCUS
}