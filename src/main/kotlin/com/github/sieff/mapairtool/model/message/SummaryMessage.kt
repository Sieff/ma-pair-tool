package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SummaryMessage(
    val summary: String,
    @SerialName("sub_problems")
    val subProblems: List<String>,
    val boundaries: List<String>
)