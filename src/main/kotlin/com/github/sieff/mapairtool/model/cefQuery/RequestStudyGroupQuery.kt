package com.github.sieff.mapairtool.model.cefQuery

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RequestStudyGroupQuery")
data class RequestStudyGroupQuery(
    override val queryType: CefQueryType = CefQueryType.REQUEST_STUDY_GROUP
): CefQuery()
