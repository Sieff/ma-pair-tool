package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.UserTelemetryInformation
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.settings.AppSettingsPublisher
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.settings.AppState
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class AgentServiceContextImpl(val project: Project, private val coroutineScope: CoroutineScope): AgentServiceContext, Observer<AppState> {
    private var agentService: AgentService? = StarterAgentServiceImpl(project)
    private val chatMessageService = project.service<ChatMessageService>()
    private val cefBrowserService = project.service<CefBrowserService>()
    private var apiKey: String? = null
    private val logger = Logger(this.javaClass)

    init {
        AppSettingsPublisher.subscribe(this)
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

    override fun notify(data: AppState) {
        apiKey = data.apiKey

        logger.info("Studygroup: ${data.studyGroup}")
        ConversationInformation.resetTimers()
        UserTelemetryInformation.resetTimers()
        cefBrowserService.updateStudyGroup(data.studyGroup)
        cefBrowserService.updateSettingsStatus(data.apiKey != null && data.apiKey!!.isNotBlank(), data.studyGroup == 1 || data.studyGroup == 2)

        if (data.studyGroup == 1) {
            agentService = BaselineAgentServiceImpl(project)
            return
        }
        if (data.studyGroup == 2) {
            agentService = CpsAgentServiceImpl(project, coroutineScope)
            return
        }
        agentService = StarterAgentServiceImpl(project)
    }
}