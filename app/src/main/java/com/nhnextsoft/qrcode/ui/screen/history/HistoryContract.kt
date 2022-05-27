package com.nhnextsoft.qrcode.ui.screen.history

import com.google.zxing.client.result.ParsedResult
import com.nhnextsoft.qrcode.base.ViewEvent
import com.nhnextsoft.qrcode.base.ViewSideEffect
import com.nhnextsoft.qrcode.base.ViewState
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import com.nhnextsoft.qrcode.db.entity.HistoryScan

class HistoryContract {

    sealed class Event : ViewEvent {
        object EventDeleteAllHistoryScan : Event()
        object EventDeleteAllHistoryCreate : Event()
        data class EventDeleteHistoryScan(val historyScanId: Long) : Event()
        data class EventDeleteHistoryCreate(val historyCreateId: Long) : Event()
        data class EventOpenHistoryCreated(val historyCreateCode: HistoryCreateCode) : Event()
        data class EventOpenHistoryScan(val historyScan: HistoryScan) : Event()
    }


    data class StateHistoryScan(val historyScan: HistoryScan, val parsedResult: ParsedResult?)
    data class StateHistoryCreate(
        val historyCreateCode: HistoryCreateCode,
        val parsedResult: ParsedResult?,
    )

    data class State(
        val isLoaded: Boolean = false,
        val listStateHistoryScan: MutableList<StateHistoryScan> = mutableListOf(),
        val listStateHistoryCreate: MutableList<StateHistoryCreate> = mutableListOf(),
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        sealed class Navigation : Effect() {
            data class NavigateToScanResult(val historyScanId: Long) : Navigation()

            data class NavigateToCreateHistory(val historyCreateId: Long) : Navigation()
        }
    }
}