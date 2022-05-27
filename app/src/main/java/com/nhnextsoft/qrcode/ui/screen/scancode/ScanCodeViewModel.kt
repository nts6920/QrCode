package com.nhnextsoft.qrcode.ui.screen.scancode

import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.db.dao.HistoryScanDao
import com.nhnextsoft.qrcode.db.entity.HistoryScan
import com.nhnextsoft.qrcode.model.data.StoreUserSetting
import com.nhnextsoft.qrcode.utils.FormatBarCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScanCodeViewModel @Inject constructor(
    private val historyScanDao: HistoryScanDao,
    private val storeUserSetting: StoreUserSetting,
) : BaseViewModel<ScanCodeContract.Event, ScanCodeContract.State, ScanCodeContract.Effect>() {
    override fun setInitialState(): ScanCodeContract.State = ScanCodeContract.State(false)

    override fun handleEvents(event: ScanCodeContract.Event) {
        when (event) {
            is ScanCodeContract.Event.OnScanResult -> {
                handleEventsScanResult(event.barcode)
            }
//                setEffect { ScanCodeContract.Effect.Navigation.NavigateToScanResult(event.listBarcode.last()) }
        }
    }

    private fun handleEventsScanResult(barcode: Barcode) {
        viewModelScope.launch(Dispatchers.IO) {
            val historyScan = historyScanDao.getItemByValueCode(barcode.rawValue ?: "")
            val format = FormatBarCode.of(barcode)

            barcode.rawValue?.let { value ->
                val itemInsert = HistoryScan(
                    valueCode = value,
                    format = format.name,
                    date = Date()
                )

                val historyScanId: Long?
                itemInsert.date = Date()
                if (historyScan == null) {
                    historyScanId = historyScanDao.insert(itemInsert)
                } else {
                    historyScanId = historyScan.id
                    itemInsert.id = historyScanId
                    historyScanDao.update(itemInsert)
                }
                viewModelScope.launch {
                    setEffect {
                        ScanCodeContract.Effect.Navigation.NavigateToScanResult(
                            historyScanId!!)
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            launch {
                storeUserSetting.getSettingVibrate.collect {
                    setState { copy(settingVibrate = it) }
                    Timber.d("state: getSettingVibrate $viewState")
                }
            }
        }
    }
}