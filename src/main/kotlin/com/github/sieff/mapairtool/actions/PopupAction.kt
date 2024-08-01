package com.github.sieff.mapairtool.actions

import com.github.sieff.mapairtool.ui.popup.PopupComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.awt.RelativePoint
import java.awt.Component
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent


class PopupAction : AnAction() {
    private lateinit var popupComponent: PopupComponent
    private var popup: JBPopup? = null

    override fun actionPerformed(e: AnActionEvent) {
        if (popup == null || !popup!!.isVisible) {
            popupComponent = PopupComponent(e.project!!)

            popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupComponent.panel, popupComponent.preferredFocusedComponent)
                .setResizable(false)
                .setMovable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .createPopup()

            addMovableSupport(popup!!, popupComponent.panel)
            showPopupAtBottomCenter(popup!!)
            Disposer.register(popup!!, popupComponent)
        }
    }

    private fun addMovableSupport(popup: JBPopup, component: Component) {
        val moveListener = object : MouseAdapter() {
            private var mouseDownScreenCoords: Point? = null
            private var mouseDownPopupCoords: Point? = null

            override fun mousePressed(e: MouseEvent) {
                mouseDownScreenCoords = e.locationOnScreen
                mouseDownPopupCoords = popup.locationOnScreen
            }

            override fun mouseDragged(e: MouseEvent) {
                if (mouseDownScreenCoords != null && mouseDownPopupCoords != null) {
                    val mouseDraggedScreenCoords = e.locationOnScreen
                    val newX = (mouseDownPopupCoords!!.x - mouseDownScreenCoords!!.x) + mouseDraggedScreenCoords.x
                    val newY = (mouseDownPopupCoords!!.y - mouseDownScreenCoords!!.y) + mouseDraggedScreenCoords.y
                    popup.setLocation(Point(newX, newY))
                }
            }
        }
        component.addMouseListener(moveListener)
        component.addMouseMotionListener(moveListener)
    }

    private fun showPopupAtBottomCenter(popup: JBPopup) {
        val screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
        val popupSize = popup.content.size
        val x = screenBounds.x + (screenBounds.width - popupSize.width) / 2
        val y = screenBounds.y + screenBounds.height - popupSize.height - 100

        popup.show(RelativePoint(Point(x, y)))
    }

    companion object {
        fun  getId(): String {
            return PopupAction::class.java.name
        }
    }
}
