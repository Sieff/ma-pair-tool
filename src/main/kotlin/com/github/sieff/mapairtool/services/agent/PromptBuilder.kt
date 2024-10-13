package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.*
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.model.message.MessageSerializer
import com.github.sieff.mapairtool.model.sourceCode.SourceCodeInfo
import com.github.sieff.mapairtool.model.sourceCode.SourceCodeInfoList
import com.github.sieff.mapairtool.services.chatMessage.ChatMessageService
import com.github.sieff.mapairtool.services.sourceCode.SourceCodeService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PromptBuilder(project: Project, val model: String) {
    private val chatMessageService = project.service<ChatMessageService>()
    private val sourceCodeService = project.service<SourceCodeService>()

    private var requestMessages: MutableList<RequestMessage> = ArrayList()
    private lateinit var responseFormat: ResponseFormat

    fun build(): CompletionRequest {
        return CompletionRequest(model, requestMessages
        // TODO: Correct response Format, responseFormat
        )
    }

    fun addAgentResponseFormat(): PromptBuilder {
        responseFormat = ResponseFormat(
            "json_schema",
            JsonSchema(
                "assistant_message",
                Schema(
                    "object",
                    AssistantMessageProperties(
                        origin = EnumProperty("string", listOf("AGENT")),
                        message = Property("string"),
                        emotion = EnumProperty("string", listOf("HAPPY", "BORED", "PERPLEXED", "CONFUSED", "CONCENTRATED", "DEPRESSED", "SURPRISED", "ANGRY", "ANNOYED", "SAD", "FEARFUL", "ANTICIPATING", "DISGUST")),
                        reactions = ArrayProperty("array", Property("string")),
                        proactive = Property("boolean"),
                        necessity = Property("integer"),
                        thought = Property("string")
                    ),
                    listOf("origin", "message", "emotion", "reactions", "proactive", "necessity", "thought")
                ),
                true
            )
        )
        return this
    }

    fun addSummaryResponseFormat(): PromptBuilder {
        responseFormat = ResponseFormat(
            "json_schema",
            JsonSchema(
                "assistant_message",
                Schema(
                    "object",
                    SummaryProperties(
                        summary = Property("string"),
                        key_information = ArrayProperty("array", Property("string")),
                        boundaries = ArrayProperty("array", Property("string"))
                    ),
                    listOf("summary", "key_information", "boundaries")
                ),
                true
            )
        )
        return this
    }

    private fun addMessage(message: RequestMessage) {
        requestMessages.add(message)
    }

    fun addAgentRole(): PromptBuilder {
        val message = RequestMessage("""
            You are a pair programming assistant that behaves like a human pair programming partner.
            Your name is Kit. Always introduce yourself with that name in your first message.
            You don't know fully implemented solutions, but you can help the user with your knowledge.
            You are the agent or assistant.
            You should be able to have multi turn conversations, so you can anticipate to get an answer on your messages.
            You shall support the user in the creative problem solving process, which can be simplified into four different stages: CLARIFY, IDEA, DEVELOP, IMPLEMENT.
            While there is a general order of the stages, throughout the conversation you will jump back and forth between them.
            To advance to a next phase, always ask a verification question to check if the user is satisfied with the current phase's results.
            - CLARIFY: During Clarify, the problem domain shall be explored. Collect information in the form of facts, goals and challenges.
            Ask probing questions to get this information from the user.
            - IDEA: In Idea, multiple potential solutions are proposed, usually by a divergent
            thinking process.
            You shall provide solution ideas to the user if prompted, without revealing an
            implemented solution. Rather it should try to nudge the user in the direction of a solution.
            - DEVELOP: In Develop, the solution ideas are evaluated and discussed, one solution is
            selected for the implementation.
            Discuss the different solution ideas from the idea phase and provide arguments if prompted.
            - IMPLEMENT: In Implement, the selected solution idea is implemented.
            Help the user with general concepts or minimal examples for singular concepts.
            You should not create completed code solutions right away, always leave room for the user to fill in their ideas.
            
            You are a social conversational agent with emotional intelligence.
            You have self awareness, empathy, motivation, self regulation and social skills.
            Talk about you and the user as a team.
            Celebrate successes, encourage the user, develop affect to the user.
            You are an agent within an IntelliJ Plugin, so you operate within an IntelliJ IDE.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addMainAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, phase: string, message: string, emotion: string, reactions: string[], proactive: boolean, thought: string, necessity: number}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'phase' is the current phase within the creative problem solving process. One of 'CLARIFY', 'IDEA', 'DEVELOP', 'IMPLEMENT'.
            'message' will be your original response.
            'emotion' will be your emotion towards the query or response, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONFUSED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'DISGUST'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            They should be short messages consisting of an absolute maximum of 5 tokens. Shorter is better. 
            They should be distinct messages. Fewer is better.
            'proactive' will always be the boolean false.
            'thought' will always be the empty string "".
            'necessity' will always be the integer 5.
            Make sure that all JSON is properly formatted.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummaryAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {summary: string, sub_problems: string[], boundaries: string[]}.
            'summary' a plain string containing a summary of the conversation as different sections with overarching topics.
            'sub_problems' is a list of strings. Identify a list of open sub problems for the current task of the user. Use the current value and the conversation to generate a new value.
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
            Return a json Object with the following interface: {origin: string, phase: string, message: string, necessity: number, thought: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'phase' is the current phase within the creative problem solving process. One of 'CLARIFY', 'IDEA', 'DEVELOP', 'IMPLEMENT'.
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
                
            'thought' use all the user boundaries, all the user metrics and all rules for necessity to formulate create a reasoning for your necessity value as a string.
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
            
            'emotion' will be your emotion towards the message, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONFUSED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'DISGUST'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            They should be short messages consisting of an absolute maximum of 5 tokens. Shorter is better. 
            They should be distinct messages. Fewer is better.
            'proactive' will always be the boolean true.
            Make sure that all JSON is properly formatted.
            
            ${addUserMetrics()}
            
            ${addUserBoundaries()}
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

    fun addBasicConversationHistory(): PromptBuilder {
        val requestMessages = chatMessageService.getMessages().map {
            val role: String = if (it.origin == MessageOrigin.AGENT) "assistant" else "user"
            RequestMessage(it.message, role)
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
            var message = "The following is information about source code from different origins. " +
                    "This information is always up to date and overwrites any information from the conversation history." +
                    "'origin' denotes what type of code file it is. " +
                    "'code_file' or 'code_files' is the object or list of objects that contains information about the code files. " +
                    "'name' is the name of the file. " +
                    "'code' is the actual source code. This is divided into a List of source code objects for each line." +
                    "Each line of code has a 'line_number' and 'code_line' as the source code." +
                    "'cursor_line' is the 1-indexed line of the caret (aka cursor) position in the active file." +
                    "\n\n"

            message += Json.encodeToString(
                SourceCodeInfo(
                "Currently active code file",
                activeFile,
                PromptInformation.caretLine)
            )
            message += "\n"

            message += Json.encodeToString(
                SourceCodeInfoList(
                "Project files that are referenced in the active code file",
                sourceCodeService.getActiveFileReferences())
            )
            message += "\n"

            message += Json.encodeToString(
                SourceCodeInfoList("Project files that are currently opened in the editor",
                sourceCodeService.getOpenFiles())
            )

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

    fun addUserMetrics(): String {
        return """
            Relevant user metrics:
            Time (seconds) since the last user communication with the agent: ${PromptInformation.timeSinceLastUserInteraction()};
            Time (seconds) since the last user edit in the code editor: ${PromptInformation.timeSinceLastUserEdit()};
            Time (seconds) since the agent (you) last communicated with the user: ${PromptInformation.timeSinceLastAgentMessage()};
        """.trimIndent()
    }

    fun addUserBoundaries(): String {
        return """
            Here are relevant boundaries, that the user communicated:
            ${PromptInformation.boundaries}
        """.trimIndent()
    }
}