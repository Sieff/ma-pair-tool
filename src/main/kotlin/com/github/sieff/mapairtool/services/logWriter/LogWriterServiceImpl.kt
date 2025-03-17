package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo
import kotlin.io.use

class LogWriterServiceImpl(val project: Project): LogWriterService {
    private val documentManager = PsiDocumentManager.getInstance(project)
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
            writeToFile(conversationLog!!, createLogMessage(message))
        }
    }

    override fun logSummary(summary: SummaryMessage) {
        if (!conversationLogIsReady()) {
            startNewLog()
        }

        if (conversationLogIsReady()) {
            writeToFile(conversationLog!!, "${getCurrentTime()},${Json.encodeToString(summary)}\n")
        }
    }

    override fun logReset() {
        if (!conversationLogIsReady()) {
            startNewLog()
        }

        if (conversationLogIsReady()) {
            writeToFile(conversationLog!!, "${getCurrentTime()},\"Conversation reset\"\n")
        }
    }

    override fun logEdit(type: String, fragment: String) {
        if (!editLogIsReady()) {
            startNewLog()
        }

        val editedFragment = fragment.replace("\\n", "%%%&NEWLINE&%%%").replace("\n", "\\n").replace("\"", "\"\"")
        if (editLogIsReady()) {
            writeToFile(editLog!!, "\"${getCurrentTime()}\",\"$type\",\"$editedFragment\"\n")
        }
    }

    override fun logSessionStart() {
        if (!conversationLogIsReady()) {
            startNewLog()
        }
        if (conversationLogIsReady()) {
            writeToFile(conversationLog!!, "${getCurrentTime()},\"Started new session\"\n")
        }
    }

    override fun logFiles(logDirectoryName: String, documents: Set<Document>) {
        val logDirPath = project.basePath?.let { Paths.get(it, LOG_DIR, logDirectoryName) }
        val projectBasePath = project.basePath?.let { Paths.get(it) }
        if (logDirPath == null || projectBasePath == null || !File(projectBasePath.pathString).exists()) {
            logger.warn("Base path not set or directory doesn't exist.")
            return
        }

        for (document in documents) {
            val virtualFile = documentManager.getPsiFile(document)?.virtualFile ?: continue
            val relativePath = getValidRelativePath(virtualFile.path, projectBasePath) ?: continue

            val filePath = Paths.get(logDirPath.pathString, relativePath.pathString)
            val fileDir = File(filePath.parent.pathString)
            fileDir.mkdirs()

            val fileCopy = File(filePath.pathString)
            fileCopy.createNewFile()
            writeToFile(fileCopy, document.text)
        }
    }

    private fun getValidRelativePath(documentPath: String, basePath: Path): Path? {
        if (!Paths.get(documentPath).pathString.contains(basePath.pathString)) {
            return null
        }
        return Paths.get(documentPath).relativeTo(basePath)
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
        var logEntry = "${getCurrentTime()},"

        if (message is AssistantMessage) {
            logEntry += MessageSerializer.json.encodeToString<AssistantMessage>(message)
        }
        if (message is Message) {
            logEntry += MessageSerializer.json.encodeToString<Message>(message)
        }
        if (message is QuickReactionMessage) {
            logEntry += MessageSerializer.json.encodeToString<QuickReactionMessage>(message)
        }

        logEntry += "\n"
        return logEntry
    }

    private fun writeToFile(log: File, message: String) {
        FileOutputStream(log, true).bufferedWriter().use { out ->
            out.write(message)
        }
    }
}
