package com.github.sieff.mapairtool.model.sourceCode

import kotlinx.serialization.Serializable

@Serializable
data class SourceCodeInfo(
    val origin: String,
    val code_file: SourceCodeFile,
    val cursor_line: Int,
)
