package com.nhnextsoft.qrcode.ui.feature

import com.nhnextsoft.qrcode.R

sealed class NavigationScreens(val router: String, val iconResourceId: Int) {
    object ScanCode : NavigationScreens("ScanCode", R.drawable.ic_scan)
    object GenerateCode : NavigationScreens("GenerateCode", R.drawable.ic_add)
    object History : NavigationScreens("History", R.drawable.ic_history)
    object Setting : NavigationScreens("Setting", R.drawable.ic_settings)
    object ScanResult : NavigationScreens("ScanResult", 0)
    object HistoryCreate : NavigationScreens("HistoryCreate", 0)
    object Arg {
        const val HISTORY_CREATE_ID = "HistoryCreateId"
        const val HISTORY_SCAN_ID = "historyScanId"
        const val HISTORY_SCAN_SHOW_IMAGE = "historyScanShowImage"
    }
}
