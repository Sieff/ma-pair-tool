package com.github.sieff.mapairtool.services.agent

import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit

object PromptInformation {
    var lastChatInputEdit: Long = getCurrentTime()
    var lastUserEdit: Long = getCurrentTime()
    var lastUserInteraction: Long = getCurrentTime()
    var lastAgentMessage: Long = getCurrentTime()
    var facts: List<String> = ArrayList()
    var goals: List<String> = ArrayList()
    var challenges: List<String> = ArrayList()
    var summary: String = ""
    var boundaries: List<String> = ArrayList()

    var caretLine: Int = 0

    fun timeSinceLastUserEdit(): Long {
        return TimeUnit.NANOSECONDS.toMinutes(getCurrentTime() - lastUserEdit)
    }

    fun timeSinceLastUserInteraction(): Long {
        return TimeUnit.NANOSECONDS.toMinutes(getCurrentTime() - lastUserInteraction)
    }

    fun timeSinceLastAgentMessage(): Long {
        return TimeUnit.NANOSECONDS.toMinutes(getCurrentTime() - lastAgentMessage)
    }

    fun timeSinceLastChatInputEdit(): Long {
        return TimeUnit.NANOSECONDS.toMinutes(getCurrentTime() - lastChatInputEdit)
    }
}