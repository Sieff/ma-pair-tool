package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class AgentServiceContextImpl(val project: Project): AgentServiceContext, Observer<AppSettingsState.State> {
    private var agentService: AgentService? = StarterAgentServiceImpl(project)
    private val chatMessageService = project.service<ChatMessageService>()
    private var apiKey: String? = null
    private val logger = Logger(this.javaClass)

    init {
        AppSettingsState.getInstance().state.subscribe(this)
    }

    override fun invokeAgent() {
        if (apiKey == null || apiKey!!.isEmpty()) {
            logger.info("No API key set")
            val errorMessage = Message(MessageOrigin.AGENT, Bundle.getMessage("errors.noApiKey"))
            chatMessageService.addMessage(errorMessage)
        } else {
            agentService?.invokeMainAgent()
        }
    }

    override fun notify(data: AppSettingsState.State) {
        apiKey = data.apiKey

        logger.info("Studygroup: ${data.studyGroup}")
        if (data.studyGroup == 1) {
            agentService = BaselineAgentServiceImpl(project)
            return
        }
        if (data.studyGroup == 2) {
            agentService = CpsAgentServiceImpl(project)
            return
        }
        agentService = StarterAgentServiceImpl(project)
    }
}