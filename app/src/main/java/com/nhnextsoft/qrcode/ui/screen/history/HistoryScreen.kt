package com.nhnextsoft.qrcode.ui.screen.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.zxing.client.result.*
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.base.LAUNCH_LISTEN_FOR_EFFECTS
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import com.nhnextsoft.qrcode.db.entity.HistoryScan
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import com.nhnextsoft.qrcode.utils.FormatBarCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.*

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HistoryScreenPreview() {
    QRCodeTheme {

        var iconResource = R.drawable.ic_text
        var titleText = stringResource(R.string.text_text)
        var bodyText = ""
        val strLastUpdate = AppUtils.formatDate(Date())
        ItemRowHistoryBarCode(icon = iconResource,
            title = titleText,
            content = bodyText,
            lastUpdate = strLastUpdate,
            onConfirmDelete = {},
            onClick = {})
    }
}

@Composable
fun HistoryScreen(
    state: HistoryContract.State,
    effectFlow: Flow<HistoryContract.Effect>?,
    onEventSent: (event: HistoryContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: HistoryContract.Effect.Navigation) -> Unit?,
) {
    LaunchedEffect(LAUNCH_LISTEN_FOR_EFFECTS) {
        effectFlow?.onEach { effect ->
            when (effect) {
                is HistoryContract.Effect.Navigation.NavigateToScanResult ->
                    onNavigationRequested.invoke(effect)
                is HistoryContract.Effect.Navigation.NavigateToCreateHistory ->
                    onNavigationRequested.invoke(effect)
            }

        }?.collect()
    }
    var isSelectedScanned by remember { mutableStateOf(true) }
    var isLoaded by remember { mutableStateOf(false) }
    var isShowConfirmHistoryScan by remember { mutableStateOf(false) }
    var isShowConfirmHistoryCreateCode by remember { mutableStateOf(false) }

    isLoaded = state.isLoaded
    Timber.d("isLoaded $isLoaded")
    Timber.d("state $state")
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(color = Color.White)) {
                Text(
                    text = stringResource(R.string.title_history),
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    style = Typography.h5)
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = ColorButton.copy(alpha = 0.8f),
                    thickness = 0.5.dp,
                )
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = Color.White)) {
                Row(modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            isSelectedScanned = true
                        },
                        contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.title_scanned),
                            color = if (isSelectedScanned) ColorButton else Color.DarkGray,
                            fontWeight = if (isSelectedScanned) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = Typography.h5)
                    }
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            isSelectedScanned = false
                        },
                        contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.title_created),
                            color = if (!isSelectedScanned) ColorButton else Color.DarkGray,
                            fontWeight = if (!isSelectedScanned) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = Typography.h5)
                    }
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = ColorButton,
                    thickness = 1.dp,
                )
            }

            val lazyListStateScanned = rememberLazyListState()
            val lazyListStateCreated = rememberLazyListState()

            val listHistoryScan = state.listStateHistoryScan
            val listHistoryCreateCode = state.listStateHistoryCreate
            if (isLoaded) {
                if (isSelectedScanned) {
                    HistoryScreenScanned(
                        listHistoryScan,
                        effectFlow,
                        onEventSent,
                        onNavigationRequested,
                        lazyListStateScanned)
                } else {
                    HistoryScreenCreated(
                        listHistoryCreateCode,
                        effectFlow,
                        onEventSent,
                        onNavigationRequested,
                        lazyListStateCreated)
                }
            } else {
                ShowPlaceholderList()
            }


        }
        FloatingActionButton(
            onClick = {
                if (isSelectedScanned) {
                    isShowConfirmHistoryScan = true
                } else {
                    isShowConfirmHistoryCreateCode = true
                }
                Timber.d("isSelectedScanned $isSelectedScanned isShowConfirmHistoryScan $isShowConfirmHistoryScan isShowConfirmHistoryCreateCode $isShowConfirmHistoryCreateCode")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp, 10.dp, 30.dp, 150.dp),
            backgroundColor = ColorButton) {
            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_trash),
                tint = Color.White,
                contentDescription = "")
        }
        if (isShowConfirmHistoryScan) {
            AlertDialogConfirmDelete(
                bobyText = stringResource(id = R.string.description_confirm_delete_all),
                onDismissRequest = { isShowConfirmHistoryScan = false }

            ) {
                onEventSent.invoke(HistoryContract.Event.EventDeleteAllHistoryScan)
                isShowConfirmHistoryScan = false
            }

        }
        if (isShowConfirmHistoryCreateCode) {
            AlertDialogConfirmDelete(
                bobyText = stringResource(id = R.string.description_confirm_delete_all),
                onDismissRequest = { isShowConfirmHistoryCreateCode = false }
            ) {
                onEventSent.invoke(HistoryContract.Event.EventDeleteAllHistoryCreate)
                isShowConfirmHistoryCreateCode = false
            }
        }
    }
}

@Composable
fun ShowPlaceholderList() {
    val list = (0..10).map { it.toString() }
    LazyColumn(modifier = Modifier
        .fillMaxHeight()
        .padding(top = 18.dp, bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = list, itemContent = { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
                .height(60.dp)) {
                Box(modifier = Modifier
                    .aspectRatio(1f)
                    .weight(0.1f)
                    .fillMaxHeight()
                    .padding(4.dp)) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_home),
                        contentDescription = "",
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }
                Column(modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(12.dp)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(12.dp)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }
                Column(modifier = Modifier.weight(0.4f)) {
                    Text(
                        text = "",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }
            }
            Divider(
                modifier = Modifier.fillMaxWidth(0.96f),
                color = ColorButton.copy(alpha = 0.5f),
                thickness = 0.4.dp,
                startIndent = 16.dp)
        })
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreenScanned(
    listHistoryScan: MutableList<HistoryContract.StateHistoryScan>,
    effectFlow: Flow<HistoryContract.Effect>?,
    onEventSent: (event: HistoryContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: HistoryContract.Effect.Navigation) -> Unit?,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp, bottom = 80.dp),
        state = lazyListState) {
        items(items = listHistoryScan) { item ->
            var isShowDialogConfirm by remember { mutableStateOf(false) }
            Card(backgroundColor = Color.White,
                elevation = 0.dp) {
                ShowContentItemHistory(item.historyScan, item.parsedResult, onConfirmDelete = {
                    Timber.d("onConfirmDelete ${item.historyScan}")
                    isShowDialogConfirm = true
                }) {
                    isShowDialogConfirm = false
                    Timber.d("onClickShow ${item.historyScan}")
                    onEventSent.invoke(HistoryContract.Event.EventOpenHistoryScan(item.historyScan))
                }
            }
            if (isShowDialogConfirm) {
                AlertDialogConfirmDelete(onDismissRequest = { isShowDialogConfirm = false }) {
                    item.historyScan.id?.let {
                        HistoryContract.Event.EventDeleteHistoryScan(it)
                    }?.let { onEventSent.invoke(it) }
                    isShowDialogConfirm = false
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreenCreated(
    listHistoryCreateCode: MutableList<HistoryContract.StateHistoryCreate>,
    effectFlow: Flow<HistoryContract.Effect>?,
    onEventSent: (event: HistoryContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: HistoryContract.Effect.Navigation) -> Unit?,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(modifier = Modifier
        .fillMaxHeight()
        .padding(top = 8.dp, bottom = 80.dp),
        state = lazyListState) {
        items(items = listHistoryCreateCode, itemContent = { item ->
            var isShowDialogConfirm by remember { mutableStateOf(false) }
            Card(backgroundColor = Color.White, elevation = 0.dp) {
                ShowContentItemHistory(item.historyCreateCode,
                    item.parsedResult,
                    onConfirmDelete = {
                        isShowDialogConfirm = true
                    }) {
                    Timber.d("HistoryCreateCode $item ")
                    onEventSent.invoke(HistoryContract.Event.EventOpenHistoryCreated(item.historyCreateCode))
                }
            }
            if (isShowDialogConfirm) {
                AlertDialogConfirmDelete(
                    onDismissRequest = { isShowDialogConfirm = false }
                ) {
                    item.historyCreateCode.id?.let {
                        HistoryContract.Event.EventDeleteHistoryCreate(it)
                    }?.let { onEventSent.invoke(it) }
                    isShowDialogConfirm = false
                }
            }

        })
    }
}

@Composable
fun ShowContentItemHistory(
    item: HistoryScan,
    barcodeParsedResult: ParsedResult?,
    onConfirmDelete: () -> Unit,
    onClick: () -> Unit,
) {
    ShowContentBarcode(
        formatCode = item.format,
        barcodeResult = barcodeParsedResult,
        lastUpdate = item.date,
        onConfirmDelete = onConfirmDelete,
        onClick = onClick
    )
}

@Composable
fun ShowContentItemHistory(
    item: HistoryCreateCode,
    barcodeParsedResult: ParsedResult?,
    onConfirmDelete: () -> Unit,
    onClick: () -> Unit,
) {
    ShowContentBarcode(
        formatCode = item.format,
        barcodeResult = barcodeParsedResult,
        lastUpdate = item.date,
        onConfirmDelete = onConfirmDelete,
        onClick = onClick
    )
}

@Composable
fun ShowContentBarcode(
    formatCode: String,
    barcodeResult: ParsedResult?,
    lastUpdate: Date,
    onConfirmDelete: () -> Unit,
    onClick: () -> Unit,
) {
    var iconResource = R.drawable.ic_text
    var titleText = stringResource(R.string.text_text)
    var bodyText = ""
    val strLastUpdate = AppUtils.formatDate(lastUpdate)
    barcodeResult?.let { parsedResult ->
        when (parsedResult) {
            is TelParsedResult -> {
                iconResource = R.drawable.ic_phone
                titleText = stringResource(R.string.text_phone)
                bodyText = parsedResult.number
            }
            is TextParsedResult -> {
                iconResource = R.drawable.ic_text
                titleText = stringResource(R.string.text_text)
                bodyText = parsedResult.displayResult
            }
            is WifiParsedResult -> {
                iconResource = R.drawable.ic_wifi
                titleText = stringResource(R.string.text_wifi)
                bodyText = stringResource(R.string.text_ssid) + ": " + parsedResult.ssid
            }
            is URIParsedResult -> {
                iconResource = R.drawable.ic_url
                titleText = stringResource(R.string.text_url)
                bodyText = parsedResult.uri
            }
            is SMSParsedResult -> {
                iconResource = R.drawable.ic_sms
                titleText = stringResource(R.string.text_sms)
                bodyText = stringResource(R.string.text_phone) + ": " + parsedResult.numbers.first()
            }
            is EmailAddressParsedResult -> {
                iconResource = R.drawable.ic_email
                titleText = stringResource(R.string.text_email)
                bodyText = parsedResult.tos.first()
            }
            is GeoParsedResult -> {
                iconResource = R.drawable.ic_geo
                titleText = stringResource(R.string.text_geo)
                bodyText = parsedResult.geoURI
            }
            is AddressBookParsedResult -> {
                iconResource = R.drawable.ic_contact
                titleText = stringResource(R.string.text_contact)
                bodyText = stringResource(R.string.text_name) + ": " + parsedResult.names.first()
            }
            is CalendarParsedResult -> {
                iconResource = R.drawable.ic_calendar
                titleText = stringResource(R.string.text_calendar)
                bodyText = parsedResult.summary
            }
            else -> {
                if (formatCode != FormatBarCode.QR_CODE.name){
                    iconResource = R.drawable.ic_barcode
                    titleText = stringResource(R.string.text_barcode)
                    bodyText = parsedResult.displayResult
                }else {
                    iconResource = R.drawable.ic_text
                    titleText = stringResource(R.string.text_text)
                    bodyText = parsedResult.displayResult
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            ItemRowHistoryBarCode(icon = iconResource,
                title = titleText,
                content = bodyText,
                lastUpdate = strLastUpdate,
                onConfirmDelete = onConfirmDelete,
                onClick = onClick)
            Divider(
                modifier = Modifier
                    .fillMaxWidth(),
                color = ColorButton.copy(alpha = 0.3f),
                thickness = 0.2.dp,
            )
        }

    }
}

@Composable
fun ItemRowHistoryBarCode(
    icon: Int,
    title: String,
    content: String,
    lastUpdate: String,
    onConfirmDelete: () -> Unit,
    onClick: () -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(90.dp)
        .clickable {
            onClick.invoke()
        }) {
        Box(modifier = Modifier
            .weight(0.1f)
            .fillMaxHeight()
            .padding(start = 8.dp),
            contentAlignment = Alignment.Center) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = ColorButton
            )
        }
        Column(modifier = Modifier
            .weight(0.6f)
            .fillMaxHeight()
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                style = Typography.body2
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = content,
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                maxLines = 1,
                fontWeight = FontWeight.Normal,
                overflow = TextOverflow.Ellipsis,
                style = Typography.body2
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = lastUpdate,
                modifier = Modifier
                    .fillMaxWidth(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = Typography.body2
            )
        }
        Column(
            modifier = Modifier
                .weight(0.1f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = {
                onConfirmDelete.invoke()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Localized description", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AlertDialogConfirmDelete(
    bobyText: String = stringResource(R.string.confirm_item_deletion),
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    var isShow by remember { mutableStateOf(true) }

    if (isShow) {
        AlertDialog(
            onDismissRequest = {
                isShow = false
                onDismissRequest.invoke()
            },
            title = {
                Text(text = stringResource(R.string.confirm))
            },
            text = {
                Text(bobyText)
            },
            buttons = {
                Box(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth(),
                ) {
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .align(Alignment.BottomStart),
                        onClick = {
                            isShow = false
                            onDismissRequest.invoke()
                        },
                    ) {
                        Text(stringResource(R.string.btn_dismiss), color = Color.Gray)
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.BottomEnd),
                        onClick = {
                            onConfirmDelete.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorButton
                        )
                    ) {
                        Text(stringResource(R.string.confirm), color = Color.White)
                    }
                }
            }
        )
    }
}


