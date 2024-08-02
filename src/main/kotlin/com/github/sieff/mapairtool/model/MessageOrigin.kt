package com.github.sieff.mapairtool.model

import kotlinx.serialization.*

@Serializable
enum class MessageOrigin {
    @SerialName("0") AGENT,
    @SerialName("1") USER
}