package com.github.sieff.mapairtool.services.agent

import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit

object PromptInformation {
    var lastChatInputEdit: Long = getCurrentTime()
    var lastUserEdit: Long = getCurrentTime()
    var lastUserInteraction: Long = getCurrentTime()
    var lastAgentMessage: Long = getCurrentTime()
    var keyInformation: List<String> = ArrayList()
    var summary: String = ""
    var boundaries: List<String> = ArrayList()

    fun timeSinceLastUserEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserEdit)
    }

    fun timeSinceLastUserInteraction(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserInteraction)
    }

    fun timeSinceLastAgentMessage(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastAgentMessage)
    }

    fun timeSinceLastChatInputEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastChatInputEdit)
    }
}