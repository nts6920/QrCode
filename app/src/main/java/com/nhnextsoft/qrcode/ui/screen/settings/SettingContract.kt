package com.nhnextsoft.qrcode.ui.screen.settings

import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState

class SettingContract {

    sealed class Event : ViewEvent {
        data class SettingVibrateChange(val state: Boolean) : Event()
        data class SettingAutoFocusCameraChange(val state: Boolean) : Event()
        data class SettingOpenUrlAutomaticallyChange(val state: Boolean) : Event()
    }

    data class State(
        val settingVibrate: Boolean = true,
        val settingAutoFocusCamera: Boolean = true,
        val settingOpenUrlAutomatically: Boolean = true,
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        object DataWasLoaded : Effect()

        sealed class Navigation : Effect() {

        }
    }
}