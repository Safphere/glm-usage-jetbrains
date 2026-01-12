package com.safphere.glmusage

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.Box
import javax.swing.event.HyperlinkEvent

/**
 * GLM Usage 悬浮提示面板 - 混合布局
 * 样式与glm-usage-vscode保持一致 (Top Info + Custom Graph + Bottom Actions)
 */
class GLMHoverTooltipPanel(private val usageData: GLMUsageData) : JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = JBColor.background()
        border = JBUI.Borders.empty(8) // VS Code padding: 8px

        // 1. Top Content (Title, Stats, Quotas)
        val topHtml = buildTopHtml(usageData)
        add(createHtmlPane(topHtml))

        // 2. Trend Graph (if history exists)
        if (usageData.history.isNotEmpty()) {
            add(Box.createVerticalStrut(3))
            val graphHeader = "<div style='font-family: Consolas, Monaco, monospace; font-size: 11px; color: #8b949e; margin-bottom: 3px;'>调用趋势 (24H) &nbsp;&nbsp;&nbsp; ${getPeakInfo(usageData)}</div>"
            add(createHtmlPane(graphHeader))

            // Custom Graph Component
            val graphPanel = GLMTrendGraphPanel(usageData.history)
            graphPanel.alignmentX = JComponent.LEFT_ALIGNMENT
            add(graphPanel)
        }

        // 3. Bottom Content (Actions)
        add(Box.createVerticalStrut(1)) // Divider space
        add(createHtmlPane(buildBottomHtml()))
    }

    private fun getPeakInfo(data: GLMUsageData): String {
        val peak = data.history.maxByOrNull { it.calls } ?: return ""
        val peakTime = if (peak.time.contains(" ")) peak.time.split(" ")[1] else peak.time
        return "<span style='font-size:12px'>峰值: <b>${peak.calls}</b> ($peakTime)</span>"
    }

    private fun createHtmlPane(htmlContent: String): JEditorPane {
        val editorPane = JEditorPane("text/html", htmlContent).apply {
            isEditable = false
            isOpaque = false // Transparent to let panel background show
            background = JBColor.background()
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            alignmentX = JComponent.LEFT_ALIGNMENT
        }
        
        // Fix for standard JEditorPane font rendering to match IDE
        val font = com.intellij.util.ui.UIUtil.getLabelFont()
        val bodyRule = "body { font-family: ${font.family}, sans-serif; font-size: ${font.size}pt; }"
        (editorPane.editorKit as? javax.swing.text.html.HTMLEditorKit)?.styleSheet?.addRule(bodyRule)

        editorPane.addHyperlinkListener { event ->
            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                // Actions handled by parent or links if needed.
                // Commands are not directly executable here unless mapped.
                // VS Code uses command: links. We can just print log or handle if we want real interactivity.
                // Since this is a tooltip, interactivity is limited.
            }
        }
        return editorPane
    }

    private fun buildTopHtml(data: GLMUsageData): String {
        val dateStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date(data.timestamp))
        val mcpPercent = data.quotas.mcp.pct
        val t5hPercent = data.quotas.token5h.pct

        val mcpPercentInt = mcpPercent.toInt()
        val t5hPercentInt = t5hPercent.toInt()

        val mcpColor = if (mcpPercentInt > 80) "#da3633" else "#58a6ff"
        val t5hColor = if (t5hPercentInt > 80) "#da3633" else "#238636"

        return buildString {
            append("<html><head><style>body { margin: 0; padding: 0; }</style></head><body>")
            append("<div style='line-height: 1.4;'>")

            // Title
            append("<div style='font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #e1e4e8;'>")
            append("GLM 数据看板 &nbsp;<span style='color: #8b949e; font-size: 12px; font-weight: normal;'>")
            append(data.platform)
            append("</span></div>")

            // Update time
            append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 10px;'>")
            append("更新时间: $dateStr")
            append("</div>")

            // Divider
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 1px 0;'>")

            // Statistics table
            append("<table style='width: 100%; margin-bottom: 10px;'>")
            append("<tr>")
            append("<td style='color: #8b949e; font-size: 11px; padding-right: 20px;'>Token 总量</td>")
            append("<td style='color: #8b949e; font-size: 11px;'>今日调用 (24H)</td>")
            append("</tr>")
            append("<tr>")
            append("<td style='color: #d2a8ff; font-size: 15px; font-weight: bold; padding-right: 20px;'>")
            append(formatNumber(data.totals.tokens))
            append("</td>")
            append("<td style='color: #79c0ff; font-size: 15px; font-weight: bold;'>")
            append(formatNumber(data.totals.calls))
            append("</td>")
            append("</tr>")
            append("</table>")

            // Divider
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 1px 0;'>")

            // Quotas
            append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 5px;'>配额状态</div>")

            // MCP Quota
            append("<div style='margin-bottom: 8px;'>")
            append("<div style='color: #e1e4e8; font-size: 12px; margin-bottom: 2px;'>")
            append("MCP 额度 (月) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: $mcpColor; font-weight: bold;'>")
            append(String.format("%.2f%%", mcpPercent))
            append("</span></div>")

            val mcpFilled = (mcpPercentInt * 15 / 100).coerceIn(0, 15)
            val mcpBarFilled = "█".repeat(mcpFilled)
            val mcpBarEmpty = "░".repeat(15 - mcpFilled)
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 11px;'>")
            append("<span style='color: $mcpColor;'>$mcpBarFilled</span>")
            append("<span style='color: #484f58;'>$mcpBarEmpty</span>")
            append(" &nbsp; ${data.quotas.mcp.used.toInt()}/${data.quotas.mcp.total.toInt()}")
            append("</div></div>")

            // Token 5h Quota
            append("<div style='margin-bottom: 8px;'>")
            append("<div style='color: #e1e4e8; font-size: 12px; margin-bottom: 2px;'>")
            append("Token 限流 (5小时) &nbsp;<span style='color: $t5hColor; font-weight: bold;'>")
            append(String.format("%.2f%%", t5hPercent))
            append("</span></div>")

            val t5hFilled = (t5hPercentInt * 15 / 100).coerceIn(0, 15)
            val t5hBarFilled = "█".repeat(t5hFilled)
            val t5hBarEmpty = "░".repeat(15 - t5hFilled)
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 11px;'>")
            append("<span style='color: $t5hColor;'>$t5hBarFilled</span>")
            append("<span style='color: #484f58;'>$t5hBarEmpty</span>")
            append(" &nbsp; ${formatTokens(data.quotas.token5h.used.toLong())}/${formatTokens(data.quotas.token5h.total.toLong())}")
            append("</div></div>")

            // Divider before Graph
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 1px 0;'>")

            append("</div></body></html>")
        }
    }

    private fun buildBottomHtml(): String {
        return buildString {
            append("<html><head><style>body { margin: 0; padding: 0; }</style></head><body>")
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 1px 0 4px 0;'>")
            append("<div style='color: #8b949e; font-size: 11px; font-family: Consolas, Monaco, monospace;'>")
            append("点击刷新数据 | 设置配置 Token")
            append("</div>")
            append("</body></html>")
        }
    }

    private fun formatNumber(num: Long): String {
        return num.toString().replace(Regex("(?<=\\d)(?=(\\d{3})+$)"), ",")
    }

    private fun formatTokens(num: Long): String {
        return if (num >= 1000000) String.format("%.2fM", num / 1000000.0)
        else if (num >= 1000) String.format("%.1fK", num / 1000.0)
        else num.toString()
    }
}

