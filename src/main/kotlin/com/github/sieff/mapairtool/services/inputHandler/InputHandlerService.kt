package com.github.sieff.mapairtool.services.inputHandler

import com.github.sieff.mapairtool.model.message.BaseMessage

interface InputHandlerService {
    fun handleInput(input: BaseMessage)
}