package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


class StarterAgentServiceImpl(val project: Project): AgentService() {
    private val chatMessageService = project.service<ChatMessageService>()

    private val logger = Logger(this.javaClass)

    override fun invokeMainAgent() {
        logger.info("Using starter agent")
        val assistantMessage = getMessage(Bundle.getMessage("errors.noAgent"))
        chatMessageService.addMessage(assistantMessage)
    }

    override fun getErrorResponse(message: String): ChatCompletion {
        val choices = ArrayList<Choice>()
        choices.add(Choice(0, CompletionMessage("assistant", message), null, ""))
        return ChatCompletion("", "", 0, "", "", choices, Usage(0, 0, 0))
    }

    private fun getMessage(content: String): BaseMessage {
        return Message(MessageOrigin.AGENT, content)
    }

}
