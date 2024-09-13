package com.github.sieff.mapairtool.services.sourceCode

import com.github.sieff.mapairtool.model.sourceCode.SourceCodeFile

interface SourceCodeService {
    fun getActiveFile(): SourceCodeFile?
    fun getActiveFileReferences(): List<SourceCodeFile>
    fun getOpenFiles(): List<SourceCodeFile>
}