package com.github.sieff.mapairtool.model.sourceCode

import kotlinx.serialization.Serializable

@Serializable
data class SourceCodeFile(
    val name: String,
    val code: List<SourceCodeLine>,
)
