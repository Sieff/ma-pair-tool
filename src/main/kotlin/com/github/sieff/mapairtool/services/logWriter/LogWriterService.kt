package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.BaseMessage
import com.github.sieff.mapairtool.model.message.SummaryMessage
import com.intellij.openapi.editor.Document

interface LogWriterService {
    fun startNewLog()
    fun logMessage(message: BaseMessage)
    fun logSummary(summary: SummaryMessage)
    fun logReset()
    fun logEdit(type: String)
    fun logSessionStart()
    fun logFiles(logDirectoryName: String, documents: Set<Document>)
}