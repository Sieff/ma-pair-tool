package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SummaryMessage(
    val summary: String,
    @SerialName("key_information")
    val keyInformation: List<String>,
    val boundaries: List<String>
)