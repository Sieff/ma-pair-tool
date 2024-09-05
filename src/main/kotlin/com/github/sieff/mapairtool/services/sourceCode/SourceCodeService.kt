package com.github.sieff.mapairtool.services.sourceCode

interface SourceCodeService {
    fun getActiveFile(): String?
    fun getActiveFileReferences(): List<String>
    fun getOpenFiles(): List<String>
}