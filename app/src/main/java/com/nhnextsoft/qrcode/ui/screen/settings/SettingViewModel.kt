package com.nhnextsoft.qrcode.ui.screen.settings

import androidx.lifecycle.viewModelScope
import com.nhnextsoft.qrcode.base.BaseViewModel
import com.nhnextsoft.qrcode.model.data.StoreUserSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val storeUserSetting: StoreUserSetting,
) : BaseViewModel<SettingContract.Event, SettingContract.State, SettingContract.Effect>() {

    init {
        viewModelScope.launch {
            launch {
                storeUserSetting.getSettingVibrate.collect {
                    setState { copy(settingVibrate = it) }
                    Timber.d("state: getSettingVibrate $viewState")
                }
            }
            launch {
                storeUserSetting.getSettingAutoFocusCamera.collect {
                    setState { copy(settingAutoFocusCamera = it) }
                    Timber.d("state: getSettingAutoFocusCamera $viewState")
                }
            }
            launch {
                storeUserSetting.getSettingOpenUrlAutomatically.collect {
                    setState { copy(settingOpenUrlAutomatically = it) }
                    Timber.d("state: settingOpenUrlAutomatically $viewState")
                }
            }

        }
    }

    override fun setInitialState(): SettingContract.State = SettingContract.State()

    override fun handleEvents(event: SettingContract.Event) {
        Timber.d("event ${event.toString()}")
        viewModelScope.launch {
            when (event) {
                is SettingContract.Event.SettingAutoFocusCameraChange -> storeUserSetting.saveSettingAutoFocusCamera(
                    event.state)
                is SettingContract.Event.SettingOpenUrlAutomaticallyChange -> storeUserSetting.saveSettingOpenUrlAutomatically(
                    event.state)
                is SettingContract.Event.SettingVibrateChange -> storeUserSetting.saveSettingVibrate(
                    event.state)
            }
        }
    }
}