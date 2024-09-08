package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.BaseMessage
import com.github.sieff.mapairtool.model.message.SummaryMessage

interface LogWriterService {
    fun startNewLog()
    fun logMessage(message: BaseMessage)
    fun logSummary(summary: SummaryMessage)
}