package com.safphere.glmusage

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class GLMStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "GLMUsageWidget"
    override fun getDisplayName(): String = "GLM/智谱 AI 实时用量监控"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = GLMStatusBarWidget(project)
    override fun disposeWidget(widget: StatusBarWidget) {
        if (widget is GLMStatusBarWidget) {
            widget.dispose()
        }
    }
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}
