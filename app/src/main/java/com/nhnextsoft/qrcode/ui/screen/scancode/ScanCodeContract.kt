package com.nhnextsoft.qrcode.ui.screen.scancode

import com.google.mlkit.vision.barcode.common.Barcode
import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState
import com.nhnextsoft.qrcode.db.entity.HistoryScan

class ScanCodeContract {

    sealed class Event : ViewEvent {
        data class OnScanResult(val barcode: Barcode) : Event()
    }

    data class State(val settingVibrate: Boolean = true) : ViewState

    sealed class Effect : ViewSideEffect {
        sealed class Navigation : Effect() {
            data class NavigateToScanResult(val historyScanId:Long) : Navigation()
        }
    }
}