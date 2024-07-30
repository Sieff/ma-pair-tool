package com.github.sieff.mapairtool.ui.swing

import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.border.AbstractBorder

class RoundedBorder(private val radius: Int) : AbstractBorder() {

    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g as Graphics2D
        g2.stroke = BasicStroke(1.5f)
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
    }

    override fun getBorderInsets(c: Component?): Insets {
        return JBUI.insets(radius, radius, radius, radius)
    }
}
