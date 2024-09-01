package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.completionRequest.RequestMessage
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.model.message.MessageSerializer
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.sourceCode.SourceCodeService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString

class PromptBuilder(project: Project, val model: String) {
    private val chatMessageService = project.service<ChatMessageService>()
    private val sourceCodeService = project.service<SourceCodeService>()

    private var requestMessages: MutableList<RequestMessage> = ArrayList()

    fun build(): CompletionRequest {
        return CompletionRequest(model, requestMessages)
    }

    private fun addMessage(message: RequestMessage) {
        requestMessages.add(message)
    }

    fun addAgentRole(): PromptBuilder {
        val message = RequestMessage("""
            You are a pair programming assistant that behaves like a human pair programming partner,
            so you don't know the implemented solution, but you can help the user with your knowledge.
            You can only speak in JSON.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addMainAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, message: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'message' will be your original response.
            'emotion' will be your sentiment towards the query or response, it can be one of 'HAPPY', 'SAD', 'NEUTRAL', 'CONFUSED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message. There may be 0 to 3 quick responses. You decide how many.
            They should be short messages consisting of 1 or 2 words. Shorter is better. 
            They should be distinct messages, if the sentiment similarity between messages is bigger then 50%, don't include both.
            'proactive' will always be the boolean false.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummaryAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {summary: string, key_information: string}.
            'summary' Summarize the given conversation into different sections with overarching topics.
            'key_information' Extract key information from the conversation and provide them in a string format that can be used in future request.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addProactiveAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            With this request you have the opportunity to proactively communicate with the user.
            Provided are information about the user and the current conversation history.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {necessity: number, thought: string, message: string, emotion: string, reactions: string[]}.
            'necessity' is an integer value from 1 to 10. This value describes how necessary you think your message currently is to show to the user.
            For example: 
                - In early phases, probing questions about the environment are more necessary. necessity = 10
                - In later phases, probing questions about the environment are less necessary. necessity = 1
                - In phases, where the user hasn't done anything for a while, a simple probing question to the user might be more necessary. necessity = 8
                - When the assistant already posted multiple messages, without the user responding, the message might be less necessary. necessity = 3
                - When you notice a possible refactoring in the code, that the user is currently working on, a small example for how to do it better might be necessary. necessity = 7
                - But when the code just seems unfinished, it might be less necessary, as the user still wants to work on it. necessity = 4
            'thought' is a string where you formulate, why you think your message has this necessity value.
            'message' will be your proactive message, that you want to show the user.
            'emotion' will be your sentiment towards the message, it can be one of 'HAPPY', 'SAD', 'NEUTRAL', 'CONFUSED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message. There may be 0 to 3 quick responses. You decide how many.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummary(): PromptBuilder {
        val message = RequestMessage("Ignore this for now", "system")
        addMessage(message)
        return this
    }

    fun addConversationHistory(): PromptBuilder {
        val requestMessages = chatMessageService.getMessages().map {
            val role: String = if (it.origin == MessageOrigin.AGENT) "assistant" else "user"
            RequestMessage(MessageSerializer.json.encodeToString(it), role)
        }

        for (request in requestMessages) {
            addMessage(request)
        }
        return this
    }

    fun addSourceCode(): PromptBuilder {
        val activeFile = sourceCodeService.getActiveFile()
        if (activeFile != null) {
            var message = ""
            message += "This is the currently active code file:\n"
            message += activeFile
            message += "\n\n"

            message += "These are project files that are referenced in the active code file:\n"
            for (reference in sourceCodeService.getActiveFileReferences()) {
                message += reference
                message += "\n\n"
            }

            message += "These are project files that are currently opened in the editor:\n"
            for (active in sourceCodeService.getActiveFiles()) {
                message += active
                message += "\n\n"
            }

            val requestMessage = RequestMessage(message, "system")
            addMessage(requestMessage)
        }
        return this
    }

    fun addKeyInformation(): PromptBuilder {
        val message = RequestMessage("Ignore this for now", "system")
        addMessage(message)
        return this
    }

    fun addUserMetrics(): PromptBuilder {
        val message = RequestMessage("Ignore this for now", "system")
        addMessage(message)
        return this
    }

    fun addUserBoundaries(): PromptBuilder {
        val message = RequestMessage("Ignore this for now", "system")
        addMessage(message)
        return this
    }
}