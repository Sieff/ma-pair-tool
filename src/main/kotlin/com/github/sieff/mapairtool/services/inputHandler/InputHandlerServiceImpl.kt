package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.github.sieff.mapairtool.services.agent.AgentService
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class InputHandlerServiceImpl(project: Project): InputHandlerService {
    private val agentService = project.service<AgentService>()
    private val chatMessageService = project.service<ChatMessageService>()

    override fun handleInput(input: String) {
        println("handling input ${input}")
        val message = Message(MessageOrigin.USER, input)
        chatMessageService.publishMessage(message)

        agentService.postMessage(input)
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): InputHandlerServiceImpl = project.service()
    }
}
