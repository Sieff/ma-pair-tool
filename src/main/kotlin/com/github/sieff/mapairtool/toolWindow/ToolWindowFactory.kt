package com.github.sieff.mapairtool.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.sieff.mapairtool.Bundle
import com.github.sieff.mapairtool.services.inputHandler.InputHandlerService
import com.github.sieff.mapairtool.toolWindow.chatHistory.ChatHistory
import com.intellij.ui.components.JBTextField
import javax.swing.JButton


class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val inputService = toolWindow.project.service<InputHandlerService>()
        private val chatHistory = ChatHistory(toolWindow.project)

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val textField = JBTextField(Bundle.message("inputLabel"), 20)

            add(chatHistory)
            add(textField)
            add(JButton(Bundle.message("sendMessage")).apply {
                addActionListener {
                    inputService.handleInput(textField.getText())
                }
            })
        }
    }
}
