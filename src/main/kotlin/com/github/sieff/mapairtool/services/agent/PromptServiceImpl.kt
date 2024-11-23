package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.message.AssistantMessage
import com.github.sieff.mapairtool.model.message.BaseMessage
import com.intellij.openapi.project.Project

class PromptServiceImpl(val project: Project): PromptService {
    override fun getCPSAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
            .addAgentResponseFormat()
            .addAgentRole()
            .addMainAgentTask()
            .addKeyInformation()
            .addSummary()
            .addConversationHistory(1, false)
            .addSourceCode()
            .addConversationHistory(1, true)
            .addMainAgentTask()
            .build()
    }

    override fun getBaselineAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
            .addSourceCode()
            .addBasicConversationHistory()
            .build()
    }

    override fun getSummaryAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
            .addSummaryResponseFormat()
            .addAgentRole()
            .addSummaryAgentTask()
            .addKeyInformation()
            .addSummary()
            .addConversationHistory()
            .addSummaryAgentTask()
            .build()
    }

    override fun getProactiveAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
            .addAgentResponseFormat()
            .addAgentRole()
            .addProactiveAgentTask()
            .addKeyInformation()
            .addSummary()
            .addConversationHistory(1, false)
            .addSourceCode()
            .addConversationHistory(1, true)
            .addProactiveAgentTask()
            .build()
    }

    override fun getSimilarityPrompt(
        model: String,
        firstMessage: BaseMessage,
        secondMessage: BaseMessage
    ): CompletionRequest {
        return PromptBuilder(project, model)
            .addSimilarityTask(firstMessage.message, secondMessage.message)
            .build()
    }

    override fun getRelevancePrompt(
        model: String,
        message: AssistantMessage
    ): CompletionRequest {
        return PromptBuilder(project, model)
            .addAgentRole()
            .addRelevanceTask(message)
            .addKeyInformation()
            .addUserBoundaries()
            .addUserMetrics()
            .addSummary()
            .addConversationHistory(1, false)
            .addSourceCode()
            .addConversationHistory(1, true)
            .addRelevanceTask(message)
            .build()
    }

    override fun dispose() {
    }
}