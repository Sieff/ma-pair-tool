package com.github.sieff.mapairtool.ui.toolWindow

import com.github.sieff.mapairtool.ui.toolWindow.chatHistory.ChatHistory
import com.github.sieff.mapairtool.ui.toolWindow.textInput.TextInput
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout


class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val chatHistory = ChatHistory(toolWindow.project)
        private val textInput = TextInput(toolWindow.project)

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            add(chatHistory, BorderLayout.CENTER)
            add(textInput, BorderLayout.SOUTH)
        }
    }
}
