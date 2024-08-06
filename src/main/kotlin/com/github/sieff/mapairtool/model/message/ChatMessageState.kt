package com.github.sieff.mapairtool.model.message

data class ChatMessageState(
    val messages: List<BaseMessage>,
    val temporaryMessage: AssistantMessage?
)
