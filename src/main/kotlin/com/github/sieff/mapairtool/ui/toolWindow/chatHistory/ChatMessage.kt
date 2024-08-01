package com.github.sieff.mapairtool.ui.toolWindow.chatHistory

import com.github.sieff.mapairtool.model.Message
import com.github.sieff.mapairtool.model.MessageOrigin
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBPanel
import com.intellij.ui.util.maximumHeight
import com.intellij.ui.util.minimumHeight
import com.intellij.util.ui.JBUI
import io.ktor.util.*
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil
import org.intellij.plugins.markdown.ui.preview.jcef.MarkdownJCEFHtmlPanel
import java.awt.Dimension
import javax.swing.BoxLayout
import kotlin.concurrent.thread


class ChatMessage(message: Message, project: Project): JBPanel<ChatMessage>() {
    init {
        val rightToLeft = if (message.origin == MessageOrigin.USER) ",rtl" else ""
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        if (message.origin == MessageOrigin.USER) {
            border = JBUI.Borders.empty(0, 100, 0,0)
        }

        val file = LightVirtualFile("content.md", message.message)

        val messageTextArea = MarkdownJCEFHtmlPanel(project, file)
        val html = runReadAction {
            MarkdownUtil.generateMarkdownHtml(file, message.message, project)
        }

        val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <script>
                    function sendValueToJava() {
                        window.external.sendValue(42);
                    }
                    </script>
                        $html
                    </html>
                """.trimIndent()

        messageTextArea.setHtml(html, 0)


        val msgRouter = CefMessageRouter.create()
        msgRouter.addHandler(object: CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser?,
                frame: CefFrame?,
                queryId: Long,
                request: String?,
                persistent: Boolean,
                callback: CefQueryCallback?
            ): Boolean {
                println(request);


                messageTextArea.component.maximumSize = Dimension(Int.MAX_VALUE, Integer.valueOf(request) + 32)
                messageTextArea.component.revalidate()
                messageTextArea.component.repaint()

                return true;
            }
        }, true)
        messageTextArea.cefBrowser.client.addMessageRouter(msgRouter)


        messageTextArea.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                println("onload")
                thread {
                    Thread.sleep(1000);
                    browser?.executeJavaScript("window.cefQuery({request: '' + document.body.clientHeight });", browser.url, 0);
                }
            }
        }, messageTextArea.cefBrowser)


        add(messageTextArea.component)
    }

}