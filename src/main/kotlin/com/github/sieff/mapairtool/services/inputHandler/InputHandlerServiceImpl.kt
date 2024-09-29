package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.agent.AgentServiceContext
import com.github.sieff.mapairtool.services.agent.PromptInformation
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime

class InputHandlerServiceImpl(project: Project): InputHandlerService {
    private val agentServiceContext = project.service<AgentServiceContext>()
    private val chatMessageService = project.service<ChatMessageService>()

    private val logger = Logger(this.javaClass)

    override fun handleInput(input: Message) {
        logger.debug("New input: $input")
        chatMessageService.addMessage(input)
        PromptInformation.lastUserInteraction = getCurrentTime()

        agentServiceContext.invokeAgent()
    }

    override fun handleWidgetInput(input: Message) {
        logger.debug("New widget input: $input")
        chatMessageService.addMessage(input)
        PromptInformation.lastUserInteraction = getCurrentTime()

        agentServiceContext.invokeAgent()
    }
}
