package com.github.sieff.mapairtool.services

import com.intellij.remoteDev.tracing.getCurrentTime
import java.util.concurrent.TimeUnit

object UserTelemetryInformation {
    var lastChatInputEdit: Long = getCurrentTime()
    var lastUserEdit: Long = getCurrentTime()

    fun secondsSinceLastUserEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastUserEdit)
    }

    fun secondsSinceLastChatInputEdit(): Long {
        return TimeUnit.NANOSECONDS.toSeconds(getCurrentTime() - lastChatInputEdit)
    }

    fun resetTimers() {
        lastChatInputEdit = getCurrentTime()
        lastUserEdit = getCurrentTime()
    }

    fun reset() {
        lastChatInputEdit = getCurrentTime()
        lastUserEdit = getCurrentTime()
    }
}