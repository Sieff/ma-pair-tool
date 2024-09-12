package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.Serializable

@Serializable
data class AssistantMessageProperties(
    val origin: EnumProperty,
    val message: Property,
    val emotion: EnumProperty,
    val reactions: ArrayProperty,
    val proactive: Property,
    val necessity: Property,
    val thought: Property,
): Properties()
