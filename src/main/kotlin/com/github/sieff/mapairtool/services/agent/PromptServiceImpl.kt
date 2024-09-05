package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.intellij.openapi.project.Project

class PromptServiceImpl(val project: Project): PromptService {
    override fun getMainAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
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

    override fun getSummaryAgentPrompt(model: String): CompletionRequest {
        return PromptBuilder(project, model)
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
            .addAgentRole()
            .addProactiveAgentTask()
            .addUserBoundaries()
            .addUserMetrics()
            .addSummary()
            .addConversationHistory(1, false)
            .addSourceCode()
            .addConversationHistory(1, true)
            .addProactiveAgentTask()
            .build()
    }

    override fun dispose() {
    }
}