package com.safphere.glmusage

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import javax.swing.JOptionPane

class GLMRefreshAction : AnAction("刷新 GLM/智谱 AI 数据", "刷新 GLM/智谱 AI 用量数据", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // Access status bar and refresh
        val statusBar = WindowManager.getInstance().getStatusBar(project)
        val widget = statusBar.getWidget("GLMUsageWidget")
        if (widget is GLMStatusBarWidget) {
            widget.updateData(force = true)
        }
    }
}

class GLMConfigureAction : AnAction("配置 GLM/智谱 AI Token", "配置 GLM/智谱 AI API Token", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // Open settings dialog
        com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            "GLM/智谱 AI 用量监控"
        )
    }
}

class GLMShowUsageAction : AnAction("查看 GLM/智谱 AI 用量", "显示 GLM/智谱 AI API 用量信息", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // 获取usage数据
        val usageData = GLMUsageService.fetchData()

        if (usageData == null) {
            JOptionPane.showMessageDialog(
                null,
                "无法获取用量数据，请检查API配置",
                "GLM/智谱 AI 用量监控",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        // 显示对话框
        val dialog = GLMUsageDialog(usageData)
        dialog.show()
    }
}
