package com.safphere.glmusage

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Point
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.border.EmptyBorder

/**
 * GLM Usage 悬停/弹出面板
 * 样式与glm-usage-vscode保持一致
 */
class GLMHoverPanel(private val usageData: GLMUsageData) : JPanel() {

    init {
        layout = BorderLayout()
        border = BorderFactory.createLineBorder(JBColor.border(), 1)
        background = JBColor.background()
        preferredSize = Dimension(350, 200)

        add(createContentPanel(), BorderLayout.CENTER)
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout(0, 5)
        panel.background = JBColor.background()
        panel.border = EmptyBorder(8, 8, 8, 8)

        // 标题
        val titlePanel = createTitlePanel()
        panel.add(titlePanel, BorderLayout.NORTH)

        // 主要统计信息
        val statsPanel = createStatsPanel()
        panel.add(statsPanel, BorderLayout.CENTER)

        // 配额信息
        val quotaPanel = createQuotaPanel()
        panel.add(quotaPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun createTitlePanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        panel.background = JBColor.background()

        val title = JLabel("📊 ${usageData.platform} Usage")
        title.font = title.font.deriveFont(14f)
        title.foreground = JBColor.foreground()
        panel.add(title)

        return panel
    }

    private fun createStatsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout(0, 5)
        panel.background = JBColor.background()

        // 总调用次数
        val callsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        callsPanel.background = JBColor.background()
        val callsLabel = JLabel("🔢 Calls: <b>${formatNumber(usageData.totals.calls)}</b>")
        callsLabel.foreground = JBColor.foreground()
        callsPanel.add(callsLabel)

        // 总Token数
        val tokensPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        tokensPanel.background = JBColor.background()
        val tokensLabel = JLabel("📝 Tokens: <b>${formatNumber(usageData.totals.tokens)}</b>")
        tokensLabel.foreground = JBColor.foreground()
        tokensPanel.add(tokensLabel)

        panel.add(callsPanel, BorderLayout.NORTH)
        panel.add(tokensPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun createQuotaPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout(0, 5)
        panel.background = JBColor.background()

        // MCP配额
        val mcpPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        mcpPanel.background = JBColor.background()
        val mcpPercent = usageData.quotas.mcp.pct.toInt()
        val mcpColor = getColorForPercent(mcpPercent)
        val mcpLabel = JLabel("⏱️  MCP: <font color='$mcpColor'>${mcpPercent}%</font> " +
                "(${formatNumber(usageData.quotas.mcp.used.toLong())}/${formatNumber(usageData.quotas.mcp.total.toLong())})")
        mcpLabel.foreground = JBColor.foreground()
        mcpPanel.add(mcpLabel)

        // Token限流
        val tokenPercent = usageData.quotas.token5h.pct.toInt()
        val tokenColor = getColorForPercent(tokenPercent)
        val tokenPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        tokenPanel.background = JBColor.background()
        val tokenLabel = JLabel("🎯 Token 5h: <font color='$tokenColor'>${tokenPercent}%</font>")
        tokenLabel.foreground = JBColor.foreground()
        tokenPanel.add(tokenLabel)

        panel.add(mcpPanel, BorderLayout.NORTH)
        panel.add(tokenPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
            number >= 1000 -> String.format("%.1fK", number / 1000.0)
            else -> number.toString()
        }
    }

    private fun getColorForPercent(percent: Int): String {
        return when {
            percent < 50 -> "#28a745"  // 绿色
            percent < 80 -> "#ffc107"  // 黄色
            else -> "#dc3545"           // 红色
        }
    }

    companion object {
        /**
         * 在编辑器中显示悬停面板
         */
        fun show(editor: Editor, usageData: GLMUsageData) {
            val panel = GLMHoverPanel(usageData)

            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setRequestFocus(false)
                .setResizable(false)
                .setMovable(false)
                .setCancelOnClickOutside(true)
                .setCancelKeyEnabled(true)
                .createPopup()

            val point = Point(editor.caretModel.visualPosition.column, editor.caretModel.visualPosition.line)
            popup.show(RelativePoint(editor.contentComponent, point))
        }

        /**
         * 在项目工具窗口中显示用量信息
         */
        fun showInProject(project: Project, usageData: GLMUsageData) {
            val panel = GLMHoverPanel(usageData)

            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setRequestFocus(false)
                .setResizable(false)
                .setMovable(false)
                .setCancelOnClickOutside(true)
                .setCancelKeyEnabled(true)
                .createPopup()

            val statusBar = com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project)
            statusBar?.component?.let {
                popup.show(RelativePoint(it, it.bounds.location))
            }
        }
    }
}