package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.*

@Serializable
enum class MessageOrigin {
    @SerialName("AGENT") AGENT,
    @SerialName("USER") USER
}