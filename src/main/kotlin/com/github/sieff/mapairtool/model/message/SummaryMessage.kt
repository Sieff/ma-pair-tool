package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.Serializable

@Serializable
data class SummaryMessage(
    val summary: String,
    val facts: List<String>,
    val goals: List<String>,
    val challenges: List<String>,
    val boundaries: List<String>
)