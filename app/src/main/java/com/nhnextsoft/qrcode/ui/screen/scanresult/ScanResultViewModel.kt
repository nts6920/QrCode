package com.nhnextsoft.qrcode.ui.screen.scanresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.db.dao.HistoryScanDao
import com.nhnextsoft.qrcode.model.data.StoreUserSetting
import com.nhnextsoft.qrcode.ui.feature.NavigationScreens
import com.nhnextsoft.qrcode.utils.FormatBarCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
    private val historyScanDao: HistoryScanDao,
    private val storeUserSetting: StoreUserSetting,
) : BaseViewModel<ScanResultContract.Event, ScanResultContract.State, ScanResultContract.Effect>() {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val historyScanId = stateHandle.get<Long>(NavigationScreens.Arg.HISTORY_SCAN_ID)
            val isShowImage: Boolean =
                stateHandle.get<Boolean>(NavigationScreens.Arg.HISTORY_SCAN_SHOW_IMAGE) ?: false
            if (historyScanId != null) {
                val historyScan = historyScanDao.getItemById(historyScanId)
                val parsedResult =
                    FormatBarCode.ofParsedResult(historyScan.valueCode, historyScan.format)
                launch {
                    storeUserSetting.getSettingOpenUrlAutomatically.collect {
                        viewModelScope.launch {
                            setState {
                                copy(isSettingOpenUrlAutomatically = it, isShowImage = isShowImage)
                            }
                        }
                    }
                }
                viewModelScope.launch {
                    setState {
                        copy(historyScan = historyScan,
                            parsedResult = parsedResult,
                            isLoading = false, isShowImage = isShowImage)
                    }
                }
            }
        }
    }

    override fun setInitialState(): ScanResultContract.State =
        ScanResultContract.State(true, null, null)

    override fun handleEvents(event: ScanResultContract.Event) {
        Timber.d("handleEvents - $event")
        when (event) {
            is ScanResultContract.Event.OnClickBottomScanNow -> setEffect { ScanResultContract.Effect.Navigation.NavigateToScanNow }
        }
    }
}