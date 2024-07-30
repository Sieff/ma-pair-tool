package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.ui.popup.PopupInvoker
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities


class AgentServiceImpl(val project: Project): AgentService {
    private val chatMessageService = project.service<ChatMessageService>()

    override fun postMessage(message: String) {
        CompletableFuture.supplyAsync {
            getUrlContents()
        }.thenAccept { result: String ->
            SwingUtilities.invokeLater {
                PopupInvoker.invokePopup(project)
                chatMessageService.publishMessage(Message(MessageOrigin.AGENT, result))
            }
        }
    }

    private fun getUrlContents(): String {
        try {
            // The URL of the server
            val url = URL("https://api.openai.com/v1/chat/completions")

            // Open a connection to the server
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            // Specify that we want to use the GET method
            connection.setRequestMethod("GET")
            connection.setDoOutput(true)

            // Set the request headers
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${AppSettingsState.getInstance().state.apiKey}")

            val body: String = "{\n" +
                    "    \"model\": \"gpt-4o-mini\",\n" +
                    "    \"messages\": [\n" +
                    "      {\n" +
                    "        \"role\": \"system\",\n" +
                    "        \"content\": \"You are a poetic assistant, skilled in explaining complex programming concepts with creative flair.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"role\": \"user\",\n" +
                    "        \"content\": \"Compose a poem that explains the concept of recursion in programming.\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }"

            connection.outputStream.use { os ->
                val input: ByteArray = body.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // Check if the response code is HTTP OK (200)
            val responseCode: Int = connection.getResponseCode()
            println("Response Code: $responseCode")
            if (responseCode != 200) {
                return Bundle.message("errors.apiError")
            }

            // Read the response from the input stream
            val `in` = BufferedReader(InputStreamReader(connection.getInputStream()))
            var inputLine: String?
            val response = StringBuilder()
            while ((`in`.readLine().also { inputLine = it }) != null) {
                response.append(inputLine)
            }
            `in`.close()

            // Print the response
            println(response.toString())
            return response.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Bundle.message("errors.unforeseenError")
    }
}
