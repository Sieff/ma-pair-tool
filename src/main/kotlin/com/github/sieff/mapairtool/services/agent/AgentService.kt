package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class AgentService(project: Project): IAgentService {
    private val chatMessageService = project.service<ChatMessageService>()
    private val logger = Logger.getInstance(AgentService::class.java)

    override fun postMessage(message: String) {
        logger.info(message)
        chatMessageService.publishMessage(Message(MessageOrigin.AGENT, "Response, 009ED1FF009ED1FF009ED1FF009ED1FF009ED1FF009ED1FF009ED1FF, 009ED1FF 009ED1FF 009ED1FF 009ED1FF 009ED1FF"))
    }
}
