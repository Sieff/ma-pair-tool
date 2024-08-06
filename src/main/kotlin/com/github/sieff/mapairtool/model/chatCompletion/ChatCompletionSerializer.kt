package com.github.sieff.mapairtool.model.chatCompletion

import kotlinx.serialization.json.Json

object ChatCompletionSerializer {
    val json: Json = Json {
        ignoreUnknownKeys = true
    }
}