package com.safphere.glmusage

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class GLMSettingsConfigurable : Configurable {
    private var mySettingsComponent: JPanel? = null
    private val apiKeyField = JBTextField()
    private val apiBaseUrlField = JBTextField()
    private val timeoutField = JBTextField()
    private val useMockDataCheckBox = JBCheckBox()
    private val helpTextArea = JBTextArea()

    override fun getDisplayName(): String  = "GLM/智谱 AI 用量监控"

    override fun getPreferredFocusedComponent(): JComponent = apiKeyField

    override fun createComponent(): JComponent {
        helpTextArea.text = """配置说明:
• API Key: 您的智谱 AI 或 Z.ai API Token (支持 sk-... 或 id.secret 格式)
• Base URL: API 基础地址 (默认: https://open.bigmodel.cn/api/anthropic)
• Timeout: 请求超时时间 (毫秒)
• Use Mock Data: 启用模拟数据用于测试

提示: 您也可以通过环境变量 ANTHROPIC_AUTH_TOKEN 设置 API Key
获取 Token: https://open.bigmodel.cn/ 或 https://z.ai/"""
        helpTextArea.isEditable = false
        helpTextArea.background = UIUtil.getPanelBackground()
        helpTextArea.border = JBUI.Borders.empty(0, 10)

        mySettingsComponent = FormBuilder.createFormBuilder()
            .addComponent(helpTextArea, 0)
            .addVerticalGap(10)
            .addLabeledComponent(JBLabel("API Key:"), apiKeyField, 1, false)
            .addTooltip("您的智谱 AI 或 Z.ai API Token")
            .addLabeledComponent(JBLabel("Base URL:"), apiBaseUrlField, 1, false)
            .addTooltip("API 基础地址")
            .addLabeledComponent(JBLabel("Timeout (ms):"), timeoutField, 1, false)
            .addTooltip("请求超时时间 (毫秒)")
            .addLabeledComponent(JBLabel("Use Mock Data:"), useMockDataCheckBox, 1, false)
            .addTooltip("启用模拟数据用于测试")
            .addComponentFillVertically(JPanel(), 0)
            .panel
        return mySettingsComponent!!
    }

    override fun isModified(): Boolean {
        val settings = GLMSettingsState.instance
        return apiKeyField.text != settings.apiKey ||
               apiBaseUrlField.text != settings.apiBaseUrl ||
               timeoutField.text != settings.timeout.toString() ||
               useMockDataCheckBox.isSelected != settings.useMockData
    }

    override fun apply() {
        val settings = GLMSettingsState.instance
        settings.apiKey = apiKeyField.text.trim()
        settings.apiBaseUrl = apiBaseUrlField.text.trim()
        settings.timeout = timeoutField.text.toIntOrNull() ?: 30000
        settings.useMockData = useMockDataCheckBox.isSelected
    }

    override fun reset() {
        val settings = GLMSettingsState.instance
        apiKeyField.text = settings.apiKey
        apiBaseUrlField.text = settings.apiBaseUrl
        timeoutField.text = settings.timeout.toString()
        useMockDataCheckBox.isSelected = settings.useMockData
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
