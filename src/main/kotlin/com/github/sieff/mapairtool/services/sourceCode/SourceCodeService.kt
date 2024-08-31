package com.github.sieff.mapairtool.services.sourceCode

interface SourceCodeService {
    fun getActiveFile(): String?
    fun getActiveFileReferences(): List<String>
    fun getActiveFiles(): List<String>
    fun getActiveFileName(): String?
    fun getActiveFileReferenceNames(): List<String>
    fun getActiveFileNames(): List<String>
}