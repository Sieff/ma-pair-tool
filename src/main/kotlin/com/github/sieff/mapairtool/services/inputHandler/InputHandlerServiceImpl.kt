package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.message.BaseMessage
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.agent.AgentServiceContext
import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime

class InputHandlerServiceImpl(project: Project): InputHandlerService {
    private val agentServiceContext = project.service<AgentServiceContext>()
    private val chatMessageService = project.service<ChatMessageService>()

    private val logger = Logger(this.javaClass)

    override fun handleInput(input: BaseMessage) {
        logger.debug("New input: $input")
        chatMessageService.addMessage(input)
        ConversationInformation.lastUserMessage = getCurrentTime()

        agentServiceContext.invokeAgent()
    }
}
