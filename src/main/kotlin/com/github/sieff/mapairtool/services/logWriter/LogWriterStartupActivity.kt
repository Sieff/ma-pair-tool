package com.github.sieff.mapairtool.services.logWriter

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class LogWriterStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val logWriterService = project.service<LogWriterService>()
        logWriterService.startNewLog()
        logWriterService.logSessionStart()
    }
}