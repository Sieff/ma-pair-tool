package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class SummaryProperties(
    val summary: Property,
    val key_information: ArrayProperty,
    val boundaries: ArrayProperty,
): Properties()