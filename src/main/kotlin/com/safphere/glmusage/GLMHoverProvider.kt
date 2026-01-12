package com.safphere.glmusage

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import javax.swing.JPanel
import java.awt.BorderLayout

/**
 * GLM Usage 悬停/弹出面板 Provider
 * 统一使用 GLMHoverTooltipPanel 以保持视觉一致性
 */
object GLMHoverProvider {

    /**
     * 在编辑器中显示悬停面板
     */
    fun show(editor: Editor, usageData: GLMUsageData) {
        val panel = GLMHoverTooltipPanel(usageData)

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
        val panel = GLMHoverTooltipPanel(usageData)

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
