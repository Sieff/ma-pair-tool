package com.github.sieff.mapairtool.services.cefBrowser

import com.github.sieff.mapairtool.model.dataPacket.ColorScheme
import com.github.sieff.mapairtool.model.message.ChatMessageState
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer
import com.intellij.ui.jcef.JBCefBrowser

interface CefBrowserService: Observer<ChatMessageState> {
    var toolWindowBrowser: JBCefBrowser?
    var widgetBrowser: JBCefBrowser?
    fun sendMessages(state: ChatMessageState)
    fun sendCurrentState()
    fun requestToolWindowFocus()
    fun requestToolWindow()
    fun updateLogStatus(status: Boolean)
    fun updateProcessingStatus(processing: Boolean)
    fun updateBundle(bundle: String)
    fun updateColorScheme(colorScheme: ColorScheme)
    fun updateStudyGroup(studyGroup: Int)
}