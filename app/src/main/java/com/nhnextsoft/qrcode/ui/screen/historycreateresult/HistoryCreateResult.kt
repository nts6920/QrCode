package com.nhnextsoft.qrcode.ui.screen.historycreateresult

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.BarcodeFormat
import com.nhnextsoft.qrcode.BuildConfig
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.db.entity.HistoryCreateCode
import com.nhnextsoft.qrcode.toColor
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultContent
import com.nhnextsoft.qrcode.ui.screen.scanresult.ScanResultContract
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import com.nhnextsoft.qrcode.utils.FormatBarCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


@Preview(showBackground = true)
@Composable
fun ScanHistoryCreateResultScreenPreview() {
    val valueCode = "sms:+15551212?subject=foo&body=bar"
    val parsedResult = FormatBarCode.ofParsedResult(valueCode, "QR Code")
    QRCodeTheme {
        HistoryCreateResult(
            state = ScanResultContract.HistoryCreateState(false,
                HistoryCreateCode(
                    id = 1,
                    valueCode = valueCode,
                    date = Date(),
                ),
                parsedResult = parsedResult
            ),
            effectFlow = null,
            onEventSent = {

            },
            onNavigationRequested = {

            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HistoryCreateResult(
    state: ScanResultContract.HistoryCreateState,
    effectFlow: Flow<ScanResultContract.Effect>?,
    onEventSent: (event: ScanResultContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: ScanResultContract.Effect.Navigation) -> Unit?,
) {
    var isShowCode by remember { mutableStateOf(false) }
    val historyCreateCode = state.historyCreateCode
    val parsedResult = state.parsedResult
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val readStoragePermissionState =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var showDialogRequestReadPermission by remember { mutableStateOf(false) }
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (headerContent, bodyContent, bottomContent) = createRefs()
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .constrainAs(headerContent) {
                top.linkTo(parent.top)
            }
            .background(color = Color.White)) {
            Text(
                text = stringResource(R.string.title_created_history),
                modifier = Modifier
                    .align(Alignment.Center),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = Typography.h5)
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = ColorButton,
                thickness = 0.5.dp,
            )
            IconButton(
                onClick = { onNavigationRequested.invoke(ScanResultContract.Effect.Navigation.NavigateToBackHistory) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
            ) {
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "")
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .constrainAs(bodyContent) {
                    top.linkTo(headerContent.bottom)
                    bottom.linkTo(bodyContent.top)
                }
                .padding(top = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(50.dp)
                                .fillMaxWidth(),
                            color = ColorButton
                        )
                        Text(text = "loading", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                if (historyCreateCode != null && parsedResult != null) {
                    val formatBarCode = FormatBarCode.ofToFormatBarCode(historyCreateCode.format)
                    Timber.d("state.lastPreviousRoute $state")

                    Timber.d("showImageResult lastPreviousRoute $state")
                    ScanResultContent(
                        valueCode = historyCreateCode.valueCode,
                        prBarCode = parsedResult,
                        formatBarCode = formatBarCode,
                        historyCreateCode.date,
                        showImageResult = false,
                        colorBarcode = historyCreateCode.color.toColor(),
                        onEventSent)
                } else Text(
                    text = stringResource(R.string.error_please_try_again),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        val qrCodeBitmap = historyCreateCode?.let {
            AppUtils.createBarCodeByBarcodeFormat(
                codeData = it.valueCode,
                barcodeFormat = FormatBarCode.ofToFormat(it.format)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .constrainAs(bottomContent) {
                    bottom.linkTo(parent.bottom)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isShowCode) 150.dp else 0.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .background(
                        color = ColorButton.copy(blue = 1f),
                        shape = RoundedCornerShape(topStart = 20.dp,
                            topEnd = 20.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Spacer(modifier = Modifier.size(18.dp))
                Column(Modifier.weight(0.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                    qrCodeBitmap?.let {
                        Image(bitmap = it.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(100.dp))

                    }
                }
                Column(Modifier.weight(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager()) {
                                historyCreateCode?.let {
                                    gotoSaveQrcode(context,
                                        it.valueCode,
                                        coroutineScope,
                                        FormatBarCode.ofToFormat(it.format),
                                        it.color.toColor())
                                }
                            } else showDialogRequestReadPermission = true
                        } else {
                            if (readStoragePermissionState.status.isGranted) {
                                showDialogRequestReadPermission = false
                                historyCreateCode?.let {
                                    gotoSaveQrcode(context,
                                        it.valueCode,
                                        coroutineScope,
                                        FormatBarCode.ofToFormat(it.format),
                                        it.color.toColor())
                                }
                            } else {
                                showDialogRequestReadPermission = true
                            }
                        }

                    },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorButton
                        )
                    ) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_save_24),
                            contentDescription = null, tint = Color.White)
                        Text(
                            text = stringResource(R.string.btn_save_code),
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            style = Typography.body2)
                    }

                    Button(
                        onClick = {
                            isShowCode = false
                        },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorButton
                        )
                    ) {

                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_qr_code),
                            contentDescription = null, tint = Color.White)
                        Text(
                            text = stringResource(R.string.btn_hide_code),
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            style = Typography.body2)
                    }
                }


            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(if (!isShowCode) 60.dp else 0.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .background(
                    color = ColorButton.copy(blue = 1f),
                    shape = RoundedCornerShape(0.dp)
                )
                .clickable {
                    isShowCode = true
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_qr_code),
                    contentDescription = "",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )

                Text(
                    text = stringResource(R.string.btn_show_code),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    style = Typography.h5)
            }

        }

        if (showDialogRequestReadPermission) {
            AlertDialog(
                onDismissRequest = {
                    showDialogRequestReadPermission = false
                },
                title = {
                    Text(text = stringResource(R.string.grant_access))
                },
                text = {
                    Text(stringResource(R.string.we_need_your_permission_to_save_photos))
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
                            onClick = { showDialogRequestReadPermission = false },
                        ) {
                            Text(stringResource(R.string.btn_dismiss), color = Color.Gray)
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .align(Alignment.BottomEnd),
                            onClick = {
                                showDialogRequestReadPermission = false


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                                    val uri: Uri =
                                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                        uri))
                                } else {
                                    if (!readStoragePermissionState.status.isGranted) {
                                        readStoragePermissionState.launchPermissionRequest()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorButton
                            )
                        ) {
                            Text(stringResource(R.string.btn_allow), color = Color.White)
                        }
                    }
                }
            )
        }

    }


}

fun gotoSaveQrcode(
    context: Context,
    valueCode: String,
    coroutineScope: CoroutineScope,
    barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE,
    colorBarcode: Color = Color.White,
) {
    AppUtils.showToast(
        context,
        "Processing is in progress"
    )
    coroutineScope.launch {
        withContext(Dispatchers.IO) {
            AppUtils.saveImageQrCodeCreate(context,
                valueCode,
                barcodeFormat = barcodeFormat,
                colorBarcode)
        }
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) {
                AppUtils.showToast(
                    context,
                    "Save to: " +
                            "\"${Environment.DIRECTORY_DOCUMENTS}\""
                            + "directory in SD card"
                )
            }
        }
    }
}
