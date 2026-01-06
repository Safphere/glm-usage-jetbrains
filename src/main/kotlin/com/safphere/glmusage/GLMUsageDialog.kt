package com.safphere.glmusage

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.border.EmptyBorder

/**
 * GLM Usage信息对话框 - 显示完整的用量统计和配额信息
 * 样式与glm-usage-vscode保持一致
 */
class GLMUsageDialog(private val usageData: GLMUsageData) : DialogWrapper(true) {

    init {
        title = "📊 GLM/智谱 AI 用量详情 - ${usageData.platform}"
        setResizable(true)
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel()
        mainPanel.layout = BorderLayout()
        mainPanel.preferredSize = Dimension(400, 350)

        // 标题区域
        val titlePanel = createTitlePanel()
        mainPanel.add(titlePanel, BorderLayout.NORTH)

        // 主要内容区域
        val contentPanel = createContentPanel()
        mainPanel.add(contentPanel, BorderLayout.CENTER)

        // 历史图表区域
        if (usageData.history.isNotEmpty()) {
            val historyPanel = createHistoryPanel()
            mainPanel.add(historyPanel, BorderLayout.SOUTH)
        }

        return mainPanel
    }

    private fun createTitlePanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.border = EmptyBorder(10, 10, 10, 10)

        val title = JBLabel("<html><b><span style='font-size:16px'>📊 ${usageData.platform} API Usage</span></b></html>")
        panel.add(title)

        return panel
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = EmptyBorder(0, 10, 10, 10)

        // 总调用次数
        val callsPanel = createStatPanel(
            "🔢 Total Calls",
            formatNumber(usageData.totals.calls),
            "Total number of API calls"
        )
        panel.add(callsPanel)
        panel.add(Box.createVerticalStrut(8))

        // 总Token数
        val tokensPanel = createStatPanel(
            "📝 Total Tokens",
            formatNumber(usageData.totals.tokens),
            "Total tokens used"
        )
        panel.add(tokensPanel)
        panel.add(Box.createVerticalStrut(15))

        // MCP配额
        val mcpPanel = createQuotaPanel(
            "⏱️  MCP (Time Limit)",
            usageData.quotas.mcp.used.toLong(),
            usageData.quotas.mcp.total.toLong(),
            usageData.quotas.mcp.pct.toInt()
        )
        panel.add(mcpPanel)
        panel.add(Box.createVerticalStrut(8))

        // Token限流
        val tokenPanel = createQuotaPanel(
            "🎯 Token 5h Limit",
            0,
            100,
            usageData.quotas.token5h.pct.toInt()
        )
        panel.add(tokenPanel)

        // 错误信息
        usageData.error?.let { error ->
            panel.add(Box.createVerticalStrut(15))
            val errorPanel = createErrorPanel(error)
            panel.add(errorPanel)
        }

        return panel
    }

    private fun createStatPanel(label: String, value: String, description: String): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        panel.alignmentX = JComponent.LEFT_ALIGNMENT

        val labelComponent = JBLabel("<html><b style='color:#cccccc;'>$label</b></html>")
        panel.add(labelComponent)

        val valueComponent = JBLabel("<html><span style='font-size:20px;color:#79c0ff;'><b>$value</b></span></html>")
        valueComponent.border = EmptyBorder(0, 10, 0, 0)
        panel.add(valueComponent)

        val descComponent = JBLabel("<html><span style='color:#8b949e;font-size:11px;'>$description</span></html>")
        descComponent.border = EmptyBorder(0, 10, 0, 0)
        panel.add(descComponent)

        return panel
    }

    private fun createQuotaPanel(label: String, used: Long, total: Long, percent: Int): JPanel {
        val panel = JPanel(BorderLayout(0, 5))
        panel.alignmentX = JComponent.LEFT_ALIGNMENT

        // 标题和百分比
        val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        val labelComponent = JBLabel("<html><b style='color:#cccccc;'>$label</b></html>")
        headerPanel.add(labelComponent)

        val percentColor = getColorForPercent(percent)
        val percentComponent = JBLabel(
            "<html><span style='font-size:14px;color:$percentColor'><b>${percent}%</b></span></html>"
        )
        percentComponent.border = EmptyBorder(0, 10, 0, 0)
        headerPanel.add(percentComponent)

        panel.add(headerPanel, BorderLayout.NORTH)

        // 进度条
        val progressBar = JProgressBar(0, 100)
        progressBar.value = percent
        progressBar.isOpaque = true
        progressBar.background = Color(72, 78, 89)  // Dark gray for VSCode look
        progressBar.foreground = getColorForPercentJBG(percent)
        progressBar.setPreferredSize(Dimension(300, 20))
        panel.add(progressBar, BorderLayout.CENTER)

        // 用量信息
        if (total > 0) {
            val infoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            val infoLabel = JBLabel(
                "<html><span style='color:#8b949e;font-size:11px;'>${formatNumber(used)} / ${formatNumber(total)}</span></html>"
            )
            infoPanel.add(infoLabel)
            panel.add(infoPanel, BorderLayout.SOUTH)
        }

        panel.border = EmptyBorder(5, 0, 5, 0)
        return panel
    }

    private fun createErrorPanel(error: String): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(10, 0, 0, 0)

        val errorLabel = JBLabel("<html><span style='color:#f85149;'>[ERROR] $error</span></html>")
        panel.add(errorLabel, BorderLayout.CENTER)

        return panel
    }

    private fun createHistoryPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(10, 10, 10, 10)
        panel.preferredSize = Dimension(400, 150)

        val title = JBLabel("<html><b style='color:#cccccc;'>📈 Recent Activity (Last 24h)</b></html>")
        panel.add(title, BorderLayout.NORTH)

        // VSCode-like sparkline visualization
        if (usageData.history.isNotEmpty()) {
            val historyContent = JPanel(BorderLayout())

            // Sparkline
            val calls = usageData.history.map { it.calls }
            val maxCalls = calls.maxOrNull()?.coerceAtLeast(1) ?: 1
            val bars = listOf(" ", "▁", "▂", "▃", "▄", "▅", "▆", "▇", "█")

            val sparklinePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 5))
            val sparklineLabel = JBLabel()
            val sparklineText = buildString {
                usageData.history.forEach { pt ->
                    if (pt.calls == 0) {
                        append("<span style='color:#30363d;'>_</span>")
                    } else {
                        val idx = ((pt.calls.toDouble() / maxCalls) * (bars.size - 1)).toInt()
                            .coerceIn(0, bars.size - 1)
                        append("<span style='color:#58a6ff;'>${bars[idx]}</span>")
                    }
                }
            }
            sparklineLabel.text = "<html>$sparklineText</html>"
            sparklinePanel.add(sparklineLabel)
            historyContent.add(sparklinePanel, BorderLayout.CENTER)

            // Peak info
            val peak = usageData.history.maxByOrNull { it.calls }
            if (peak != null) {
                val peakTime = if (peak.time.contains(" ")) peak.time.split(" ")[1] else peak.time
                val peakLabel = JBLabel("<html><span style='color:#8b949e;font-size:10px;'>Peak: <span style='color:#79c0ff;font-weight:bold;'>${peak.calls}</span> calls at $peakTime</span></html>")
                historyContent.add(peakLabel, BorderLayout.SOUTH)
            }

            panel.add(historyContent, BorderLayout.CENTER)
        }

        return panel
    }

    private fun formatNumber(number: Long): String {
        // Add commas for thousands separator (like VSCode)
        return number.toString().reversed().chunked(3).joinToString(",").reversed()
    }

    private fun getColorForPercent(percent: Int): String {
        return when {
            percent < 50 -> "#58a6ff"  // Blue (VSCode info)
            percent < 80 -> "#d29922"  // Yellow/orange (VSCode warning)
            else -> "#f85149"           // Red (VSCode error)
        }
    }

    private fun getColorForPercentJBG(percent: Int): Color {
        return when {
            percent < 50 -> Color(88, 166, 255)   // Blue
            percent < 80 -> Color(210, 153, 34)   // Yellow/orange
            else -> Color(248, 81, 73)            // Red
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(450, 400)
    }
}