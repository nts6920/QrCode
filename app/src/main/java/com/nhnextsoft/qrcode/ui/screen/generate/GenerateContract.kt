package com.nhnextsoft.qrcode.ui.screen.generate

import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState

class GenerateContract {
    sealed class Event : ViewEvent {
        //        data class EventGenerateCode(val valueData: String) : Event()
        data class EventSaveCreateCode(val valueData: String, val formatBarCode: String) : Event()
    }


    data class State(
        val isLoaded: Boolean = false,
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        data class EffectGenerateCode(val valueData: String) : Effect()

        sealed class Navigation : Effect() {
            data class NavigateToCreateHistory(val historyCreateId: Long) : Navigation()
        }
    }
}