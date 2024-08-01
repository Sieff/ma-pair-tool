package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletion(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val system_fingerprint: String,
    val choices: List<Choice>,
    val usage: Usage
)





