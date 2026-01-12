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
