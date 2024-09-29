package com.github.sieff.mapairtool.services.agent

import com.github.sieff.mapairtool.settings.AppSettingsState
import com.github.sieff.mapairtool.util.Logger
import com.github.sieff.mapairtool.util.observerPattern.observer.Observer
import com.intellij.openapi.project.Project

class AgentServiceContextImpl(val project: Project): AgentServiceContext, Observer<AppSettingsState.State> {
    private var agentService: AgentService? = null
    private val logger = Logger(this.javaClass)

    init {
        AppSettingsState.getInstance().state.subscribe(this)
    }

    override fun invokeAgent() {
        agentService?.invokeMainAgent()
    }

    override fun notify(data: AppSettingsState.State) {
        logger.info("Studygroup: ${data.studyGroup}")
        if (data.studyGroup == 1) {
            agentService = BaselineAgentServiceImpl(project)
        }
        if (data.studyGroup == 2) {
            agentService = CpsAgentServiceImpl(project)
        }
        if (data.studyGroup != 1 && data.studyGroup != 2) {
            agentService = null
        }
    }
}