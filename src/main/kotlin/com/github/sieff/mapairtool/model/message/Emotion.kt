package com.github.sieff.mapairtool.model.message

import kotlinx.serialization.*

@Serializable
enum class Emotion {
    @SerialName("HAPPY") HAPPY,
    @SerialName("BORED") BORED,
    @SerialName("PERPLEXED") PERPLEXED,
    @SerialName("CONFUSED") CONFUSED,
    @SerialName("CONCENTRATED") CONCENTRATED,
    @SerialName("DEPRESSED") DEPRESSED,
    @SerialName("SURPRISED") SURPRISED,
    @SerialName("ANGRY") ANGRY,
    @SerialName("ANNOYED") ANNOYED,
    @SerialName("SAD") SAD,
    @SerialName("FEARFUL") FEARFUL,
    @SerialName("ANTICIPATING") ANTICIPATING,
    @SerialName("DISGUST") DISGUST,
    @SerialName("JOY") JOY
}
