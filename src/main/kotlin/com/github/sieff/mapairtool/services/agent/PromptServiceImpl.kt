package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
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