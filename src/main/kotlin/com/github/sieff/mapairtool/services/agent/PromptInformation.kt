package com.github.sieff.mapairtool.services.agent

import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit

object PromptInformation {
    var lastChatInputEdit: Long = getCurrentTime()
    var lastUserEdit: Long = getCurrentTime()
    var lastUserInteraction: Long = getCurrentTime()
    var lastAgentMessage: Long = getCurrentTime()
    var lastProactiveMessageTry: Long = getCurrentTime()
    var facts: List<String> = ArrayList()
    var goals: List<String> = ArrayList()
    var challenges: List<String> = ArrayList()
    var summary: String = ""
    var boundaries: List<String> = ArrayList()

    var caretLine: Int = 0

    fun secondsSinceLastUserEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserEdit)
    }

    fun secondsSinceLastUserInteraction(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserInteraction)
    }

    fun secondsSinceLastAgentMessage(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastAgentMessage)
    }

    fun secondsSinceLastChatInputEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastChatInputEdit)
    }

    fun secondsSinceLastProactiveMessageTry(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastProactiveMessageTry)
    }

    fun resetTimers() {
        lastChatInputEdit = getCurrentTime()
        lastUserEdit = getCurrentTime()
        lastUserInteraction = getCurrentTime()
        lastAgentMessage = getCurrentTime()
    }

    fun reset() {
        lastChatInputEdit = getCurrentTime()
        lastUserEdit = getCurrentTime()
        lastUserInteraction = getCurrentTime()
        lastAgentMessage = getCurrentTime()
        facts = ArrayList()
        goals = ArrayList()
        challenges = ArrayList()
        summary = ""
        boundaries = ArrayList()
    }
}