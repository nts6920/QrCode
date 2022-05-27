package com.nhnextsoft.qrcode.ui.screen.historycreateresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.db.dao.HistoryCreateCodeDao
import com.nhnextsoft.qrcode.ui.feature.NavigationScreens
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultContract
import com.nhnextsoft.qrcode.utils.FormatBarCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HistoryCreateResultViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
    private val historyCreateCodeDao: HistoryCreateCodeDao,
) : BaseViewModel<ScanResultContract.Event, ScanResultContract.HistoryCreateState, ScanResultContract.Effect>() {

    override fun setInitialState(): ScanResultContract.HistoryCreateState =
        ScanResultContract.HistoryCreateState(true, null, null)

    override fun handleEvents(event: ScanResultContract.Event) {

    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val historyCreateId = stateHandle.get<Long>(NavigationScreens.Arg.HISTORY_CREATE_ID)
            if (historyCreateId != null) {
                val historyCreateCode = historyCreateCodeDao.getItemById(historyCreateId)
                val parsedResult =
                    FormatBarCode.ofParsedResult(historyCreateCode.valueCode,
                        FormatBarCode.QR_CODE.toString())
                viewModelScope.launch {
                    setState {
                        copy(
                            historyCreateCode = historyCreateCode,
                            parsedResult = parsedResult,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
