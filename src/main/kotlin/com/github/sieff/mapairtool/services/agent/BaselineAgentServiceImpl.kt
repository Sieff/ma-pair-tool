package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities


class BaselineAgentServiceImpl(val project: Project): AgentService() {
    private val chatMessageService = project.service<ChatMessageService>()
    private val promptService = project.service<PromptService>()
    private val cefBrowserService = project.service<CefBrowserService>()

    private val logger = Logger(this.javaClass)

    override fun invokeMainAgent() {

        logger.info("Using Baseline agent")
        cefBrowserService.updateProcessingStatus(true)
        CompletableFuture.supplyAsync {
            getAiCompletion(promptService.getBaselineAgentPrompt(model))
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                cefBrowserService.updateProcessingStatus(false)
                val message = result.choices[0].message.content
                val assistantMessage = getMessage(message)
                chatMessageService.addMessage(assistantMessage)
            }
        }
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
