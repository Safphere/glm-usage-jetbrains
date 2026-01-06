package com.safphere.glmusage

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JLabel
import javax.swing.JComponent
import com.intellij.ui.JBColor
import java.awt.Cursor
import javax.swing.Timer
import java.awt.event.MouseEvent
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.awt.event.MouseAdapter
import javax.swing.SwingUtilities

class GLMStatusBarWidget(private val project: Project) : CustomStatusBarWidget {
    private val component = JLabel("GLM 初始化...")
    private var data: GLMUsageData? = null
    private var timer: Timer? = null
    private var isRefreshing = false
    private var popup: JBPopup? = null

    init {
        component.border = JBUI.Borders.empty(0, 4)
        component.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        startRefresh()

        // Add mouse listener for click to refresh
        component.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                // Click to force refresh
                if (e != null && e.button == MouseEvent.BUTTON1) {
                    updateData(force = true)
                }
            }
        })
        
        // Add mouse listener for hover tooltip
        setupHoverTooltip()
    }
    
    private fun setupHoverTooltip() {
        component.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                showTooltip()
            }
            
            override fun mouseExited(e: MouseEvent?) {
                hideTooltip()
            }
        })
    }
    
    private fun showTooltip() {
        val d = data ?: return
        if (popup != null && popup!!.isVisible) return
        
        val tooltipContent = createTooltipPanel(d)
        
        // Let the content determine its preferred size
        tooltipContent.preferredSize = null
        
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(tooltipContent, null)
            .setRequestFocus(false)
            .setResizable(false)
            .setMovable(false)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .setCancelOnWindowDeactivation(true)
            .createPopup()
        
        // Position the popup centered above the status bar
        val statusBarWidth = component.parent?.width ?: component.width
        val popupWidth = tooltipContent.preferredSize.width
        val popupHeight = tooltipContent.preferredSize.height
        
        // Center the popup above the status bar
        val xOffset = (statusBarWidth - popupWidth) / 2
        val location = Point(xOffset.coerceAtLeast(0), -popupHeight)
        popup!!.show(RelativePoint(component, location))
    }
    
    private fun hideTooltip() {
        popup?.cancel()
        popup = null
    }
    
    private fun createTooltipPanel(data: GLMUsageData): JComponent {
        return GLMHoverTooltipPanel(data)
    }

    private fun startRefresh() {
        // Initial fetch
        updateData()
        // Periodic fetch every 60 seconds (matching VSCode)
        timer = Timer(60000) { updateData() }
        timer?.start()
    }

    fun updateData(force: Boolean = false) {
        if (isRefreshing) return

        val settings = GLMSettingsState.instance

        // Check for API key in settings or environment variable
        val apiKey = if (settings.apiKey.isNotEmpty()) {
            settings.apiKey
        } else {
            System.getenv("ANTHROPIC_AUTH_TOKEN") ?: ""
        }

        if (apiKey.isEmpty()) {
            component.text = "⚙️ 配置 GLM"
            component.foreground = JBColor(0xFFA500, 0xFFA500) // Orange
            data = null
            return
        }

        if (force) {
            component.text = "🔄 刷新中..."
            component.foreground = UIUtil.getLabelForeground()
        }

        isRefreshing = true
        ApplicationManager.getApplication().executeOnPooledThread {
            val newData = GLMUsageService.fetchData()
            isRefreshing = false
            ApplicationManager.getApplication().invokeLater {
                if (newData != null) {
                    data = newData
                    updateUI()
                } else {
                    component.text = "❌ GLM 错误"
                    component.foreground = JBColor.RED
                }
            }
        }
    }

    private fun updateUI() {
        val d = data ?: return

        // Check if there's an error
        if (d.error != null) {
            component.text = "❌ ${d.error}"
            component.foreground = JBColor.RED
            return
        }

        val mcp = formatPct(d.quotas.mcp.pct)
        val t5h = formatPct(d.quotas.token5h.pct)

        // Formatted Text matching VS Code version exactly
        // VSCode: $(graph) GLM: ${mcp}%(MCP) ${t5h}%(5h) | ${calls}次 ${tokens}
        val txt = "📊 GLM: $mcp%(MCP) $t5h%(5h) | ${d.totals.calls}次 ${formatTokens(d.totals.tokens)}"

        component.text = txt

        // Warning color when over 80%
        if (d.quotas.mcp.pct > 80 || d.quotas.token5h.pct > 80) {
            component.foreground = JBColor.RED
        } else {
            component.foreground = UIUtil.getLabelForeground()
        }
    }

        private fun buildDashboardTooltip(data: GLMUsageData): String {
        val dateStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date(data.timestamp))
        val mcpPercent = data.quotas.mcp.pct.toInt()
        val t5hPercent = data.quotas.token5h.pct.toInt()

        // Determine colors based on percentage (matching VSCode)
        val mcpColor = if (mcpPercent > 80) "#da3633" else "#58a6ff"
        val t5hColor = if (t5hPercent > 80) "#da3633" else "#238636"

        // Build HTML tooltip similar to VSCode MarkdownString
        return buildString {
            append("<html><body>")
            append("<div style='font-family: Consolas, Monaco, monospace; font-size: 12px; line-height: 1.4; padding: 8px;'>")

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
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

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
            append("<hr style='border: none; border-top: 1px solid #30363d; margin: 8px 0;'>")

            // Quotas
            append("<div style='color: #8b949e; font-size: 11px; margin-bottom: 5px;'>配额状态</div>")

            // MCP Quota
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

            // Token 5h Quota
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

            // Traffic trend (sparkline)
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

    private fun formatPct(pct: Double): String {
        // If integer, drop .0
        if (pct % 1.0 == 0.0) return pct.toInt().toString()
        // Otherwise keep decimals
        return pct.toString()
    }

    private fun formatNumber(num: Long): String {
        return num.toString().replace(Regex("(?<=\\d)(?=(\\d{3})+$)"), ",")
    }

    private fun repeatChar(char: Char, count: Int): String {
        return StringBuilder().apply {
            repeat(count) { append(char) }
        }.toString()
    }

    private fun formatTokens(num: Long): String {
        return if (num >= 1000000) String.format("%.2fM", num / 1000000.0)
        else if (num >= 1000) String.format("%.1fK", num / 1000.0)
        else num.toString()
    }

    override fun ID(): String = "GLMUsageWidget"
    override fun getComponent(): JComponent = component
    override fun install(statusBar: StatusBar) {}
    override fun dispose() {
        timer?.stop()
        hideTooltip()
    }
}
