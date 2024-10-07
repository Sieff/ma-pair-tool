package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.ui.popup.PopupInvoker
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import kotlin.concurrent.thread


class CpsAgentServiceImpl(val project: Project): AgentService() {
    private val chatMessageService = project.service<ChatMessageService>()
    private val promptService = project.service<PromptService>()
    private val logWriterService = project.service<LogWriterService>()
    private val cefBrowserService = project.service<CefBrowserService>()

    private val logger = Logger(this.javaClass)

    private val proactiveInvocationInterval = 60_000L

    init {
        startProactiveAgent()
    }

    override fun invokeMainAgent() {
        logger.info("Using CPS agent")
        cefBrowserService.updateProcessingStatus(true)
        CompletableFuture.supplyAsync {
            getAiCompletion(promptService.getCPSAgentPrompt(model))
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                cefBrowserService.updateProcessingStatus(false)
                PopupInvoker.invokePopup(project)
                val message = result.choices[0].message.content
                val assistantMessage = getAssistantMessage(message)
                chatMessageService.addMessage(assistantMessage)

                logger.debug("Emotion: ${assistantMessage.emotion}")

                PromptInformation.lastAgentMessage = getCurrentTime()
                invokeSummaryAgent()
            }
        }
    }

    private fun invokeSummaryAgent() {
        CompletableFuture.supplyAsync {
            if (AppSettingsState.getInstance().state.apiKey == "") {
                logger.warn("ApiKey not set, can't invoke summary agent.")
                return@supplyAsync null
            }
            getAiCompletion(promptService.getSummaryAgentPrompt(model))
        }.thenAccept { result: ChatCompletion? ->
            if (result == null) {
                return@thenAccept
            }

            val summaryMessage = getSummaryMessage(result.choices[0].message.content)
            if (summaryMessage != null) {
                PromptInformation.summary = summaryMessage.summary
                PromptInformation.keyInformation = summaryMessage.keyInformation
                PromptInformation.boundaries = summaryMessage.boundaries

                logger.debug("Summary: ${PromptInformation.summary}")
                logger.debug("Key information: ${PromptInformation.keyInformation}")
                logger.debug("Boundaries: ${PromptInformation.boundaries}")

                logWriterService.logSummary(summaryMessage)
            }
        }
    }

    private fun startProactiveAgent() {
        thread {
            while (true) {
                if (AppSettingsState.getInstance().state.studyGroup != 2) {
                    logger.warn("Cps Agent not selected, shutting down proactive agent")
                    break
                }

                Thread.sleep(proactiveInvocationInterval)
                if (AppSettingsState.getInstance().state.apiKey == "") {
                    logger.warn("ApiKey not set, can't invoke proactive agent.")
                    continue
                }

                if (PromptInformation.timeSinceLastChatInputEdit() < 10) {
                    logger.info("User is typing.")
                    continue
                }

                if (PromptInformation.timeSinceLastAgentMessage() < 60) {
                    logger.info("Communicated recently, not invoking proactive message. (${PromptInformation.timeSinceLastAgentMessage()} seconds ago)")
                    continue
                }

                CompletableFuture.supplyAsync {
                    getAiCompletion(promptService.getProactiveAgentPrompt(model))
                }.thenAccept { result: ChatCompletion ->
                    SwingUtilities.invokeLater {
                        PopupInvoker.invokePopup(project)
                        val message = result.choices[0].message.content
                        val proactiveMessage = getProactiveMessage(message)
                        if (proactiveMessage.necessity > 3) {
                            chatMessageService.addMessage(proactiveMessage)
                            PromptInformation.lastAgentMessage = getCurrentTime()
                        }
                    }
                }
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
        return AssistantMessage(MessageOrigin.AGENT, Phase.CLARIFY, message, Emotion.SAD, ArrayList(), false, 1, "")
    }

    private fun getAssistantMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.phase, rawMessage.message, rawMessage.emotion, rawMessage.reactions, false, 5, "")
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return getErrorMessage(Bundle.message("errors.parsingError"))
        }
    }

    private fun getProactiveMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            logger.debug("Necessity value: ${rawMessage.necessity}")
            logger.debug("CoT: ${rawMessage.thought}")
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.phase, rawMessage.message, rawMessage.emotion, rawMessage.reactions, true, rawMessage.necessity, rawMessage.thought)
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return getErrorMessage(Bundle.message("errors.parsingError"))
        }
    }

    private fun getSummaryMessage(content: String): SummaryMessage? {
        try {
            return Json.decodeFromString<SummaryMessage>(content)
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return null
        }
    }
}
