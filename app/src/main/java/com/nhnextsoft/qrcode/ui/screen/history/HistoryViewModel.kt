package com.nhnextsoft.qrcode.ui.screen.history

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.lifecycle.viewModelScope
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.db.dao.HistoryCreateCodeDao
import com.nhnextsoft.qrcode.db.dao.HistoryScanDao
import com.nhnextsoft.qrcode.model.data.Product
import com.nhnextsoft.qrcode.model.data.StoreUserSetting
import com.nhnextsoft.qrcode.ui.screen.scanresult.ProductResult
import com.nhnextsoft.qrcode.utils.FormatBarCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyScanDao: HistoryScanDao,
    private val historyCreateCodeDao: HistoryCreateCodeDao,
    private val storeUserSetting: StoreUserSetting,
) : BaseViewModel<HistoryContract.Event, HistoryContract.State, HistoryContract.Effect>() {

    private var isLoadedHistoryScan = false
    private var isLoadedHistoryCreateCode = false

    private var listStateHistoryScanData: MutableList<HistoryContract.StateHistoryScan> =
        mutableListOf()
    private var listStateHistoryCreateData: MutableList<HistoryContract.StateHistoryCreate> =
        mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                loadHistoryScanData()
            }
            launch {
                loadHistoryCreateData()
            }


        }
    }

    private suspend fun loadHistoryScanData() {
        historyScanDao.getAll().collect { listHistoryScan ->
            listStateHistoryScanData =
                listHistoryScan.map {
                    val barcodeParsedResult =
                        FormatBarCode.ofParsedResult(it.valueCode, it.format)
                    HistoryContract.StateHistoryScan(it, barcodeParsedResult)
                }.toMutableList()
            isLoadedHistoryScan = true
            checkLoading()
        }
    }

    private suspend fun loadHistoryCreateData() {
        historyCreateCodeDao.getAll().collect { listHistoryCreateCode ->
            listStateHistoryCreateData =
                listHistoryCreateCode.map {
                    val barcodeParsedResult = FormatBarCode.ofParsedResult(it.valueCode,
                        FormatBarCode.QR_CODE.name)
                    HistoryContract.StateHistoryCreate(it, barcodeParsedResult)
                }.toMutableList()
            isLoadedHistoryCreateCode = true
            checkLoading()
            //
            val productResult = ProductResult.create().getProduct()
            productResult.enqueue( object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>?, response: Response<List<Product>>?) {
                    if(response?.body() != null)
                       println(response.body())
                }
                override fun onFailure(call: Call<List<Product>>?, t: Throwable?) {
                    println(T)
                }
            })
        }
    }

    private fun checkLoading() {
        Timber.d(" isLoadedHistoryScan $isLoadedHistoryScan isLoadedHistoryCreateCode $isLoadedHistoryCreateCode")
        Timber.d(" isLoadedHistoryScan $listStateHistoryScanData isLoadedHistoryCreateCode $listStateHistoryCreateData")
        if (isLoadedHistoryScan && isLoadedHistoryCreateCode) {
            viewModelScope.launch {
                setState {
                    copy(
                        isLoaded = true,
                        listStateHistoryScan = listStateHistoryScanData,
                        listStateHistoryCreate = listStateHistoryCreateData
                    )
                }
            }
        }
    }

    override fun setInitialState(): HistoryContract.State = HistoryContract.State()

    override fun handleEvents(event: HistoryContract.Event) {
        when (event) {
            is HistoryContract.Event.EventDeleteHistoryScan -> {
                viewModelScope.launch(Dispatchers.IO) {
                    historyScanDao.deleteById(event.historyScanId)
                    loadHistoryScanData()
                }
            }
            is HistoryContract.Event.EventDeleteHistoryCreate -> {
                viewModelScope.launch(Dispatchers.IO) {
                    historyCreateCodeDao.deleteById(event.historyCreateId)
                    loadHistoryCreateData()
                }
            }
            is HistoryContract.Event.EventDeleteAllHistoryScan -> {
                viewModelScope.launch(Dispatchers.IO) {
                    historyScanDao.deleteAll()
                    loadHistoryCreateData()
                }
            }
            is HistoryContract.Event.EventDeleteAllHistoryCreate -> {
                viewModelScope.launch(Dispatchers.IO) {
                    historyCreateCodeDao.deleteAll()
                    loadHistoryCreateData()
                }
            }
            is HistoryContract.Event.EventOpenHistoryScan -> {
                viewModelScope.launch() {
                    event.historyScan.id?.let {
                        HistoryContract.Effect.Navigation.NavigateToScanResult(it).let {
                            setEffect { it }
                        }
                    }
                }
            }
            is HistoryContract.Event.EventOpenHistoryCreated -> {
                viewModelScope.launch {
                    event.historyCreateCode.id?.let {
                        HistoryContract.Effect.Navigation.NavigateToCreateHistory(it).let {
                            setEffect { it }
                        }
                    }
                }
            }
        }
    }
}