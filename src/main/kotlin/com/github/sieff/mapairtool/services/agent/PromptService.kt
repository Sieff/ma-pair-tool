package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.completionRequest.CompletionRequest
import com.github.sieff.mapairtool.model.message.BaseMessage
import com.intellij.openapi.Disposable

interface PromptService: Disposable {
    fun getCPSAgentPrompt(model: String): CompletionRequest
    fun getBaselineAgentPrompt(model: String): CompletionRequest
    fun getSummaryAgentPrompt(model: String): CompletionRequest
    fun getProactiveAgentPrompt(model: String): CompletionRequest
    fun getSimilarityPrompt(model: String, firstMessage: BaseMessage, secondMessage: BaseMessage): CompletionRequest
}