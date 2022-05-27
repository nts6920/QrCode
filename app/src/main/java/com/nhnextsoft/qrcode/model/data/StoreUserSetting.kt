package com.nhnextsoft.qrcode.model.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreUserSetting @Inject constructor(@ApplicationContext val context: Context) {
    companion object {
        private const val PREFERENCES_NAME_USER = "app_setting_datastore_prefs"

        private val Context.dataStore by preferencesDataStore(
            name = PREFERENCES_NAME_USER
        )

        val USER_SETTING_VIBRATE = booleanPreferencesKey("setting_vibrate")
        val USER_SETTING_AUTOFOCUS_CAMERA = booleanPreferencesKey("setting_autofocus_camera")
        val USER_SETTING_OPEN_URL_AUTOMATICALLY =
            booleanPreferencesKey("setting_open_url_automatically")
    }

    val getSettingVibrate: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_SETTING_VIBRATE] ?: true
    }

    val getSettingAutoFocusCamera: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_SETTING_AUTOFOCUS_CAMERA] ?: true
    }
    val getSettingOpenUrlAutomatically: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_SETTING_OPEN_URL_AUTOMATICALLY] ?: true
    }


    //save email into datastore
    suspend fun saveSettingVibrate(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_SETTING_VIBRATE] = state
        }
    }

    //save email into datastore
    suspend fun saveSettingAutoFocusCamera(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_SETTING_AUTOFOCUS_CAMERA] = state
        }
    }

    //save email into datastore
    suspend fun saveSettingOpenUrlAutomatically(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_SETTING_OPEN_URL_AUTOMATICALLY] = state
        }
    }


}