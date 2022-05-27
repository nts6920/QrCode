package com.nhnextsoft.qrcode.ui.screen.scanresult

import com.google.zxing.client.result.ParsedResult
import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import com.nhnextsoft.qrcode.db.entity.HistoryScan
import com.nhnextsoft.qrcode.ui.feature.NavigationScreens
import com.nhnextsoft.qrcode.utils.FormatBarCode

class ScanResultContract {
    sealed class Event : ViewEvent {
        object OnClickBottomScanNow : Event()

        data class EventShare(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventCopy(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventCopyPassWifi(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventConnectWifi(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventEmail(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventAddContact(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventCallContact(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventSendSMS(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventOpenWeb(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventOpenGeo(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventSearchWeb(val valueQrCode: String, val format: FormatBarCode) : Event()
        data class EventEventAddCalendar(val valueQrCode: String, val format: FormatBarCode) : Event()
    }

    data class HistoryCreateState(
        val isLoading: Boolean = true,
        val historyCreateCode: HistoryCreateCode?,
        val parsedResult: ParsedResult?
    ) : ViewState

    data class State(
        val isLoading: Boolean = true,
        val historyScan: HistoryScan?,
        val parsedResult: ParsedResult?,
        val isSettingOpenUrlAutomatically: Boolean = false,
        val isShowImage:Boolean = false
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        data class OnHandleEventEffect(val event: Event):Effect()

        sealed class Navigation : Effect() {
            object NavigateToScanNow : Navigation()
            object NavigateToBackHistory : Navigation()
        }
    }
}