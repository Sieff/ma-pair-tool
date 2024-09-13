package com.github.sieff.mapairtool.model.sourceCode

import kotlinx.serialization.Serializable

@Serializable
data class SourceCodeInfoList(
    val origin: String,
    val code_files: List<SourceCodeFile>,
)
