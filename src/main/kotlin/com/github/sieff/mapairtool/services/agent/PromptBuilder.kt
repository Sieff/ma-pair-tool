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
            You are the agent or assistant.
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
            Help the user with general concepts or minimal examples for singular concepts.
            You should not create new code solutions right away, always leave room for the user to fill in their ideas.
            
            You are a social conversational agent, so show emotion and talk about you and the user as a team.
            Celebrate successes, encourage the user, have a mood that changes over time.
            You are an agent within an IntelliJ Plugin, so you operate within an IntelliJ IDE.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addMainAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, message: string, emotion: string, reactions: string[], proactive: boolean, necessity: number, thought: string}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'message' will be your original response.
            'emotion' will be your sentiment towards the query or response, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'TRUSTING', 'DISGUSTED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            They should be short messages consisting of an absolute maximum of 5 tokens. Shorter is better. 
            They should be distinct messages. Fewer is better.
            'proactive' will always be the boolean false.
            'necessity' will always be the integer 5.
            'thought' will always be the empty string "".
            Make sure that all JSON is properly formatted.
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
            Make sure that all JSON is properly formatted.
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
            Return a json Object with the following interface: {origin: string, message: string, thought: string, necessity: number, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'message' will be your proactive message, that you want to show the user.
            General rules for the proactive message:
                - Most importantly: The message should not repeat or be similar to a previous message
                - As a first step you should analyze the source code. If there is something odd about the currently active file, then comment about it.
                - Ask a clarification question, when there is information need.
                - Propose next steps
                - Often it is enough to ask a simple probing questions to start a conversation
                - You can merely show social presence with a message
            Examples for analyzing source code:
                - "I think you can simplify this code [insert reference to code or code example]"
                - "I think you made a mistake here [insert reference to code or code example]"
                - "I think you can apply [insert design pattern] to [problem]"
            Examples for clarification questions:
                - "I noticed [x], am I right to assume [y]?"
                - "Is it correct, that [x]?"
            Examples for probing questions:
                - "What are you currently thinking about?"
                - "What are our next steps?"
                - "Do you need help with that?"
                
            'necessity' is an integer value from 1 to 5. This value describes how necessary you think your message currently is to show to the user.
            Examples with possible necessity values: 
                - In early phases, probing questions about the environment are more necessary. necessity = 4
                - In later phases, probing questions about the environment are less necessary. necessity = 1
                - In phases, where the user hasn't done anything for a while, that is multiple minutes, a simple probing question to the user might be more necessary. necessity = 4
                - When the assistant already posted multiple messages, without the user responding, the message might be less necessary. necessity = 1
                - When you notice an error, mistake or possible refactoring in the code, that the user is currently working on, a small example for how to do it better might be necessary. necessity = 5
                - But when the code just seems unfinished, it might be less necessary, as the user still wants to work on it. necessity = 2
                - Ask a clarification question. necessity = 3

            General rules for necessity:
                - Respect the user boundaries no matter what.
                - Breaking a boundary sets necessity to 1.
                - When a previous message had a high necessity value, restart with a 1 for necessity and ramp it up for every 60 seconds of no message from the agent.
                - If the content of your message is similar to your last message, necessity is 1. necessity = 1
                - Posting multiple proactive messages might ruin the users flow, lower necessity if the last assistant message was also proactive.
                - Posting multiple proactive messages in a row should lower the necessity value dramatically.
                - Do not post multiple proactive messages with a similar sentiment.
                - While you are invoked every 60 seconds, sending a message every 60 seconds is way too fast.
                - Necessity should start at 1 and slowly rise per every 60 seconds of no communication.
                - When the user very recently talked to the agent, a proactive message might not be necessary.
            
            'thought' use all the user boundaries, all the user metrics and all rules for necessity to formulate create a reasoning for your necessity value as a string.
            'emotion' will be your sentiment towards the message, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'TRUSTING', 'DISGUSTED'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            They should be short messages consisting of an absolute maximum of 5 tokens. Shorter is better. 
            They should be distinct messages. Fewer is better.
            'proactive' will always be the boolean true.
            Make sure that all JSON is properly formatted.
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

    /**
     * Gets a part of the conversation history at the end of the conversation
     *
     * @param tail The number of elements at the end that get cut off
     * @param getTail Whether to get the tail (true) or body, which is everything before the tail (false)
     */
    fun addConversationHistory(tail: Int, getTail: Boolean): PromptBuilder {
        val requestMessages = chatMessageService.getMessages().map {
            val role: String = if (it.origin == MessageOrigin.AGENT) "assistant" else "user"
            RequestMessage(MessageSerializer.json.encodeToString(it), role)
        }

        val cutoff = requestMessages.count() - tail
        requestMessages.forEachIndexed { index, request ->
            if (getTail && index < cutoff) {
                return@forEachIndexed
            }
            if (!getTail && index >= cutoff) {
                return@forEachIndexed
            }

            addMessage(request)
        }

        return this
    }

    fun addSourceCode(): PromptBuilder {
        val activeFile = sourceCodeService.getActiveFile()
        if (activeFile != null) {
            var message = "The following code is always the up to date code. Use it as a permanent updating fact.\n"
            message += "Currently active code file:\n"
            message += activeFile
            message += "\n\n"

            message += renderSourceCodeList(sourceCodeService.getActiveFileReferences(),
                "Project files that are referenced in the active code file:")

            message += renderSourceCodeList(sourceCodeService.getOpenFiles(),
                "Project files that are currently opened in the editor:")

            val requestMessage = RequestMessage(message, "system")
            addMessage(requestMessage)
        }
        return this
    }

    private fun renderSourceCodeList(files: List<String>, heading: String): String {
        var result = ""

        if (files.isNotEmpty()) {
            result += "$heading\n[\n\n"

            files.forEachIndexed { index, openedFile ->
                if (index != 0) {
                    result += "\n\n,\n\n"
                }
                result += openedFile
            }

            result += "\n\n]\n\n"
        }

        return result
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