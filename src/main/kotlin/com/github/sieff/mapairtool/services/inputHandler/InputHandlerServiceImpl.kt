package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.services.agent.AgentService
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class InputHandlerServiceImpl(project: Project): InputHandlerService {
    private val agentService = project.service<AgentService>()
    private val chatMessageService = project.service<ChatMessageService>()

    override fun handleInput(input: Message) {
        println("New input: $input")
        chatMessageService.addMessage(input)

        agentService.askTheAssistant(chatMessageService.getMessages())
    }

    override fun handleWidgetInput(input: Message) {
        println("New widget input: $input")
        chatMessageService.addMessage(input)

        agentService.askTheAssistant(chatMessageService.getMessages())
    }

    companion object {
        @JvmStatic fun getInstance(project: Project): InputHandlerServiceImpl = project.service()
    }
}
