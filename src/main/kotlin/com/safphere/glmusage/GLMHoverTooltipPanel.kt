package com.safphere.glmusage

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

/**
 * GLM Usage 悬浮提示面板 - 使用HTML显示
 * 样式与glm-usage-vscode保持一致
 */
class GLMHoverTooltipPanel(private val usageData: GLMUsageData) : JPanel() {

    init {
        layout = BorderLayout()
        background = JBColor.background()
        border = JBUI.Borders.empty()
        
        val editorPane = createHtmlPane()
        add(editorPane, BorderLayout.CENTER)
    }

    private fun createHtmlPane(): JComponent {
        val htmlContent = buildTooltipHtml(usageData)
        
        val editorPane = JEditorPane("text/html", htmlContent).apply {
            isEditable = false
            isOpaque = true
            background = JBColor.background()
            // Let the content determine the size - adaptive sizing
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        }
        
        // Make links clickable if needed
        editorPane.addHyperlinkListener { event ->
            if (event.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                // Handle link clicks if needed
            }
        }
        
        return editorPane
    }

    private fun buildTooltipHtml(data: GLMUsageData): String {
        val dateStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date(data.timestamp))
        val mcpPercent = data.quotas.mcp.pct.toInt()
        val t5hPercent = data.quotas.token5h.pct.toInt()

        // Determine colors based on percentage (matching VSCode exactly)
        val mcpColor = if (mcpPercent > 80) "#da3633" else "#58a6ff"
        val t5hColor = if (t5hPercent > 80) "#da3633" else "#238636"

        // Build HTML tooltip exactly like VSCode MarkdownString
        return buildString {
            append("<html><body>")
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 12px; line-height: 1.4; padding: 8px;'>")

            // Title - exactly like VSCode
            append("<div style='font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #e1e4e8;'>")
            append("GLM 数据看板 &nbsp;<span style='color: #8b949e; font-size: 12px; font-weight: normal;'>")
            append(data.platform)
            append("</span></div>")

            // Update time
            append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 10px;'>")
            append("更新时间: $dateStr")
            append("</div>")

            // Divider
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

            // Statistics table - exactly like VSCode
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
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

            // Quotas
            append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 5px;'>配额状态</div>")

            // MCP Quota - exactly like VSCode
            append("<div style='margin-bottom: 8px;'>")
            append("<div style='color: #e1e4e8; font-size: 12px; margin-bottom: 2px;'>")
            append("MCP 额度 (月) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='color: $mcpColor; font-weight: bold;'>")
            append("${mcpPercent}%")
            append("</span></div>")

            val mcpFilled = (mcpPercent * 15 / 100).coerceIn(0, 15)
            val mcpBarFilled = "█".repeat(mcpFilled)
            val mcpBarEmpty = "░".repeat(15 - mcpFilled)
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 11px;'>")
            append("<span style='color: $mcpColor;'>")
            append(mcpBarFilled)
            append("</span>")
            append("<span style='color: #484f58;'>")
            append(mcpBarEmpty)
            append("</span>")
            append(" &nbsp; ${data.quotas.mcp.used.toInt()}/${data.quotas.mcp.total.toInt()}")
            append("</div></div>")

            // Token 5h Quota - exactly like VSCode
            append("<div style='margin-bottom: 8px;'>")
            append("<div style='color: #e1e4e8; font-size: 12px; margin-bottom: 2px;'>")
            append("Token 限流 (5小时) &nbsp;<span style='color: $t5hColor; font-weight: bold;'>")
            append("${t5hPercent}%")
            append("</span></div>")

            val t5hFilled = (t5hPercent * 15 / 100).coerceIn(0, 15)
            val t5hBarFilled = "█".repeat(t5hFilled)
            val t5hBarEmpty = "░".repeat(15 - t5hFilled)
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 11px;'>")
            append("<span style='color: $t5hColor;'>")
            append(t5hBarFilled)
            append("</span>")
            append("<span style='color: #484f58;'>")
            append(t5hBarEmpty)
            append("</span>")
            append("</div></div>")

            // Divider
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

            // Traffic trend (sparkline) - exactly like VSCode
            if (data.history.isNotEmpty()) {
                append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 5px;'>调用趋势 (24H)</div>")

                val calls = data.history.map { it.calls }
                val max = calls.maxOrNull()?.coerceAtLeast(1) ?: 1
                val bars = listOf(" ", "▂", "▃", "▄", "▅", "▆", "▇", "█")

                val sparkline = buildString {
                    data.history.forEach { pt ->
                        if (pt.calls == 0) {
                            append("<span style='color: #30363d;'>_</span>")
                        } else {
                            val idx = ((pt.calls.toDouble() / max) * (bars.size - 1)).toInt().coerceIn(0, bars.size - 1)
                            append("<span style='color: #58a6ff;'>")
                            append(bars[idx])
                            append("</span>")
                        }
                    }
                }

                append("<div style='font-family: Consolas, Monaco, monospace; font-size: 11px; margin-bottom: 5px;'>")
                append(sparkline)
                append("</div>")

                // Peak info
                val peak = data.history.maxByOrNull { it.calls }
                if (peak != null) {
                    val peakTime = if (peak.time.contains(" ")) peak.time.split(" ")[1] else peak.time
                    append("<div style='color: #8b949e; font-size: 11px;'>")
                    append("峰值: <span style='color: #e1e4e8; font-weight: bold;'>")
                    append("${peak.calls}")
                    append("</span> 次 ($peakTime)")
                    append("</div>")
                }
            }

            // Divider
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

            // Actions
            append("<div style='color: #8b949e; font-size: 11px;'>")
            append("点击刷新数据 | 设置配置 Token")
            append("</div>")

            append("</div></body></html>")
        }
    }

    private fun formatNumber(num: Long): String {
        return num.toString().replace(Regex("(?<=\\d)(?=(\\d{3})+$)"), ",")
    }
}
