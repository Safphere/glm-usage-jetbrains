package com.safphere.glmusage

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.safphere.glmusage.GLMSettingsState",
    storages = [Storage("GLMUsagePlugin.xml")]
)
class GLMSettingsState : PersistentStateComponent<GLMSettingsState> {
    var apiKey: String = ""
    var apiBaseUrl: String = "https://open.bigmodel.cn/api/anthropic"
    var timeout: Int = 30000
    var useMockData: Boolean = false

    companion object {
        val instance: GLMSettingsState
            get() = ApplicationManager.getApplication().getService(GLMSettingsState::class.java)
    }

    override fun getState(): GLMSettingsState {
        return this
    }

    override fun loadState(state: GLMSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
