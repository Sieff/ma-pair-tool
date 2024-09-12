package com.github.sieff.mapairtool.model.completionRequest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PropertiesSerializer: KSerializer<Properties> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Properties")

    override fun serialize(encoder: Encoder, value: Properties) {
        when (value) {
            is AssistantMessageProperties -> encoder.encodeSerializableValue(AssistantMessageProperties.serializer(), value)
            is SummaryProperties -> encoder.encodeSerializableValue(SummaryProperties.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): Properties {
        throw NotImplementedError("Deserialization not implemented")
    }
}