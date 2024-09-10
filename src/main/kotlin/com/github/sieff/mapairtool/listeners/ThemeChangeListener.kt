package com.github.sieff.mapairtool.listeners

import com.github.sieff.mapairtool.model.dataPacket.ColorScheme
import com.github.sieff.mapairtool.services.cefBrowser.CefBrowserService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.project.Project


class ThemeChangeListener(val project: Project) : EditorColorsListener {
    private fun isDarkTheme(): Boolean {
        return EditorColorsManager.getInstance().isDarkEditor
    }

    override fun globalSchemeChange(scheme: EditorColorsScheme?) {
        val cefBrowserService = project.service<CefBrowserService>()

        if (isDarkTheme()) {
            cefBrowserService.updateColorScheme(ColorScheme.DARK)
        } else {
            cefBrowserService.updateColorScheme(ColorScheme.LIGHT)
        }
    }
}