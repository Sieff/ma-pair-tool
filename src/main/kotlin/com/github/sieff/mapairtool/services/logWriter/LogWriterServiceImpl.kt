package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths

class LogWriterServiceImpl(val project: Project): LogWriterService {
    private val logger = Logger(this.javaClass)
    private val LOG_DIR = ".cpsLog"

    private var conversationLog: File? = null
    private var editLog: File? = null

    override fun startNewLog() {
        conversationLog = createNewConversationLog()
        editLog = createNewEditLog()

        val success =  conversationLog != null && editLog != null
        project.service<CefBrowserService>().updateLogStatus(success)
    }

    private fun createNewConversationLog(): File? {
        try {
            val logDirPath = createLogDir()

            val logFile = createConversationLogFile(logDirPath)

            return logFile
        } catch (e: Exception) {
            e.message?.let { logger.fatal(it) }

            return null
        }
    }

    private fun createNewEditLog(): File? {
        try {
            val logDirPath = createLogDir()

            val logFile = createEditLogFile(logDirPath)

            return logFile
        } catch (e: Exception) {
            e.message?.let { logger.fatal(it) }

            return null
        }
    }

    override fun logMessage(message: BaseMessage) {
        if (!conversationLogIsReady()) {
            startNewLog()
        }

        if (conversationLogIsReady()) {
            FileOutputStream(conversationLog!!, true).bufferedWriter().use { out ->
                out.write(createLogMessage(message))
            }
        }
    }

    override fun logSummary(summary: SummaryMessage) {
        if (!conversationLogIsReady()) {
            startNewLog()
        }

        if (conversationLogIsReady()) {
            FileOutputStream(conversationLog!!, true).bufferedWriter().use { out ->
                out.write("${getCurrentTime()} - ${Json.encodeToString(summary)}\n")
            }
        }
    }

    override fun logReset() {
        if (!conversationLogIsReady()) {
            startNewLog()
        }

        if (conversationLogIsReady()) {
            FileOutputStream(conversationLog!!, true).bufferedWriter().use { out ->
                out.write("${getCurrentTime()} - \"Conversation reset\"\n")
            }
        }
    }

    override fun logEdit(type: String) {
        if (!editLogIsReady()) {
            startNewLog()
        }
        if (editLogIsReady()) {
            FileOutputStream(editLog!!, true).bufferedWriter().use { out ->
                out.write("${getCurrentTime()} - $type\n")
            }
        }
    }

    private fun conversationLogIsReady(): Boolean {
        return conversationLog != null && conversationLog!!.exists()
    }

    private fun editLogIsReady(): Boolean {
        return editLog != null && editLog!!.exists()
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

    private fun createConversationLogFile(logDirPath: Path): File {
        val logFilePath = logDirPath.resolve("conversation-log.log").toString()
        val logFile = File(logFilePath)

        if (!logFile.exists()) {
            logFile.createNewFile()
            logger.info("Conversation Log file created at: ${logFile.absolutePath}")
        }

        if (!logFile.exists()) {
            throw Exception("Could not create log file")
        }

        return logFile
    }

    private fun createEditLogFile(logDirPath: Path): File {
        val logFilePath = logDirPath.resolve("edit-log.log").toString()
        val logFile = File(logFilePath)

        if (!logFile.exists()) {
            logFile.createNewFile()
            logger.info("Edit Log file created at: ${logFile.absolutePath}")
        }

        if (!logFile.exists()) {
            throw Exception("Could not create log file")
        }

        return logFile
    }

    private fun createLogMessage(message: BaseMessage): String {
        var logEntry = "${getCurrentTime()} - "

        if (message is AssistantMessage) {
            logEntry += MessageSerializer.json.encodeToString<AssistantMessage>(message)
        }
        if (message is Message) {
            logEntry += MessageSerializer.json.encodeToString<Message>(message)
        }

        logEntry += "\n"
        return logEntry
    }
}