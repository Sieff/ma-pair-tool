package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class ColorScheme {
    @SerialName("LIGHT") LIGHT,
    @SerialName("DARK") DARK,
}