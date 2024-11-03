package com.github.sieff.mapairtool.listeners

import com.github.sieff.mapairtool.services.agent.PromptInformation
import com.github.sieff.mapairtool.services.agent.PromptService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.util.Logger
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.psi.PsiDocumentManager
import com.intellij.remoteDev.tracing.getCurrentTime
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class DocumentChangeListener(val project: Project) : ProjectManagerListener {
    private val promptService = project.service<PromptService>()
    private val logWriterService = project.service<LogWriterService>()
    private val LOG_DIR = ".cpsLog"
    private val logger = Logger(DocumentChangeListener::class.java)
    private val documentManager = PsiDocumentManager.getInstance(project)

    private val documentListener = DocumentListener { edit ->
        PromptInformation.lastUserEdit = getCurrentTime()

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
        PromptInformation.caretLine = event.newPosition.line + 1
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
                PromptInformation.lastFileLog = getCurrentTime()

                val logDirPath = project.basePath?.let { Paths.get(it, LOG_DIR, getCurrentTime().toString()) }
                val projectBasePath = project.basePath?.let { Paths.get(it) }
                if (logDirPath == null || projectBasePath == null || !File(projectBasePath.pathString).exists()) {
                    logger.warn("Base path not set or directory doesn't exist.")
                    continue
                }

                SwingUtilities.invokeLater {
                    for (document in editedDocuments) {
                        val relativePath = getValidRelativePath(document, projectBasePath) ?: continue

                        val filePath = Paths.get(logDirPath.pathString, relativePath.pathString)
                        val fileDir = File(filePath.parent.pathString)
                        fileDir.mkdirs()

                        val fileCopy = File(filePath.pathString)
                        fileCopy.createNewFile()
                        fileCopy.writeText(document.text)
                    }
                    editedDocuments.clear()
                }
            }
        }
    }

    private fun getValidRelativePath(document: Document, basePath: Path): Path? {
        val documentPath = documentManager.getPsiFile(document)?.virtualFile?.path ?: ""
        if (!Paths.get(documentPath).pathString.contains(basePath.pathString)) {
            return null
        }
        return Paths.get(documentPath).relativeTo(basePath)
    }

    private fun shouldLogFiles(): Boolean {
        return PromptInformation.secondsSinceLastUserEdit() > 5 && PromptInformation.secondsSinceLastFileLog() > 60
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
            logger.info("Editor released: ${event.editor.virtualFile.path}")
        }
    }
}