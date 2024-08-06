package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.model.message.BaseMessage

interface AgentService {
    fun askTheAssistant(history: List<BaseMessage>)
}