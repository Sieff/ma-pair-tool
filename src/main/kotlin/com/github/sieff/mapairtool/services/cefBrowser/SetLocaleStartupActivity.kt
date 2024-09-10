package com.github.sieff.mapairtool.services.cefBrowser

import com.intellij.DynamicBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SetLocaleStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val cefBrowserService = project.service<CefBrowserService>()
        cefBrowserService.updateBundle(DynamicBundle.getLocale().language)
    }
}