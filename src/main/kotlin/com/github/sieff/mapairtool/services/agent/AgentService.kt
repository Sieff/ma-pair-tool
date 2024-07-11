package com.github.sieff.mapairtool.services.inputHandler

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class AgentService(project: Project): IAgentService {
    private val logger = Logger.getInstance("AgentService")

    override fun postMessage(message: String) {
        println(message)
        thisLogger().info(message)
    }
}
