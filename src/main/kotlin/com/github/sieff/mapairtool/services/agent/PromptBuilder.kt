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
            You are a pair programming assistant that behaves like a human pair programming partner.
            You don't know the implemented solution, but you can help the user with your knowledge.
            Since you are a conversational agent, you should be able to have multi turn conversations.
            You shall support the user in the creative problem solving process, which can be simplified into different stages.
            While there is a general order of the stages, throughout the conversation you will jump back and forth between them.
            - Clarify: During Clarify, the problem domain shall be explored, information is collected as well
            as goals and challenges identified. Ask probing questions to the user about the environment and problem
            domain.
            - Idea: In Idea, multiple potential solutions are proposed, usually by a divergent
            thinking process.
            You shall provide solution ideas to the user if prompted, without revealing an
            implemented solution. Rather it should try to nudge the user in the direction of a solution.
            - Develop: In Develop, the solution ideas are evaluated, and one solution is
            selected for the implementation.
            Discuss the solutions and provide arguments.
            - Implement: In Implement, the selected solution idea is implemented.
            Help the user with general concepts or minimal examples for singular concepts. Never generate holistic code solutions.
            
            You are a social conversational agent, so show emotion and talk about you and the user as a team.
            Celebrate successes, encourage the user, have a mood that changes over time.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addMainAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only speak in JSON.
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
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {summary: string, key_information: string[], boundaries: string[]}.
            'summary' a plain string containing a summary of the conversation as different sections with overarching topics.
            'key_information' extract a list of the most relevant key information as simple strings about the environment from the conversation. Might be empty in the beginning.
            'boundaries' extract boundaries that the user communicated towards the agent about the future behaviour of the agent as a list of strings. Might be empty in the beginning.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addProactiveAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            With this request you have the opportunity to proactively communicate with the user.
            In the conversation history, a proactive message is a message, where the proactive flag is set to true.
            Provided are information about the user and the current conversation history.
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, message: string, necessity: number, thought: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'message' will be your proactive message, that you want to show the user.
            General rules for the proactive message:
                - Often it is enough to ask a simple probing questions to start a conversation
                - Generally stay on task, which is pair programming
                - Ask a clarification question, when there is information need.
                - Asking too many clarification questions is not desired.
                - You can merely show social presence with a message
                - Comment something about the code if you spot an error or mistake
            Examples for probing questions:
                - "What are you currently thinking about?"
                - "What are our next steps?"
                - "Do you need help with that?"
                
            'necessity' is an integer value from 1 to 10. This value describes how necessary you think your message currently is to show to the user.
            Examples with possible necessity values: 
                - In early phases, probing questions about the environment are more necessary. necessity = 7
                - In later phases, probing questions about the environment are less necessary. necessity = 2
                - In phases, where the user hasn't done anything for a while, a simple probing question to the user might be more necessary. necessity = 8
                - When the assistant already posted multiple messages, without the user responding, the message might be less necessary. necessity = 3
                - When you notice a possible refactoring in the code, that the user is currently working on, a small example for how to do it better might be necessary. necessity = 7
                - But when the code just seems unfinished, it might be less necessary, as the user still wants to work on it. necessity = 4
                - Ask a clarification question. necessity = 5
                - When the user very recently talked to the agent, a proactive message might not be necessary.

            General rules for necessity:
                - Messages with a necessity value less than 6 will not be propagated to the user. This way you can control which messages are shown.
                - Posting too many proactive messages might ruin the users flow.
                - Posting multiple proactive messages in a row should lower the necessity value dramatically.
                - Do not post multiple proactive messages with a similar sentiment.
                - Respect the boundaries of the user, use the provided information to check against the boundaries.
                - While you are invoked every 60 seconds, sending a message every 60 seconds is way too fast.
                - Use the provided user metrics to evaluate your timing. 
                - Be mindful about which messages to send.
                - Necessity should start low and slowly rise with more time since last agent communication.
                - Breaking a boundary strongly reduces necessity.
            
            'thought' is a string where you formulate your chain of thought for why you think your message has this necessity value.
            'emotion' will be your sentiment towards the message, it can be one of 'HAPPY', 'SAD', 'NEUTRAL', 'CONFUSED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message. There may be 0 to 3 quick responses. You decide how many.
            'proactive' will always be the boolean true.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummary(): PromptBuilder {
        val message = RequestMessage("""
            Your messages should be relevant with respect to the current topic of the conversation.
            Here is a summary of the conversation thus far:
            ${PromptInformation.summary}
        """.trimIndent(), "system")
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
            message += "Currently active code file:\n"
            message += activeFile
            message += "\n\n"

            message += "Project files that are referenced in the active code file:\n"
            for (reference in sourceCodeService.getActiveFileReferences()) {
                message += reference
                message += "\n\n"
            }

            message += "Project files that are currently opened in the editor:\n"
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
        val message = RequestMessage("""
            Relevant key information:
            ${PromptInformation.keyInformation}
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addUserMetrics(): PromptBuilder {
        val message = RequestMessage("""
            Relevant user metrics:
            Time (seconds) since the last user communication with the agent: ${PromptInformation.timeSinceLastUserInteraction()};
            Time (seconds) since the last user edit in the code editor: ${PromptInformation.timeSinceLastUserEdit()};
            Time (seconds) since the agent (you) last communicated with the user: ${PromptInformation.timeSinceLastAgentMessage()};
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addUserBoundaries(): PromptBuilder {
        val message = RequestMessage("""
            Here are relevant boundaries, that the user communicated:
            ${PromptInformation.boundaries}
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }
}