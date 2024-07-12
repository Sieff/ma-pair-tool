package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class InputHandlerService(project: Project): IInputHandlerService {
    private val agentService = project.service<AgentService>()
    private val chatMessageService = project.service<ChatMessageService>()

    override fun handleInput(input: String) {
        val message = Message(MessageOrigin.USER, input)
        chatMessageService.publishMessage(message)

        agentService.postMessage(input)
    }
}
