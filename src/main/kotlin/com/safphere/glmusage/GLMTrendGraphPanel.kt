package com.safphere.glmusage

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.roundToInt

class GLMTrendGraphPanel(private val history: List<HistoryPoint>) : JPanel() {

    init {
        // VS Code width: 24 points * (8 + 2.5) = 252px
        // VS Code height: 60px
        preferredSize = Dimension(260, 55)
        isOpaque = false
        border = JBUI.Borders.empty(2, 0)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // VS Code Parameters
        val barWidth = 8
        val gap = 2.5
        val totalPoints = 24
        // Fixed total width 240
        // We can center it if the panel is wider, but for now stick to left alignment or basic flow
        
        // 1. Prepare Data: Always 24 points. Pad left with 0 if needed.
        val points = history.toMutableList()
        if (points.size > totalPoints) {
             // Take last 24
             val p = points.subList(points.size - totalPoints, points.size)
             points.clear()
             points.addAll(p)
        }
        while (points.size < totalPoints) {
            points.add(0, HistoryPoint("", 0))
        }

        val maxCalls = points.maxOfOrNull { it.calls }?.coerceAtLeast(1) ?: 1

        val peakColor = Color.decode("#d2a8ff") // Peak color
        val normalColor = Color.decode("#58a6ff") // Normal color
        val labelColor = Color.decode("#8b949e")
        val font = Font("SansSerif", Font.PLAIN, 9)
        g2.font = font

        points.forEachIndexed { i, p ->
            // h = p.calls === 0 ? 2 : Math.max(Math.round((p.calls / maxCalls) * 40), 2);
            val h = if (p.calls == 0) 2 else max(((p.calls.toDouble() / maxCalls) * 38).roundToInt(), 2)
            val x = (i * (barWidth + gap)).toInt()
            val y = 42 - h // Base line at 42

            val isPeak = (p.calls == maxCalls && maxCalls > 0)
            g2.color = if (isPeak) peakColor else normalColor
            
            // Opacity handled by alpha if needed, but VS Code uses solid colors in SVG mostly or fill-opacity
            // VS Code: opacity 1.0 for peak, 0.8 for others.
            if (!isPeak) {
                // Apply 0.8 alpha
                g2.color = Color(normalColor.red, normalColor.green, normalColor.blue, (255 * 0.8).toInt())
            }

            // Draw rounded rect: rx="2"
            g2.fillRoundRect(x, y, barWidth, h, 4, 4) // arcWidth/Height approx 2*rx
        }

        // Draw Labels
        // 0, 12, 23
        g2.color = labelColor
        val yLabel = 52

        val drawLabel = { index: Int, align: String ->
            val p = points.getOrNull(index)
            if (p != null) {
                val t = p.time
                // Format: "09" from "09:00"
                val text = if (t.contains(":")) t.split(":")[0] else ""
                if (text.isNotEmpty()) {
                    val metrics = g2.fontMetrics
                    val w = metrics.stringWidth(text)
                    val xBase = (index * (barWidth + gap) + (barWidth / 2)).toInt()

                    val panelWidth = 260
                    val xText = when(align) {
                        "start" -> 0
                        "end" -> panelWidth - w - 2
                        else -> xBase - (w / 2)
                    }
                    g2.drawString(text, xText, yLabel)
                }
            }
        }

        drawLabel(0, "start")
        drawLabel(12, "middle")
        drawLabel(23, "end")
    }
}
