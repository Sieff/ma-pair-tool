package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.*

@Serializable
enum class Emotion {
    @SerialName("NEUTRAL") NEUTRAL,
    @SerialName("HAPPY") HAPPY,
    @SerialName("SAD") SAD,
    @SerialName("THINKING") THINKING
}