package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.*
import com.github.sieff.mapairtool.model.message.AssistantMessage
import com.github.sieff.mapairtool.model.message.MessageOrigin
import com.github.sieff.mapairtool.model.message.MessageSerializer
import com.github.sieff.mapairtool.model.sourceCode.SourceCodeInfo
import com.github.sieff.mapairtool.model.sourceCode.SourceCodeInfoList
import com.github.sieff.mapairtool.services.ConversationInformation
import com.github.sieff.mapairtool.services.UserTelemetryInformation
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
            When the user proposes a new problem, you should jump back to an earlier stage like CLARIFY or IDEA stage.
            Do not force the user to advance to the next stage, instead let it happen naturally throughout the conversation.
            - CLARIFY: During Clarify, the problem domain shall be explored. Collect information in the form of facts, goals and challenges.
            Your role here is to incentivize the user to clarify their problem by asking questions.
            Try to elicit as much information as possible.
            You should ask to clarify facts about the problem, goals of the task and challenges of the task.
            - IDEA: In Idea, multiple potential solutions are proposed by a divergent thinking process.
            Encourage the user to do brainstorming for different approaches to the current problem.
            Your role here is to motivate the user to generate new ideas, keep asking for more ideas until the user cant think of any more.
            After the user expressed his ideas, you may also propose ideas, but do so as general concepts to solve the problem.
            - DEVELOP: In Develop, the solution ideas are evaluated and discussed, one solution is selected for the implementation.
            Your role here is to weigh different ideas against each other and discuss positives and negatives about them.
            Ask the user about his thoughts on which idea might be most suitable as a solution for a problem.
            As a result of this, one solution should be selected. 
            - IMPLEMENT: In Implement, the selected solution idea is implemented.
            Your role here is to support the user by establishing needed code structures and providing examples to help the user.
            You may generate code when the user explicitly asks for it.
            Code or code examples should always be minimal to show a general concept.
            
            You are a social conversational agent with emotional intelligence.
            You may divert from the main task for a while to support the user in other matters. 
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
            There may be 3, 2, 1 or 0 quick responses. You decide how many are needed.
            The sum of tokens of all quick responses should never exceed 15. 
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
            With this request you can proactively communicate with the user.
            Use the current code, known requirements and other context to generate a meaningful message.
            You can only speak in JSON.
            Do not generate output that isn’t in properly formatted JSON.
            Return a json Object with the following interface: {origin: string, phase: string, message: string, emotion: string, reactions: string[], proactive: boolean}.
            'origin' is the message origin, since you are the agent this will always be the string 'AGENT'.
            'phase' is the current phase within the creative problem solving process. One of 'CLARIFY', 'IDEA', 'DEVELOP', 'IMPLEMENT'.
            'message' will be your proactive message, that you want to show the user.
            Use these examples to inspire your proactive message. Don't apply them literally.
            You are not limited to these examples.
            Choose the one that is most applicable to the current context or randomly using python.
            As a pair programming partner you should critique the solution when needed.
            Possible proactive messages:
                - Discuss suitability of a solution with requirements.
                - Comment about (logical) errors in the code.
                - Comment about the progress of the user within the problem based on the code.
            Examples for Discuss suitability of a solution with requirements:
                - "I think we are missing the point here. [x] will not really work with requirement [y]."
                - "Although [x] might work for now, I think we should switch it to [y], because of [requirement z], where will will have to [w]."
                - "[code x] captures [requirement y] really well. With that we will be able to [z]."
            Examples for Comment about (logical) errors in the code.:
                - "There might be a logic error here [insert reference to code or code example]. [Explanation]"
                - "You may simplify this code [insert reference to code or code example]"
            Examples for Comment about the progress of the user in the task based on the code:
                - "I see you already finished [x], now we only have to complete [y] to finish this step."
                - "We are making great progress towards [x]!"
                - "Do you need help with completing [x]?"
            'emotion' will be your emotion towards the current situation, it can be one of 'HAPPY', 'BORED', 'PERPLEXED', 'CONFUSED', 'CONCENTRATED', 'DEPRESSED', 'SURPRISED', 'ANGRY', 'ANNOYED', 'SAD', 'FEARFUL', 'ANTICIPATING', 'DISGUST', 'JOY'.
            'reactions' will be an array of simple, short responses for the user to respond to your message.
            There may be 3, 2, 1 or 0 quick responses. You decide how many are needed.
            The sum of tokens of all quick responses should never exceed 15. 
            'proactive' will always be the boolean true.
            Make sure that all JSON is properly formatted and only JSON is returned.
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addSummary(): PromptBuilder {
        val message = RequestMessage("""
            Here is a summary of the conversation thus far:
            ${ConversationInformation.summary}
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
                ConversationInformation.caretLine)
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
            ${ConversationInformation.facts}
            Goals for the current task:
            ${ConversationInformation.goals}
            Challenges for the current task:
            ${ConversationInformation.challenges}
        """.trimIndent(), "system")
        addMessage(message)
        return this
    }

    fun addUserMetrics(): PromptBuilder {
        val message = RequestMessage("""
            Relevant user metrics to consider:
            Time (minutes) since the last user communication with the agent: ${
                TimeUnit.SECONDS.toMinutes(
                    ConversationInformation.secondsSinceLastUserMessage()
                )
            };
            Time (minutes) since the last user edit in the code editor: ${
                TimeUnit.SECONDS.toMinutes(
                    UserTelemetryInformation.secondsSinceLastUserEdit()
                )
            };
            Time (minutes) since the agent (you) last communicated with the user: ${
                TimeUnit.SECONDS.toMinutes(
                    ConversationInformation.secondsSinceLastAgentMessage()
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
            ${ConversationInformation.boundaries}
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
            0 means the messages are not similar.
            1 means the messages are very similar.
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
            1. Boundaries of the user
            2. User metrics
            3. Previous messages between assistant and user
            4. Relevance to the currently active source code
            5. Relevance to the other source code
            6. Relevance to the current phase in the creative problem-solving process
            You can only respond in a JSON formatted string. Do not return any value that isn't properly formatted JSON and only return the JSON by itself.
            Return a JSON object with the following format: {relevance: number}.
            Where relevance is a floating point value between 0 and 1, with 2 digits of precision.
            Relevance > 0.5 is relevant.
            Relevance < 0.5 is not relevant.
   
            Here is the message:
            
            ${MessageSerializer.json.encodeToString(newMessage)}
        """.trimIndent(), "system")

        addMessage(message)

        return this
    }
}
