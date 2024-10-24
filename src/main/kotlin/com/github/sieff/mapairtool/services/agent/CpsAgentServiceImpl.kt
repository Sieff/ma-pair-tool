package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.settings.AppSettingsState
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

    private val proactiveInvocationInterval = 5_000L // 5 seconds

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

                // PopupInvoker.invokePopup(project)

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
                PromptInformation.facts = summaryMessage.facts
                PromptInformation.goals = summaryMessage.goals
                PromptInformation.challenges = summaryMessage.challenges
                PromptInformation.boundaries = summaryMessage.boundaries

                logger.debug("Summary: ${PromptInformation.summary}")
                logger.debug("Facts: ${PromptInformation.facts}")
                logger.debug("Goals: ${PromptInformation.goals}")
                logger.debug("Challenges: ${PromptInformation.challenges}")
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
                if (!checkProactiveInvocationTiming()) {
                    continue
                }

                CompletableFuture.supplyAsync {
                    getAiCompletion(promptService.getProactiveAgentPrompt(model))
                }.thenAccept { result: ChatCompletion ->
                    SwingUtilities.invokeLater {
                        // PopupInvoker.invokePopup(project)

                        val message = result.choices[0].message.content
                        val proactiveMessage = getProactiveMessage(message)
                        handleProactiveMessage(proactiveMessage)
                    }
                }
            }
        }
    }

    private fun checkProactiveInvocationTiming(): Boolean {
        if (AppSettingsState.getInstance().state.apiKey == "") {
            logger.warn("ApiKey not set, can't invoke proactive agent.")
            return false
        }

        if (PromptInformation.timeSinceLastChatInputEdit() < 5) {
            logger.info("User is typing in the chat.")
            return false
        }

        if (PromptInformation.timeSinceLastUserEdit() < 5) {
            logger.info("User is typing in the editor.")
            return false
        }

        if (PromptInformation.timeSinceLastAgentMessage() < 60) {
            logger.info("Communicated recently, not invoking proactive message. (${PromptInformation.timeSinceLastAgentMessage()} seconds ago)")
            return false
        }

        return true
    }

    private fun handleProactiveMessage(message: AssistantMessage) {
        if (message.necessity <= 3) {
            logger.info("Necessity of proactive message is too low.")
            return
        }

        val lastMessage = chatMessageService.getLastAgentMessage()

        CompletableFuture.supplyAsync {
            getAiCompletion(promptService.getSimilarityPrompt(model, message, lastMessage))
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                val content = result.choices[0].message.content
                val similarity = getMessageSimilarity(content)

                if (similarity.similarity <= 0.5f) {
                    chatMessageService.addMessage(message)
                    PromptInformation.lastAgentMessage = getCurrentTime()
                } else {
                    logger.info("Proactive message too similar to last message.")
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

    private fun getMessageSimilarity(content: String): MessageSimilarity {
        try {
            val messageSimilarity = Json.decodeFromString<MessageSimilarity>(content)
            logger.debug("Message similarity: ${messageSimilarity.similarity}")
            return messageSimilarity
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return MessageSimilarity(0.0f)
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
