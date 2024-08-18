package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.completionRequest.RequestMessage
import com.github.sieff.mapairtool.model.message.*
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.ui.popup.PopupInvoker
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
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

    init {
        thread {
            while (true) {
                Thread.sleep(50_000)
                val message = AssistantMessage(MessageOrigin.AGENT, "Proaktive Testnachricht :)", Emotion.NEUTRAL, ArrayList(), true)
                chatMessageService.addMessage(message)
            }
        }
    }

    override fun askTheAssistant(history: List<BaseMessage>) {
        CompletableFuture.supplyAsync {
            getAiCompletion(history)
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                PopupInvoker.invokePopup(project)
                val message = result.choices[0].message.content
                chatMessageService.addMessage(getAssistantMessage(message))
            }
        }
    }

    private fun getAiCompletion(history: List<BaseMessage>): ChatCompletion {
        try {
            // The URL of the server
            val url = URL("https://api.openai.com/v1/chat/completions")

            // Open a connection to the server
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            // Specify that we want to use the GET method
            connection.setRequestMethod("GET")
            connection.setDoOutput(true)

            println(AppSettingsState.getInstance().state.apiKey)

            // Set the request headers
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${AppSettingsState.getInstance().state.apiKey}")

            val body: String = Json.encodeToString(getRequest("gpt-4o-mini", history))
            println(body)

            connection.outputStream.use { os ->
                val input: ByteArray = body.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // Check if the response code is HTTP OK (200)
            val responseCode: Int = connection.getResponseCode()
            println("Response Code: $responseCode")
            if (responseCode != 200) {
                return getErrorResponse(Bundle.message("errors.apiError"))
            }

            // Read the response from the input stream
            val `in` = BufferedReader(InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
            var inputLine: String?
            val response = StringBuilder()
            while ((`in`.readLine().also { inputLine = it }) != null) {
                response.append(inputLine)
            }
            `in`.close()

            // Print the response
            println(response.toString())
            return ChatCompletionSerializer.json.decodeFromString<ChatCompletion>(response.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getErrorResponse(Bundle.message("errors.unforeseenError"))
    }

    private fun getRequest(model: String, messages: List<BaseMessage>): CompletionRequest {
        val requestMessages = messages.map {
            val role: String = if (it.origin == MessageOrigin.AGENT) "assistant" else "user"
            RequestMessage(MessageSerializer.json.encodeToString(it), role)
        }.toMutableList()

        requestMessages.add(0, getSystemMessage())
        requestMessages.add(getSystemResponseFormatMessage())

        return CompletionRequest(model, requestMessages)
    }

    private fun getSystemMessage(): RequestMessage {
        return RequestMessage("You are a pair programming assistant that behaves like a human pair programming partner, so you don't know the implemented solution, but you can help the user with your knowledge. You can only speak in json.", "system")
    }

    private fun getSystemResponseFormatMessage(): RequestMessage {
        return RequestMessage("""
            You can only speak in JSON. Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, message: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'message' will be your original response.
            'emotion' will be your sentiment towards the query or response, it can be one of 'HAPPY', 'SAD', 'NEUTRAL', 'CONFUSED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message. There may be 0 to 3 quick responses. You decide how many.
            They should be short messages consisting of 1 or 2 words. Shorter is better. 
            They should be distinct messages, if the sentiment similarity between messages is bigger then 50%, don't include both.
            'proactive' will always be the boolean false.
        """.trimIndent(), "system")
    }

    private fun getErrorResponse(message: String): ChatCompletion {
        val choices = ArrayList<Choice>()
        val rawMessage = AssistantMessage(MessageOrigin.AGENT, message, Emotion.SAD, ArrayList(), false)
        val rawMessageJson = Json.encodeToString(rawMessage)
        choices.add(Choice(0, CompletionMessage("assistant", rawMessageJson), null, ""))
        return ChatCompletion("", "", 0, "", "", choices, Usage(0, 0, 0))
    }

    private fun getErrorMessage(message: String): AssistantMessage {
        return AssistantMessage(MessageOrigin.AGENT, message, Emotion.SAD, ArrayList(), false)
    }

    private fun getAssistantMessage(content: String): AssistantMessage {
        try {
            val rawMessage = Json.decodeFromString<AssistantMessage>(content)
            return AssistantMessage(MessageOrigin.AGENT, rawMessage.message, rawMessage.emotion, rawMessage.reactions, false)
        } catch (e: Exception) {
            println(e.message)
            return getErrorMessage(Bundle.message("errors.parsingError"))
        }
    }
}
