package com.github.sieff.mapairtool.listeners

import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.UserTelemetryInformation
import com.github.sieff.mapairtool.services.agent.PromptService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

class DocumentChangeListener(val project: Project) : ProjectManagerListener {
    private val promptService = project.service<PromptService>()
    private val logWriterService = project.service<LogWriterService>()
    private var lastFileLog = getCurrentTime()

    private val documentListener = DocumentListener { edit ->
        UserTelemetryInformation.lastUserEdit = getCurrentTime()

        if (edit.oldFragment.isEmpty() && edit.newFragment.isNotEmpty()) {
            logWriterService.logEdit("add")
        } else if (edit.oldFragment.isNotEmpty() && edit.newFragment.isEmpty()) {
            logWriterService.logEdit("remove")
        } else if (edit.oldFragment.isNotEmpty() && edit.newFragment.isNotEmpty()) {
            logWriterService.logEdit("replace")
        }

        editedDocuments.add(edit.document)
    }

    private val caretListener = CaretListener { event ->
        ConversationInformation.caretLine = event.newPosition.line + 1
    }

    private val registeredDocuments = mutableSetOf<Document>()
    private val editedDocuments = mutableSetOf<Document>()

    init {
        // Register the listener for existing editors
        registerListeners()
        // Register a listener for new editors
        EditorFactory.getInstance().addEditorFactoryListener(EditorFactoryListenerImpl(), promptService)
        startFileLogger()
    }

    private fun startFileLogger() {
        thread {
            while (true) {
                Thread.sleep(1000)
                if (!shouldLogFiles()) {
                    continue
                }

                val timestamp = getCurrentTime()
                val logDirectoryName = timestamp.toString()
                lastFileLog = timestamp

                SwingUtilities.invokeLater {
                    logWriterService.logFiles(logDirectoryName, editedDocuments)
                    editedDocuments.clear()
                }
            }
        }
    }

    private fun shouldLogFiles(): Boolean {
        return UserTelemetryInformation.secondsSinceLastUserEdit() > 5 && secondsSinceLastFileLog() > 60
    }

    private fun secondsSinceLastFileLog(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastFileLog)
    }

    private fun registerListeners() {
        val editors = EditorFactory.getInstance().allEditors
        editors.forEach { editor ->
            if (editor.document !in registeredDocuments) {
                editor.document.addDocumentListener(documentListener)
                editor.caretModel.addCaretListener(caretListener)
                registeredDocuments.add(editor.document)
            }
        }
    }

    private fun unregisterListeners() {
        val editors = EditorFactory.getInstance().allEditors
        editors.forEach { editor ->
            if (editor.document in registeredDocuments) {
                editor.document.removeDocumentListener(documentListener)
                editor.caretModel.removeCaretListener(caretListener)
                registeredDocuments.remove(editor.document)
            }
        }
    }

    override fun projectClosed(project: Project) {
        // Unregister listeners when the project is closed
        unregisterListeners()
    }

    private inner class EditorFactoryListenerImpl : EditorFactoryListener {
        override fun editorCreated(event: EditorFactoryEvent) {
            val document = event.editor.document
            if (document !in registeredDocuments) {
                document.addDocumentListener(documentListener)
                event.editor.caretModel.addCaretListener(caretListener)
                registeredDocuments.add(document)
            }
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            val document = event.editor.document
            if (document in registeredDocuments) {
                document.removeDocumentListener(documentListener)
                event.editor.caretModel.removeCaretListener(caretListener)
                registeredDocuments.remove(document)
            }
        }
    }
}