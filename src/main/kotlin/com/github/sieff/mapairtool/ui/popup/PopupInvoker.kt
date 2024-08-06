package com.github.sieff.mapairtool.ui.popup

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager

object PopupInvoker {

    @JvmStatic
    fun invokePopup(project: Project) {
        // Retrieve the action by its ID
        val action = ActionManager.getInstance().getAction(PopupAction.getId())

        if (action != null) {
            // Create an AnActionEvent with the appropriate context
            val dataContext = DataManager.getInstance().getDataContext(WindowManager.getInstance().getFrame(project))
            val event = AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataContext)

            // Invoke the action
            action.actionPerformed(event)
        }
    }
}