package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.ui.popup.PopupInvoker
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.remoteDev.tracing.getCurrentTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import kotlin.concurrent.thread


class AgentServiceImpl(val project: Project): AgentService {
    private val chatMessageService = project.service<ChatMessageService>()
    private val promptService = project.service<PromptService>()

    private val logger = Logger(this.javaClass)

    private val url = URL("https://api.openai.com/v1/chat/completions")
    private val model = "gpt-4o-mini"
    private val proactiveInvocationInterval = 60_000L

    init {
        startProactiveAgent()
    }

    override fun invokeMainAgent() {
        logger.debug(chatMessageService.getMessages().count())
        CompletableFuture.supplyAsync {
            getAiCompletion(promptService.getMainAgentPrompt(model))
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                PopupInvoker.invokePopup(project)
                val message = result.choices[0].message.content
                chatMessageService.addMessage(getAssistantMessage(message))
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

                logger.debug(PromptInformation.summary)
                logger.debug(PromptInformation.keyInformation)
                logger.debug(PromptInformation.boundaries)
            }
        }
    }

    private fun startProactiveAgent() {
        thread {
            while (true) {
                Thread.sleep(proactiveInvocationInterval)
                if (AppSettingsState.getInstance().state.apiKey == "") {
                    logger.warn("ApiKey not set, can't invoke proactive agent.")
                    continue
                }

                if (PromptInformation.timeSinceLastChatInputEdit() < 10) {
                    logger.info("User is typing.")
                    continue
                }

                if (PromptInformation.timeSinceLastAgentMessage() < 55) {
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

    private fun getAiCompletion(prompt: CompletionRequest): ChatCompletion {
        try {
            val connection: HttpURLConnection = createConnection(prompt)

            val responseCode: Int = connection.getResponseCode()
            if (responseCode != 200) {
                logger.error("Response Code: $responseCode")
                return getErrorResponse(Bundle.message("errors.apiError"))
            }

            val response = readResponse(connection)
            logger.debug("Response:")
            logger.debug(response)

            return ChatCompletionSerializer.json.decodeFromString<ChatCompletion>(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getErrorResponse(Bundle.message("errors.unforeseenError"))
    }

    private fun createConnection(prompt: CompletionRequest): HttpURLConnection {
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

        connection.setRequestMethod("GET")
        connection.setDoOutput(true)

        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Authorization", "Bearer ${AppSettingsState.getInstance().state.apiKey}")

        val body: String = Json.encodeToString(prompt)
        logger.debug("Request body:")
        logger.debug(body)

        connection.outputStream.use { os ->
            val input: ByteArray = body.toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
        }

        return connection
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val `in` = BufferedReader(InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
        var inputLine: String?
        val response = StringBuilder()
        while ((`in`.readLine().also { inputLine = it }) != null) {
            response.append(inputLine)
        }
        `in`.close()

        return response.toString()
    }

    private fun getErrorResponse(message: String): ChatCompletion {
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

    private fun getProactiveMessage(content: String): AssistantMessage {
        try {
            val rawMessage = MessageSerializer.json.decodeFromString<AssistantMessage>(content)
            logger.debug("Necessity value: ${rawMessage.necessity}")
            logger.debug("CoT: ${rawMessage.thought}")
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.message, rawMessage.emotion, rawMessage.reactions, true, rawMessage.necessity, rawMessage.thought)
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
