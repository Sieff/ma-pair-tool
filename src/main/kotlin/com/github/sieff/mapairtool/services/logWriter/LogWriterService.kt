package com.github.sieff.mapairtool.services.logWriter

import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer

interface LogWriterService: Observer<ChatMessageState> {
    fun startNewLog(): Boolean
}