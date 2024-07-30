package com.github.sieff.mapairtool.ui.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import net.miginfocom.swing.MigLayout
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil
import org.intellij.plugins.markdown.ui.preview.jcef.MarkdownJCEFHtmlPanel


class ChatMessage(message: Message, project: Project): JBPanel<ChatMessage>() {
    init {
        val rightToLeft = if (message.origin == MessageOrigin.USER) ",rtl" else ""
        layout = MigLayout("inset 5$rightToLeft", "[]", "[top]")

        if (message.origin == MessageOrigin.USER) {
            border = JBUI.Borders.empty(0, 100, 0,0)
        }

        val file = LightVirtualFile("content.md", message.message)
        file.charset = Charsets.UTF_8

        val messageTextArea = MarkdownJCEFHtmlPanel(project, file)
        val rawHtmlContent = runReadAction {
            MarkdownUtil.generateMarkdownHtml(file, message.message, project)
        }
        val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                        <p>Markdown Previewäöü</p>
                        $rawHtmlContent
                    </html>
                """.trimIndent()

        println(htmlContent)
        messageTextArea.setHtml(htmlContent, 0)
        messageTextArea.setProperty("Content-Type", "text/html; charset=UTF-8")

        messageTextArea.jbCefClient.cefClient.removeRequestHandler();

        //messageTextArea.component.border = JBUI.Borders.empty(5, 10)
        //messageTextArea.component.background = if (message.origin == MessageOrigin.AGENT) Colors.AGENT.color else Colors.USER.color
        //messageTextArea.component.isOpaque = message.origin == MessageOrigin.USER


        add(messageTextArea.component)
    }

}