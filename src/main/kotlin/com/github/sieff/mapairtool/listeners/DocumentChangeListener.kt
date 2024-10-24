package com.github.sieff.mapairtool.listeners

import com.github.sieff.mapairtool.services.agent.PromptInformation
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

class DocumentChangeListener(project: Project) : ProjectManagerListener {
    private val promptService = project.service<PromptService>()
    private val logWriterService = project.service<LogWriterService>()

    private val documentListener = DocumentListener { edit ->
        PromptInformation.lastUserEdit = getCurrentTime()

        if (edit.oldFragment.isEmpty() && edit.newFragment.isNotEmpty()) {
            logWriterService.logEdit("add")
        } else if (edit.oldFragment.isNotEmpty() && edit.newFragment.isEmpty()) {
            logWriterService.logEdit("remove")
        } else if (edit.oldFragment.isNotEmpty() && edit.newFragment.isNotEmpty()) {
            logWriterService.logEdit("replace")
        }
    }

    private val caretListener = CaretListener { event ->
        PromptInformation.caretLine = event.newPosition.line + 1
    }

    private val registeredDocuments = mutableSetOf<Document>()

    init {
        // Register the listener for existing editors
        registerListeners()
        // Register a listener for new editors
        EditorFactory.getInstance().addEditorFactoryListener(EditorFactoryListenerImpl(), promptService)
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