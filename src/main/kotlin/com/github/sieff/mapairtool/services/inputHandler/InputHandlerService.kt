package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.message.Message

interface InputHandlerService {
    fun handleInput(input: Message)
    fun handleWidgetInput(input: Message)
}