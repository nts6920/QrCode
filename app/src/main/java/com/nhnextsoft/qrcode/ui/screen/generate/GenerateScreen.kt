package com.nhnextsoft.qrcode.ui.screen.generate

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.zxing.BarcodeFormat
import com.nhnextsoft.qrcode.Constants
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.ui.screen.history.HistoryContract
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import com.nhnextsoft.qrcode.utils.FormatBarCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

data class GenerateTab(val type: Int, val name: String, val iconId: Int)

private val lightBlue = Color(0xFFD8FFFF)
private val blue = Color(0xff76a9ff)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun GenerateScreenPreview() {
    QRCodeTheme {
        GenerateScreen(
            state = GenerateContract.State(
                isLoaded = true,
            ),
            effectFlow = null,
            onEventSent = {

            },
            onNavigationRequested = {

            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GenerateScreen(
    state: GenerateContract.State,
    effectFlow: Flow<GenerateContract.Effect>?,
    onEventSent: (event: GenerateContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: GenerateContract.Effect.Navigation) -> Unit?,
) {

    val tabData = listOf(
        GenerateTab(Constants.TYPE_TEXT,
            stringResource(id = R.string.text_text),
            R.drawable.ic_text),
        GenerateTab(Constants.TYPE_WIFI,
            stringResource(id = R.string.text_wifi),
            R.drawable.ic_wifi),
        GenerateTab(Constants.TYPE_URL, stringResource(id = R.string.text_url), R.drawable.ic_url),
        GenerateTab(Constants.TYPE_EMAIL,
            stringResource(id = R.string.text_email),
            R.drawable.ic_email),
        GenerateTab(Constants.TYPE_SMS,
            stringResource(id = R.string.text_send_sms),
            R.drawable.ic_sms),
        GenerateTab(Constants.TYPE_CONTACT_INFO,
            stringResource(id = R.string.text_contact),
            R.drawable.ic_contact),
        GenerateTab(Constants.TYPE_BARCODE,
            stringResource(id = R.string.text_barcode),
            R.drawable.ic_barcode),
    )
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val titleState by remember { mutableStateOf(tabData[0].name) }

    var isShowButtonBarcodeState by remember { mutableStateOf(false) }
    val isShowContentBarcodeState by remember { mutableStateOf(false) }
    var barcodeValueState by remember { mutableStateOf("") }

    LaunchedEffect(effectFlow) {
        effectFlow?.collect { effect ->
            Timber.d(effect.toString())
            when (effect) {
                is GenerateContract.Effect.EffectGenerateCode -> {
                    Timber.d("isShowButtonBarcodeState $isShowButtonBarcodeState " +
                            "isShowContentBarcodeState $isShowContentBarcodeState " +
                            "barcodeValueState $barcodeValueState")
                    isShowButtonBarcodeState = true
                    barcodeValueState = effect.valueData
                }
                is GenerateContract.Effect.Navigation.NavigateToCreateHistory -> {
                    onNavigationRequested.invoke(effect)
                }
            }
        }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)) {

                Text(text = titleState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .animateContentSize(),
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    style = Typography.h5
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.padding(start = 0.dp, end = 0.dp),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                        color = Color.Transparent
                    )
                },
                divider = {
                    Divider(color = Color.Transparent)
                },
                backgroundColor = Color.White,
                contentColor = Color.DarkGray
            ) {
                tabData.forEachIndexed { index, pair ->
                    TabCustomRounded(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = pair.iconId,
                        selectedContentColor = ColorButton,
                        unselectedContentColor = Color.White,
                    )
                }
            }
            HorizontalPager(
                count = tabData.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 8.dp)
            ) { page ->
                when (tabData[page].type) {
                    Constants.TYPE_TEXT -> {
                        GenerateTypeText(onEventSent = onEventSent)
                    }
                    Constants.TYPE_WIFI -> {
                        GenerateTypeWifi(onEventSent = onEventSent)
                    }
                    Constants.TYPE_URL -> {
                        GenerateTypeUrl(onEventSent = onEventSent)
                    }
                    Constants.TYPE_EMAIL -> {
                        GenerateTypeEmail(onEventSent = onEventSent)
                    }
                    Constants.TYPE_SMS -> {
                        GenerateTypeSMS(onEventSent = onEventSent)
                    }
                    Constants.TYPE_CONTACT_INFO -> {
                        GenerateTypeContactInfo(onEventSent = onEventSent)
                    }
                    Constants.TYPE_BARCODE -> {
                        GenerateTypeBarcode(onEventSent = onEventSent)
                    }
                }
            }
        }
    }

}

@Composable
fun GenerateTypeBarcode(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textContentState by remember { mutableStateOf("") }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        RowInputContent(
            value = textContentState,
            label = "Content",
            required = false,
            placeholder = "Please fill in the content",
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone
            )
        ) { textFieldValue ->
            textContentState = textFieldValue
        }
        var codeData = textContentState
        if (textContentState.isNotEmpty()) {
            codeData = textContentState
            RowButtonGenerate {
                focusManager.clearFocus()
                Timber.d("RowButtonGenerate: $codeData")
                val bitmap = AppUtils.createBarcode(codeData)

                Timber.d("bitmap: ${bitmap?.width}/${bitmap?.height}")
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.CODE_128.name))

                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun GenerateTypeContactInfo(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textContentState by remember { mutableStateOf("") }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        RowInputContent(
            value = textContentState,
            label = "Content",
            required = true,
            placeholder = "Please fill in the content",
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone
            )
        ) { textFieldValue ->
            textContentState = textFieldValue
        }
        var codeData = textContentState
        if (textContentState.isNotEmpty()) {
            codeData = textContentState
            RowButtonGenerate {
                focusManager.clearFocus()
                Timber.d("RowButtonGenerate: $codeData")
                val bitmap = AppUtils.createBarCodeByBarcodeFormat(codeData,
                    barcodeFormat = BarcodeFormat.CODE_128)
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun GenerateTypeSMS(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textRecipientNumberState by remember { mutableStateOf("") }
    var textMessageState by remember { mutableStateOf("") }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        RowInputContent(
            value = textRecipientNumberState,
            label = "Recipient number",
            required = true,
            placeholder = "Please fill in the recipient number",
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone
            )
        ) { textFieldValue ->
            textRecipientNumberState = textFieldValue
        }
        RowInputContent(
            value = textMessageState,
            label = "Message",
            required = true,
            placeholder = "Please fill in the message",
        ) { textFieldValue ->
            textMessageState = textFieldValue
        }
        var codeData = ""
        if (textRecipientNumberState.isNotEmpty() && textMessageState.isNotEmpty()) {
            codeData = "SMSTO:$textRecipientNumberState:$textMessageState"

            RowButtonGenerate {
                focusManager.clearFocus()
                Timber.d("RowButtonGenerate: $codeData")
                val bitmap = AppUtils.createBarCodeByBarcodeFormat(codeData)
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun GenerateTypeEmail(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textEmailState by remember { mutableStateOf("") }
    var textSubjectState by remember { mutableStateOf("") }
    var textContentState by remember { mutableStateOf("") }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 30.dp)) {
        RowInputContent(
            value = textEmailState,
            label = "E-mail",
            required = true,
            placeholder = "Please fill in the email",
        ) { textFieldValue ->
            textEmailState = textFieldValue
        }
        RowInputContent(
            value = textSubjectState,
            label = "Subject",
            required = true,
            placeholder = "Please fill in the subject",
        ) { textFieldValue ->
            textSubjectState = textFieldValue
        }
        RowInputContent(
            value = textContentState,
            label = "Content",
            required = true,
            placeholder = "Please fill in the content",
        ) { textFieldValue ->
            textContentState = textFieldValue
        }

        var codeData = ""
        if (textEmailState.isNotEmpty() && textSubjectState.isNotEmpty() && textContentState.isNotEmpty()) {
            codeData =
                "MATMSG:TO:" + textEmailState.trim() + ";SUB:" + textSubjectState.trim() + ";BODY:" + textContentState.trim() + ";;";

            RowButtonGenerate {
                focusManager.clearFocus()
                Timber.d("RowButtonGenerate: $codeData")
                val bitmap = AppUtils.createBarCodeByBarcodeFormat(codeData)
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }

}

@Composable
fun GenerateTypeUrl(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textUrlState by remember { mutableStateOf("") }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 30.dp)) {
        RowInputContent(
            value = textUrlState,
            label = "Website",
            placeholder = "Please fill in the website address",
        ) { textFieldValue ->
            textUrlState = textFieldValue
        }

        Row(modifier = Modifier.fillMaxWidth()) {

            TextButton(
                onClick = { textUrlState += "https://" },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorButton.copy(alpha = 0.4f)
                )
            ) {
                Text(text = "https://")
            }
            TextButton(onClick = { textUrlState += "http://" },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorButton.copy(alpha = 0.4f)
                )) {
                Text(text = "http://")
            }
            TextButton(onClick = { textUrlState += ".com" },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorButton.copy(alpha = 0.4f)
                )) {
                Text(text = ".com")
            }
            TextButton(onClick = { textUrlState += "www." },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorButton.copy(alpha = 0.4f)
                )) {
                Text(text = "www.")
            }
        }
        var codeData = ""
        if (textUrlState.isNotEmpty()) {
            codeData =
                if (textUrlState.contains("https://") || textUrlState.contains("http://"))
                    textUrlState.trim()
                else "http://" + textUrlState.trim()

            RowButtonGenerate {
                focusManager.clearFocus()

                Timber.d("RowButtonGenerate: $codeData")
                val bitmap = AppUtils.createBarCodeByBarcodeFormat(codeData)
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun GenerateTypeWifi(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textSSIDState by remember { mutableStateOf("") }
    var textPasswordState by remember { mutableStateOf("") }
    var typeWifiState by remember { mutableStateOf("WPA") }
    var wifiHiddenNetworkState by remember { mutableStateOf(false) }
    val qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 30.dp)) {
        RowInputContent(
            value = textSSIDState,
            label = "Network name (SSID)",
            placeholder = "Please fill in the SSID"
        ) { textFieldValue ->
            textSSIDState = textFieldValue
        }
        RowRadioGroup(label = "Security",
            options = listOf(
                "WPA/WPA2" to "WPA",
                "WEP" to "WEP",
                "None" to "None",
            )) { type ->
            typeWifiState = type.second
        }

        AnimatedVisibility(typeWifiState != "None") {
            RowInputContent(
                value = textPasswordState,
                label = "Password",
                placeholder = "Please fill in the password"
            ) { textFieldValue ->
                textPasswordState = textFieldValue
            }

        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(8.dp),
            contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Hidden network",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 16.dp, bottom = 4.dp)
                    .align(Alignment.TopStart),
                textAlign = TextAlign.Start,
                color = Color.Black
            )
            Switch(
                checked = wifiHiddenNetworkState,
                onCheckedChange = { wifiHiddenNetworkState = !wifiHiddenNetworkState },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
            )

        }
        var codeData = ""
        if (textSSIDState.isNotEmpty()) {
            val showButtonGenerate = if (typeWifiState == "None") {
                true
            } else {
                textPasswordState.isNotEmpty()
            }
            if (showButtonGenerate) {
                codeData = if (typeWifiState == "None") {
                    "WIFI:S:${
                        textSSIDState
                            .trim()
                            .replace("\n+", " ")
                    };H:$wifiHiddenNetworkState;;"
                } else {
                    "WIFI:S:${
                        textSSIDState
                            .trim()
                            .replace("\n+", " ")
                    };P:$textPasswordState;T:$typeWifiState;H:$wifiHiddenNetworkState;;"
                }
                RowButtonGenerate {
                    focusManager.clearFocus()
                    Timber.d("RowButtonGenerate: $codeData")
                    val bitmap = AppUtils.createBarCodeByBarcodeFormat(codeData)
                    qrCodeBitmapState.value = bitmap
                }
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun GenerateTypeText(onEventSent: (event: GenerateContract.Event) -> Unit?) {
    val focusManager = LocalFocusManager.current
    var textState by remember { mutableStateOf("") }
    var qrCodeBitmapState: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 30.dp)
    ) {
        RowInputContent(
            value = textState,
            label = "Content:",
            placeholder = "Please input content"
        ) { textFieldValue ->
            textState = textFieldValue
        }

        var codeData = ""
        if (textState.isNotEmpty()) {
            codeData = textState
            RowButtonGenerate {
                focusManager.clearFocus()
                Timber.d("RowButtonGenerate: $textState")
                val bitmap = AppUtils.createBarCodeByBarcodeFormat(textState)
                qrCodeBitmapState.value = bitmap
            }
        }
        val context = LocalContext.current
        qrCodeBitmapState.value?.also {
            RowShowBarCodeImage(it) {
                onEventSent.invoke(GenerateContract.Event.EventSaveCreateCode(codeData,
                    formatBarCode = FormatBarCode.QR_CODE.name))
                AppUtils.showToast(context, "Save success")
            }
        }
    }
}

@Composable
fun RowRadioGroup(
    label: String,
    options: List<Pair<String, String>>,
    onSelectedChange: (Pair<String, String>) -> Unit,
) {
    var selectedOption by remember {
        mutableStateOf(options.first())
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = Color.Black
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(30.dp, 40.dp)
                .background(blue.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(0.33f, true)
                        .fillMaxHeight()
                        .clip(
                            shape = RoundedCornerShape(
                                size = 8.dp,
                            ),
                        )
                        .clickable {
                            selectedOption = option
                            onSelectedChange.invoke(selectedOption)
                        }
                        .background(
                            if (option.first == selectedOption.first) {
                                ColorButton
                            } else {
                                Color.Transparent
                            }, RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.first,
                        style = Typography.body1.merge(),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}


@Composable
fun RowShowBarCodeImage(bitmap: Bitmap, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(200.dp))

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorButton
            ),
            modifier = Modifier.size(100.dp, 50.dp)
        ) {
            Text(text = "Save", color = Color.White, style = Typography.button)
        }

        Spacer(modifier = Modifier.size(32.dp))
    }
}

@Composable
fun RowButtonGenerate(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorButton
            ),
            modifier = Modifier.size(100.dp, 50.dp)
        ) {
            Text(text = "Generate", color = Color.White, style = Typography.button)
        }

    }
}

@Composable
private fun RowInputContent(
    value: String,
    label: String,
    required: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    placeholder: String,
    isError: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(
            text = label + if (required) " (*)" else "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Start,
            color = Color.Black
        )
        Spacer(modifier = Modifier.size(4.dp))
        TextField(
            value = value,
            placeholder = {
                Text(text = placeholder, color = Color.LightGray)
            },
            onValueChange = {
                onValueChange.invoke(it)
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            isError = isError,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = lightBlue,
                cursorColor = Color.Black,
                disabledLabelColor = lightBlue,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
        )
    }
}

/**
 * Cusstom by LeadingIconTab
 */
@Composable
fun TabCustomRounded(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Int = 0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
) {
    val transition = updateTransition(selected, label = "TabCustomRounded")
    val color by transition.animateColor(
        transitionSpec = {
            val finiteAnimationSpec: FiniteAnimationSpec<Color> =
                if (false.isTransitioningTo(true)) tween(durationMillis = 100,
                    easing = LinearEasing) else tween(durationMillis = 100, easing = LinearEasing)
            finiteAnimationSpec
        }, label = ""
    ) {
        if (it) selectedContentColor else unselectedContentColor
    }

    // The color of the Ripple should always the selected color, as we want to show the color
    // before the item is considered selected, and hence before the new contentColor is
    // provided by TabTransition.
    val ripple = rememberRipple(bounded = true, color = selectedContentColor)

    CompositionLocalProvider(
        LocalContentColor provides color.copy(alpha = 1f),
        LocalContentAlpha provides color.alpha,
        content = {
            Column(
                modifier = modifier
                    .height(48.dp)
                    .selectable(
                        selected = selected,
                        onClick = onClick,
                        enabled = enabled,
                        role = Role.Tab,
                        interactionSource = interactionSource,
                        indication = ripple
                    )
                    .fillMaxWidth()
                    .background(color, RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = "",
                    tint = if (selected) Color.White else Color.DarkGray
                )
            }
        }
    )
}
