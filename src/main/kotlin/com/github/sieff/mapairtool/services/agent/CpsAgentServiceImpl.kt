package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.UserTelemetryInformation
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.logWriter.LogWriterService
import com.github.sieff.mapairtool.settings.AppSettingsConfigurable
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import kotlin.concurrent.thread


class CpsAgentServiceImpl(override val project: Project, private val coroutineScope: CoroutineScope): AgentService(project) {
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

                ConversationInformation.lastAgentMessage = getCurrentTime()
                invokeSummaryAgent()
            }
        }
    }

    private fun invokeSummaryAgent() {
        CompletableFuture.supplyAsync {
            val apiKey = project.service<AppSettingsState>().retrieveApiKey()
            if (apiKey == null || apiKey == "") {
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
                ConversationInformation.summary = summaryMessage.summary
                ConversationInformation.facts = summaryMessage.facts
                ConversationInformation.goals = summaryMessage.goals
                ConversationInformation.challenges = summaryMessage.challenges
                ConversationInformation.boundaries = summaryMessage.boundaries

                logger.debug("Summary: ${ConversationInformation.summary}")
                logger.debug("Facts: ${ConversationInformation.facts}")
                logger.debug("Goals: ${ConversationInformation.goals}")
                logger.debug("Challenges: ${ConversationInformation.challenges}")
                logger.debug("Boundaries: ${ConversationInformation.boundaries}")

                logWriterService.logSummary(summaryMessage)
            }
        }
    }

    private fun startProactiveAgent() {
        thread {
            while (true) {
                if (project.service<AppSettingsState>().state.studyGroup != 2) {
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
        val apiKey = project.service<AppSettingsState>().retrieveApiKey()
        if (apiKey == null || apiKey == "") {
            logger.warn("ApiKey not set, can't invoke proactive agent.")
            return false
        }

        if (ConversationInformation.secondsSinceLastAgentMessage() < 60) {
            logger.info("Communicated recently, not invoking proactive message. (${ConversationInformation.secondsSinceLastAgentMessage()} seconds ago)")
            return false
        }

        if (UserTelemetryInformation.secondsSinceLastChatInputEdit() < 5) {
            logger.info("User is typing in the chat.")
            return false
        }

        if (UserTelemetryInformation.secondsSinceLastUserEdit() < 5) {
            logger.info("User is typing in the editor.")
            return false
        }

        if (ConversationInformation.secondsSinceLastProactiveMessageTry() < 10) {
            logger.info("Tried to create proactive message recently.")
            return false
        }

        return true
    }

    private fun handleProactiveMessage(message: AssistantMessage) {
        val lastMessage = chatMessageService.getLastAgentMessage()

        coroutineScope.launch {
            val similarity = getSimilarity(model, message, lastMessage)

            // Run relevance check only if similarity threshold is met
            if (similarity <= 0.5f) {
                val relevance = getRelevance(model, message)

                if (relevance >= 0.5f) {
                    if (checkProactiveInvocationTiming()) {
                        cefBrowserService.requestToolWindow()
                        chatMessageService.addMessage(message)
                        ConversationInformation.lastAgentMessage = getCurrentTime()
                    }
                } else {
                    logger.info("Proactive message is not relevant enough.")
                }
            } else {
                logger.info("Proactive message too similar to last message.")
            }

            ConversationInformation.lastProactiveMessageTry = getCurrentTime()
        }

    }

    private fun getSimilarity(model: String, message: BaseMessage, lastMessage: BaseMessage): Float {
        val prompt = promptService.getSimilarityPrompt(model, message, lastMessage)
        val result = getAiCompletion(prompt)
        return getMessageSimilarity(result.choices[0].message.content).similarity
    }

    private fun getRelevance(model: String, message: AssistantMessage): Float {
        val prompt = promptService.getRelevancePrompt(model, message)
        val result = getAiCompletion(prompt)
        return getMessageRelevance(result.choices[0].message.content).relevance
    }

    override fun getErrorResponse(message: String): ChatCompletion {
        val choices = ArrayList<Choice>()
        val rawMessage = getErrorMessage(message)
        val rawMessageJson = Json.encodeToString(rawMessage)
        choices.add(Choice(0, CompletionMessage("assistant", rawMessageJson), null, ""))
        return ChatCompletion("", "", 0, "", "", choices, Usage(0, 0, 0))
    }

    private fun getErrorMessage(message: String): AssistantMessage {
        return AssistantMessage(MessageOrigin.AGENT, Phase.CLARIFY, message, Emotion.SAD, ArrayList(), false)
    }

    private fun getAssistantMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.phase, rawMessage.message, rawMessage.emotion, rawMessage.reactions, false)
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return getErrorMessage(Bundle.message("errors.parsingError"))
        }
    }

    private fun getProactiveMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.phase, rawMessage.message, rawMessage.emotion, rawMessage.reactions, true)
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

    private fun getMessageRelevance(content: String): MessageRelevance {
        try {
            val messageRelevance = Json.decodeFromString<MessageRelevance>(content)
            logger.debug("Message relevance: ${messageRelevance.relevance}")
            return messageRelevance
        } catch (e: Exception) {
            e.message?.let { logger.error(it) }
            return MessageRelevance(0.0f)
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
