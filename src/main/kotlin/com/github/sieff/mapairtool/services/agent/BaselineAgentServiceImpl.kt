package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.ui.popup.PopupInvoker
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities


class BaselineAgentServiceImpl(val project: Project): AgentService() {
    private val chatMessageService = project.service<ChatMessageService>()
    private val promptService = project.service<PromptService>()

    private val logger = Logger(this.javaClass)

    override fun invokeMainAgent() {
        logger.info("Using Baseline agent")
        CompletableFuture.supplyAsync {
            getAiCompletion(promptService.getMainAgentPrompt(model))
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                PopupInvoker.invokePopup(project)
                val message = result.choices[0].message.content
                val assistantMessage = getAssistantMessage(message)
                chatMessageService.addMessage(assistantMessage)

                PromptInformation.lastAgentMessage = getCurrentTime()
            }
        }
    }

    override fun getErrorResponse(message: String): ChatCompletion {
        val choices = ArrayList<Choice>()
        val rawMessage = getErrorMessage(message)
        val rawMessageJson = Json.encodeToString(rawMessage)
        choices.add(Choice(0, CompletionMessage("assistant", rawMessageJson), null, ""))
        return ChatCompletion("", "", 0, "", "", choices, Usage(0, 0, 0))
    }

    private fun getErrorMessage(message: String): AssistantMessage {
        return AssistantMessage(MessageOrigin.AGENT, message, Emotion.SAD, ArrayList(), false, 1, "")
    }

    private fun getAssistantMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.message, rawMessage.emotion, rawMessage.reactions, false, 5, "")
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return getErrorMessage(Bundle.message("errors.parsingError"))
        }
    }

}
