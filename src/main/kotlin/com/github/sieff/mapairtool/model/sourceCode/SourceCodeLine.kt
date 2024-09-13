package com.github.sieff.mapairtool.model.sourceCode

import kotlinx.serialization.Serializable

@Serializable
data class SourceCodeLine(
    val line_number: Int,
    val code_line: String,
)
