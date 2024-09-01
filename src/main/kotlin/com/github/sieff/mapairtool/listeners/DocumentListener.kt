package com.github.sieff.mapairtool.listeners

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class DocumentListener(private val onDocumentChanged: (DocumentEvent) -> Unit) :
    DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        onDocumentChanged(event)
    }
}