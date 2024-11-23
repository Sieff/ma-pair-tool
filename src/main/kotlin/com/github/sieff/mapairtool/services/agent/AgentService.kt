package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.model.chatCompletion.ChatCompletion
import com.github.sieff.mapairtool.model.chatCompletion.ChatCompletionSerializer
import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.util.Logger
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets

abstract class AgentService(open val project: Project) {
    private val logger = Logger(this.javaClass)

    private val url: URL = URI("https://api.openai.com/v1/chat/completions").toURL()
    protected val model = "gpt-4o-mini"

    protected var stopped = false

    abstract fun invokeMainAgent()
    protected abstract fun getErrorResponse(message: String): ChatCompletion

    fun stop() {
        stopped = true
    }

    protected fun getAiCompletion(prompt: CompletionRequest): ChatCompletion {
        try {
            val connection: HttpURLConnection = createConnection(prompt)

            val responseCode: Int = connection.getResponseCode()
            if (responseCode != 200) {
                logger.error("Response Code: $responseCode")
                logger.error("Response: ${readResponse(connection)}")
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
        connection.setRequestProperty("Authorization", "Bearer ${project.service<AppSettingsState>().state.apiKey}")

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
}