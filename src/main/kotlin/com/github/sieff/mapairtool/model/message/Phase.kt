package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName

enum class Phase {
    @SerialName("CLARIFY") CLARIFY,
    @SerialName("IDEA") IDEA,
    @SerialName("DEVELOP") DEVELOP,
    @SerialName("IMPLEMENT") IMPLEMENT,
}