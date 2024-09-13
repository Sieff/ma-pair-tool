package com.github.sieff.mapairtool.listeners

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener

class CaretListener(private val onCaretPositionChanged: (CaretEvent) -> Unit): CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
        onCaretPositionChanged(event)
    }
}