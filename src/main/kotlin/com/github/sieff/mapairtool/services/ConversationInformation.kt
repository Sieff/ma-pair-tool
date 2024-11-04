package com.github.sieff.mapairtool.services

import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit

object ConversationInformation {
    var lastUserMessage: Long = getCurrentTime()
    var lastAgentMessage: Long = getCurrentTime()
    var lastProactiveMessageTry: Long = getCurrentTime()
    var facts: List<String> = ArrayList()
    var goals: List<String> = ArrayList()
    var challenges: List<String> = ArrayList()
    var summary: String = ""
    var boundaries: List<String> = ArrayList()

    var caretLine: Int = 0


    fun secondsSinceLastUserMessage(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserMessage)
    }

    fun secondsSinceLastAgentMessage(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastAgentMessage)
    }

    fun secondsSinceLastProactiveMessageTry(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastProactiveMessageTry)
    }

    fun resetTimers() {
        lastUserMessage = getCurrentTime()
        lastAgentMessage = getCurrentTime()
    }

    fun reset() {
        lastUserMessage = getCurrentTime()
        lastAgentMessage = getCurrentTime()
        facts = ArrayList()
        goals = ArrayList()
        challenges = ArrayList()
        summary = ""
        boundaries = ArrayList()
    }
}