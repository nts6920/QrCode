package com.nhnextsoft.qrcode.ui.screen.main

import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState

class MainContract {

    sealed class Event : ViewEvent {}

    data class State(val isLoading: Boolean = false) :
        ViewState

    sealed class Effect : ViewSideEffect {
        object DataWasLoaded : Effect()

        sealed class Navigation : Effect() {
            data class ToCategoryDetails(val categoryName: String) : Navigation()
        }
    }
}