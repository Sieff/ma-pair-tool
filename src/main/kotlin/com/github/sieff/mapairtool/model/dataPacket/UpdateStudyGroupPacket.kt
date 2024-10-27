package com.github.sieff.mapairtool.model.dataPacket

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudyGroupPacket(
    val studyGroup: Int,
    override val packetType: DataPacketType
): DataPacket()