package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest

interface PromptService {
    fun getMainAgentPrompt(model: String): CompletionRequest
    fun getSummaryAgentPrompt(model: String): CompletionRequest
    fun getProactiveAgentPrompt(model: String): CompletionRequest
}