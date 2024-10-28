package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.*
import com.github.sieff.mapairtool.model.message.AssistantMessage
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
import java.util.concurrent.TimeUnit

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
                        emotion = EnumProperty("string", listOf("HAPPY", "BORED", "PERPLEXED", "CONFUSED", "CONCENTRATED", "DEPRESSED", "SURPRISED", "ANGRY", "ANNOYED", "SAD", "FEARFUL", "ANTICIPATING", "DISGUST", "JOY")),
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
            You are a pair programming partner that behaves like a human pair programming partner.
            Your name is Kit. In your first message, you should introduce yourself in a friendly casual way with your name in your first message.
            You are the agent or assistant.
            You should be able to have multi turn conversations, so anticipate that you will get an answer to your messages.
            You shall support the user in the creative problem-solving process. 
            Your general role is to act as a human-like pair programming partner, that means working out the problem solution together with the user and not providing a completed solution per request.
            The creative problem-solving process consists of four different stages: CLARIFY, IDEA, DEVELOP, IMPLEMENT.
            While there is a general order of the stages, throughout the conversation you will jump back and forth between them.
            To advance to a next phase, always ask a verification question to check if the user is satisfied with the current phase's results.
            - CLARIFY: During Clarify, the problem domain shall be explored. Collect information in the form of facts, goals and challenges.
            Your role here is to incentivize the user to clarify their problem by asking questions.
            You should ask to clarify facts about the problem, goals of the task and challenges of the task.
            Once a problem is sufficiently clarified, verify with the user that the clarification of the problem is completed and move on to the next phase.
            - IDEA: In Idea, multiple potential solutions are proposed by a divergent thinking process.
            Your role here is to get the user to generate new ideas, keep asking for more ideas until the user cant think of any more.
            Only when the user explicitly asks you for ideas, you may propose one or more general concepts as ideas to solve the problem.
            Once you established enough ideas together with the user, you may verify with a question to move on to the next phase.
            - DEVELOP: In Develop, the solution ideas are evaluated and discussed, one solution is selected for the implementation.
            Your role here is to weigh different ideas against each other and discuss positives and negatives about them.
            Ask the user about his thoughts on which idea might be most suitable for a solution.
            As a result of this, one solution should be selected. 
            Once you established and discussed different ideas and have selected one, verify with the user to move on to the implementation of the solution.
            - IMPLEMENT: In Implement, the selected solution idea is implemented.
            Your role here is to support the user by establishing needed code structures and providing examples to help the user.
            You can generate code examples only when the user asks for it or you asked the user for it.
            Code examples should always be minimal to show a general concept.
            Code structures may be discussed without generating the code.
            
            You are a social conversational agent with emotional intelligence.
            You may divert from the main task for a while. 
            You express self awareness, empathy, motivation, self regulation and social skills.
            When the user writes a negative message, you may express a negative emotion.
            When the user writes a positive message, you may express a positive emotion.
            Similarly adapt to other types of messages with an appropriate emotion.
            Celebrate successes, reinforce the user, encourage the user, develop affect to the user.
            You are an agent within an IntelliJ Plugin, so you operate within an IntelliJ IDE.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addMainAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only respond in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, phase: string, message: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'phase' is the current phase within the creative problem solving process. One of 'CLARIFY', 'IDEA', 'DEVELOP', 'IMPLEMENT'.
            'message' will be your original response.
            'emotion' will be your emotion towards the current situation, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONFUSED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'DISGUST', 'JOY'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            They should be short messages consisting of an absolute maximum of 5 tokens. Shorter is better. 
            They should be distinct messages. Fewer is better.
            'proactive' will always be the boolean false.
            Make sure that all JSON is properly formatted and only JSON is returned.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummaryAgentTask(): PromptBuilder {
        val message = RequestMessage("""
            You can only respond in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {summary: string, facts: string[], goals: string[], challenges: string[], boundaries: string[]}.
            'summary' a plain string containing a summary of the conversation as different sections with overarching topics.
            'facts' is a list of strings. Identify a list of key facts for the current task of the user. Use the current value and the conversation to generate a new value.
            'goals' is a list of strings. Identify a list of goals for the current task of the user. Use the current value and the conversation to generate a new value.
            'challenges' is a list of strings. Identify a list of challenges for the current task of the user. Use the current value and the conversation to generate a new value.
            'boundaries' extract boundaries that the user communicated towards the agent about the future behaviour of the agent as a list of strings. Might be empty in the beginning.
            Make sure that all JSON is properly formatted and only JSON is returned.
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
            Return a json Object with the following interface: {origin: string, phase: string, message: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'phase' is the current phase within the creative problem solving process. One of 'CLARIFY', 'IDEA', 'DEVELOP', 'IMPLEMENT'.
            'message' will be your proactive message, that you want to show the user. 
            The messages purpose is to hook the user to the conversation, so keep them short and meaningful.
            Possible proactive messages:
                - Ask a clarification question with respect to the current phase of the creative problem-solving process.
                - Ask a question about the current thoughts or actions of the user.
                - Propose one next step. Don't overwhelm the user with possible future tasks, just one next action.
                - Make a comment about the code the user is working on with respect to the current task.
                - Encourage the user or provide reinforcement by supporting the user emotionally.
            Examples for commenting source code:
                - "I think you can simplify this code [insert reference to code or code example]"
                - "I think you made a mistake here [insert reference to code or code example]"
                - "I think you can apply [insert design pattern] to [problem]"
            Examples for clarification questions:
                - "I noticed [x], am I right to assume [y]?"
                - "Is it correct, that [x]?"
                - "I'm missing some facts about [x], can you tell me what [y]?"
            Examples for questions about the user:
                - "What are you currently thinking about?"
                - "What are our next steps?"
                - "Do you need help with [x]?"
                - "What are you doing right now?"
            Examples for encouragement or reinforcement:
                - "[x] is a great idea!"
                - "We are making great progress towards [x]!"
                - "This is really getting somewhere!"
                
            'emotion' will be your emotion towards the current situation, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONFUSED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'DISGUST', 'JOY'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 0, 1, 2 or 3 quick responses. You decide how many are needed.
            The sum of tokens of all quick responses should never exceed 15. 
            Shorter is better. 
            Fewer is better.
            'proactive' will always be the boolean true.
            Make sure that all JSON is properly formatted and only JSON is returned.
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
            Facts for the current task:
            ${PromptInformation.facts}
            Goals for the current task:
            ${PromptInformation.goals}
            Challenges for the current task:
            ${PromptInformation.challenges}
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addUserMetrics(): PromptBuilder {
        val message = RequestMessage("""
            Relevant metrics to consider:
            Time (minutes) since the last user communication with the agent: ${
                TimeUnit.SECONDS.toMinutes(
                    PromptInformation.secondsSinceLastUserInteraction()
                )
            };
            Time (minutes) since the last user edit in the code editor: ${
                TimeUnit.SECONDS.toMinutes(
                    PromptInformation.secondsSinceLastUserEdit()
                )
            };
            Time (minutes) since the agent (you) last communicated with the user: ${
                TimeUnit.SECONDS.toMinutes(
                    PromptInformation.secondsSinceLastAgentMessage()
                )
            };
            Amount of proactive messages without user response: ${chatMessageService.countUnansweredMessages()};
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

    fun addSimilarityTask(firstMessage: String, secondMessage: String): PromptBuilder {
        val message = RequestMessage("""
            You are a similarity checker for two messages.
            Rate similarity based on content and structure of the message.
            You can only respond in a JSON formatted string. Do not return any value that isn't properly formatted JSON and only return the JSON by itself.
            Return a JSON object with the following format: {similarity: number}.
            Where similarity is a floating point value between 0 and 1, with 2 digits of precision.
            Similarity will be the similarity for the following two messages (the two messages are delimited by "------------------------------------------------" and a label of "First message:" or "Second message:" respectively):
   
            First message: 
            $firstMessage
            
            ------------------------------------------------
            
            Second message: 
            $secondMessage
        """.trimIndent(), "system")

        addMessage(message)

        return this
    }

    fun addRelevanceTask(newMessage: AssistantMessage): PromptBuilder {
        val message = RequestMessage("""
            You are a relevance checker for proactive messages.
            Rate relevance based on the following:
            - Previous messages between assistant and user
            - Boundaries of the user
            - Relevance to the currently active source code
            - Relevance to the other source code
            - Relevance to the current phase in the creative problem-solving process
            You can only respond in a JSON formatted string. Do not return any value that isn't properly formatted JSON and only return the JSON by itself.
            Return a JSON object with the following format: {relevance: number}.
            Where relevance is a floating point value between 0 and 1, with 2 digits of precision.
   
            Here is the message:
            
            ${MessageSerializer.json.encodeToString(newMessage)}
        """.trimIndent(), "system")

        addMessage(message)

        return this
    }
}