package com.github.sieff.mapairtool.ui.swing

import com.intellij.ui.JBColor
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent


class DragHandleComponent() :
    JComponent() {
    private var dotColor: Color = JBColor.gray
    private var dotSize = 3
    private var componentSize = 9 * dotSize

    init {
        preferredSize = Dimension(componentSize, componentSize)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        g2d.color = Color(0, 0, 0, 0)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val roundedRectangle: RoundRectangle2D = RoundRectangle2D.Double(
            0.0, 0.0, componentSize.toDouble(), componentSize.toDouble(), componentSize / 5.0, componentSize / 5.0
        )
        g2d.fill(roundedRectangle)

        val dotRadius = dotSize.toFloat() / 2
        val halfComponent = componentSize.toFloat() / 2

        val dotPositions = arrayOf(
            intArrayOf((halfComponent - 3 * dotRadius).toInt(), (halfComponent - dotRadius).toInt()),
            intArrayOf((halfComponent - 3 * dotRadius).toInt(), (halfComponent - 5 * dotRadius).toInt()),
            intArrayOf((halfComponent - 3 * dotRadius).toInt(), (halfComponent + 3 * dotRadius).toInt()),
            intArrayOf((halfComponent + 2 * dotRadius).toInt(), (halfComponent - dotRadius).toInt()),
            intArrayOf((halfComponent + 2 * dotRadius).toInt(), (halfComponent - 5 * dotRadius).toInt()),
            intArrayOf((halfComponent + 2 * dotRadius).toInt(), (halfComponent + 3 * dotRadius).toInt())
        )

        g2d.color = dotColor
        for (pos in dotPositions) {
            g2d.fillOval(pos[0], pos[1], dotSize, dotSize)
        }
    }
}
