package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.*
import com.github.sieff.mapairtool.model.message.Message
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.completionRequest.RequestMessage
import com.github.sieff.mapairtool.model.message.BaseMessage
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


class AgentServiceImpl(val project: Project): AgentService {
    private val chatMessageService = project.service<ChatMessageService>()

    override fun askTheAssistant(messages: List<BaseMessage>) {
        CompletableFuture.supplyAsync {
            getAiCompletion(messages)
        }.thenAccept { result: ChatCompletion ->
            SwingUtilities.invokeLater {
                PopupInvoker.invokePopup(project)
                chatMessageService.addMessage(Message(MessageOrigin.AGENT, result.choices[0].message.content))
            }
        }
    }

    private fun getAiCompletion(messages: List<BaseMessage>): ChatCompletion {
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

            val body: String = Json.encodeToString(getRequest("gpt-4o-mini", messages))
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

    fun getRequest(model: String, messages: List<BaseMessage>): CompletionRequest {
        val requestMessages = messages.map {
            val role: String = if (it.origin == MessageOrigin.AGENT) "assistant" else "user"
            RequestMessage(it.message, role)
        }.toMutableList()

        requestMessages.add(0, getSystemMessage())

        return CompletionRequest(model, requestMessages)
    }

    fun getSystemMessage(): RequestMessage {
        return RequestMessage("You are a pair programming assistant that behaves like a human pair programming partner, so you don't know the implemented solution, but you can help the user with your knowledge.", "system")
    }

    fun getErrorResponse(message: String): ChatCompletion {
        val choices = ArrayList<Choice>()
        choices.add(Choice(0, CompletionMessage("assistant", message), null, ""))
        return ChatCompletion("", "", 0, "", "", choices, Usage(0, 0, 0))
    }
}
