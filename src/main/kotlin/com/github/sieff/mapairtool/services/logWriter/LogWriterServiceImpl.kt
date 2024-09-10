package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths

class LogWriterServiceImpl(val project: Project): LogWriterService {
    private val logger = Logger(this.javaClass)
    private val LOG_DIR = ".conversationLog"

    private var currentLog: File? = null

    override fun startNewLog() {
        currentLog = createNewLog()

        val success =  currentLog != null
        project.service<CefBrowserService>().updateLogStatus(success)
    }

    private fun createNewLog(): File? {
        try {
            val logDirPath = createLogDir()

            val logFile = createLogFile(logDirPath)

            return logFile
        } catch (e: Exception) {
            e.message?.let { logger.fatal(it) }

            return null
        }
    }

    override fun logMessage(message: BaseMessage) {
        if (!logIsReady()) {
            startNewLog()
        }

        if (logIsReady()) {
            FileOutputStream(currentLog!!, true).bufferedWriter().use { out ->
                out.write(createLogMessage(message))
            }
        }
    }

    override fun logSummary(summary: SummaryMessage) {
        if (!logIsReady()) {
            startNewLog()
        }

        if (logIsReady()) {
            FileOutputStream(currentLog!!, true).bufferedWriter().use { out ->
                out.write("${Json.encodeToString(summary)}\n")
            }
        }
    }

    override fun logReset() {
        if (logIsReady()) {
            FileOutputStream(currentLog!!, true).bufferedWriter().use { out ->
                out.write("\"Conversation reset\"\n")
            }
        }
    }

    private fun logIsReady(): Boolean {
        return currentLog != null && currentLog!!.exists()
    }

    private fun createLogDir(): Path {
        val logDirPath = project.basePath?.let { Paths.get(it, LOG_DIR) }

        if (logDirPath == null) {
            throw Exception("Cannot get path to log directory")
        }

        val logDir = File(logDirPath.toString())
        if (!logDir.exists()) {
            logDir.mkdirs()
            logger.info("Directory created at: ${logDir.absolutePath}")
        }

        if (!logDir.exists() || !logDir.isDirectory) {
            throw Exception("Cannot create or use log directory")
        }

        return logDirPath
    }

    private fun createLogFile(logDirPath: Path): File {
        val timestamp = System.currentTimeMillis()

        val logFilePath = logDirPath.resolve("$timestamp.log").toString()
        val logFile = File(logFilePath)

        if (!logFile.exists()) {
            logFile.createNewFile()
            logger.info("Log file created at: ${logFile.absolutePath}")
        }

        if (!logFile.exists()) {
            throw Exception("Could not create log file")
        }

        return logFile
    }

    private fun createLogMessage(message: BaseMessage): String {
        var logEntry = ""

        if (message.origin === MessageOrigin.AGENT) {
            val m = message as AssistantMessage
            logEntry += MessageSerializer.json.encodeToString<AssistantMessage>(m)
        }
        if (message.origin === MessageOrigin.USER) {
            val m = message as Message
            logEntry += MessageSerializer.json.encodeToString<Message>(m)
        }

        logEntry += "\n"
        return logEntry
    }
}