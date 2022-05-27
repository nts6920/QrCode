package com.nhnextsoft.qrcode.ui.screen.generate

import androidx.lifecycle.viewModelScope
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.db.dao.HistoryCreateCodeDao
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val historyCreateCodeDao: HistoryCreateCodeDao,
) : BaseViewModel<GenerateContract.Event, GenerateContract.State, GenerateContract.Effect>() {
    override fun setInitialState(): GenerateContract.State = GenerateContract.State()

    override fun handleEvents(event: GenerateContract.Event) {
        when (event) {
            is GenerateContract.Event.EventSaveCreateCode -> {
                handleSaveCreateCode(event.valueData, event.formatBarCode)
            }
        }
    }

    private fun handleSaveCreateCode(valueData: String, formatBarCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val historyCreateCode = HistoryCreateCode(
                valueCode = valueData,
                date = Date(),
                format = formatBarCode
            )
            val historyCreateTemp =
                historyCreateCodeDao.getItemByValueCode(historyCreateCode.valueCode ?: "")
            if (historyCreateTemp != null) {
                historyCreateCode.id = historyCreateTemp.id
            }
            val idSave = historyCreateCodeDao.insert(historyCreateCode)
            setEffect {
                GenerateContract.Effect.Navigation.NavigateToCreateHistory(idSave)
            }
        }
    }

}