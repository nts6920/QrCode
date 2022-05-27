package com.nhnextsoft.qrcode.ui.screen.scanresult

import android.content.Intent
import android.net.Uri
import android.telecom.Call
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.zxing.client.result.*
import com.nhnextsoft.qrcode.Constants
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.base.LAUNCH_LISTEN_FOR_EFFECTS
import com.nhnextsoft.qrcode.db.entity.HistoryScan
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import com.nhnextsoft.qrcode.utils.FormatBarCode
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber
import java.io.File
import java.util.*


@Preview(showBackground = true)
@Composable
fun ScanResultScreenPreview() {
    val valueCode = "sms:+15551212?subject=foo&body=bar"
    val parsedResult = FormatBarCode.ofParsedResult(valueCode, "QR Code")
    QRCodeTheme {
        ScanResultScreen(
            state = ScanResultContract.State(false,
                HistoryScan(id = 1,
                    valueCode = valueCode,
                    date = Date(),
                    format = "Qr code"
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

@Composable
fun ScanResultScreen(
    state: ScanResultContract.State,
    effectFlow: Flow<ScanResultContract.Effect>?,
    onEventSent: (event: ScanResultContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: ScanResultContract.Effect.Navigation) -> Unit?,
) {
    val context = LocalContext.current
    LaunchedEffect(LAUNCH_LISTEN_FOR_EFFECTS) {
        effectFlow?.collect { effect ->
            when (effect) {
                is ScanResultContract.Effect.Navigation.NavigateToScanNow -> onNavigationRequested(
                    effect)
            }
        }
    }
    var isCheckOpenUrl by remember {
        mutableStateOf(false)
    }
    val historyScan = state.historyScan
    val parsedResult = state.parsedResult
    val isOpenUrlAutomatically = state.isSettingOpenUrlAutomatically
    Timber.d("isCheckOpenUrl $isCheckOpenUrl parsedResult $parsedResult, isOpenUrlAutomatically $isOpenUrlAutomatically")
    when {
        parsedResult != null -> {
            if (isOpenUrlAutomatically && parsedResult is URIParsedResult && !isCheckOpenUrl) {
                isCheckOpenUrl = true
                Timber.d("parsedResult.uri ${parsedResult.uri}")
                val url = parsedResult.uri
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                AppUtils.showToast(context, R.string.btn_open_web)
                context.startActivity(intent)
            }
        }
    }



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
                text = stringResource(R.string.title_scan_result),
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
        }

        Column(
            Modifier
                .fillMaxSize()
                .constrainAs(bodyContent) {
                    top.linkTo(headerContent.bottom)
                    bottom.linkTo(bodyContent.top)
                }
                .padding()
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
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                    if (historyScan != null && parsedResult != null) {
                        val formatBarCode = FormatBarCode.ofToFormatBarCode(historyScan.format)
                        ScanResultContent(
                            valueCode = historyScan.valueCode,
                            prBarCode = parsedResult,
                            formatBarCode = formatBarCode,
                            historyScan.date,
                            showImageResult = state.isShowImage,
                            colorBarcode = Color.White,
                            onEventSent)

                    } else Text(
                        text = "Error, Please try again",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .constrainAs(bottomContent) {
                    bottom.linkTo(parent.bottom)
                }
                .background(ColorButton.copy(blue = 1f))
                .clickable {
                    onEventSent.invoke(ScanResultContract.Event.OnClickBottomScanNow)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                    contentDescription = "",
                    modifier = Modifier.size(32.dp), tint = Color.White
                )

                Text(
                    text = stringResource(R.string.btn_scan_now),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    style = Typography.h5)
            }
        }
    }
}

@Composable
fun ScanResultContent(
    valueCode: String,
    prBarCode: ParsedResult,
    formatBarCode: FormatBarCode,
    lastDate: Date,
    showImageResult: Boolean = false,
    colorBarcode: Color = Color.White,
    onEventSent: (event: ScanResultContract.Event) -> Unit?,
) {

//    Column(Modifier
//        .fillMaxWidth()
//        .height()
//        .padding(16.dp)
//        .verticalScroll(rememberScrollState())
//    ) {
    ScanResultContentHeader(prBarCode = prBarCode,
        formatBarCode = formatBarCode,
        lastDate = lastDate)

    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(8.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(24.dp))

    ScanResultContentInfo(
        valueCode = valueCode,
        prBarCode = prBarCode,
        formatBarCode = formatBarCode,
        colorBarcode = colorBarcode
    )
    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(4.dp))

    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(8.dp))
    Timber.d("showImageResult a $showImageResult")
    if (showImageResult) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            val content = LocalContext.current
            val pathImage =
                if (LocalInspectionMode.current) "" else content.cacheDir.absolutePath + "/" + Constants.barcodeDetectorImage

            Spacer(modifier = Modifier.size(18.dp))
            if (pathImage != "") {
                Card(
                    modifier = Modifier
                        .size(125.dp)
                        .padding(12.dp)
                        .background(Color.DarkGray),
                    backgroundColor = Color.White.copy(alpha = 0.3f),
                    elevation = 8.dp
                ) {
                    Image(
                        painter = rememberImagePainter(
                            request = ImageRequest.Builder(LocalContext.current)
                                .data(File(pathImage).toUri())
                                .memoryCachePolicy(policy = CachePolicy.ENABLED)
                                .diskCachePolicy(policy = CachePolicy.DISABLED)
                                .crossfade(true)
                                .build(),
                        ),
                        contentDescription = "",
                        modifier = Modifier.size(120.dp),
                    )

                }
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp))
        }
    }
//    }
}

@Composable
fun ScanResultContentHeader(prBarCode: ParsedResult, formatBarCode: FormatBarCode, lastDate: Date) {
    var iconResourceId = R.drawable.ic_barcode
    var textValueCode = stringResource(R.string.text_barcode)
    if (formatBarCode == FormatBarCode.QR_CODE) {
        when (prBarCode) {
            is TelParsedResult -> {
                iconResourceId = R.drawable.ic_phone
                textValueCode = stringResource(R.string.text_phone)
            }
            is TextParsedResult -> {
                iconResourceId = R.drawable.ic_text
                textValueCode = stringResource(R.string.text_text)
            }
            is WifiParsedResult -> {
                iconResourceId = R.drawable.ic_wifi
                textValueCode = stringResource(R.string.text_wifi)
            }
            is URIParsedResult -> {
                iconResourceId = R.drawable.ic_web
                textValueCode = stringResource(R.string.text_url)
            }
            is SMSParsedResult -> {
                iconResourceId = R.drawable.ic_send_sms
                textValueCode = stringResource(R.string.text_send_sms)
            }
            is EmailAddressParsedResult -> {
                iconResourceId = R.drawable.ic_send_email
                textValueCode = stringResource(R.string.text_email)
            }
            is GeoParsedResult -> {
                iconResourceId = R.drawable.ic_geo
                textValueCode = stringResource(R.string.text_geo)
            }
            is AddressBookParsedResult -> {
                iconResourceId = R.drawable.ic_contact
                textValueCode = stringResource(R.string.text_contact)
            }
            is CalendarParsedResult -> {
                iconResourceId = R.drawable.ic_calendar
                textValueCode = stringResource(R.string.text_calendar)
            }
            else -> {
                iconResourceId = R.drawable.ic_text
                textValueCode = stringResource(R.string.text_text)
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)) {
        Column(modifier = Modifier.size(60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = ImageVector.vectorResource(id = iconResourceId),
                contentDescription = "",
                modifier = Modifier.size(40.dp),
                tint = ColorButton)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = textValueCode,
                modifier = Modifier.fillMaxWidth(),
                color = ColorButton,
                style = Typography.h5)
            val formatDate = AppUtils.formatDate(lastDate)
            Text(text = formatDate,
                modifier = Modifier.fillMaxWidth(),
                style = Typography.body1)
        }
    }
}

@Composable
fun ScanResultContentInfo(
    valueCode: String,
    prBarCode: ParsedResult,
    formatBarCode: FormatBarCode,
    colorBarcode: Color,
) {
    when (prBarCode) {
        is TelParsedResult -> ContentInfoTel(valueCode = valueCode,
            telParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is TextParsedResult -> ContentInfoText(id = "id = 3", valueCode = valueCode, textParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is WifiParsedResult -> ContentInfoWifi(valueCode = valueCode,
            wifiParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is URIParsedResult -> ContentInfoUrl(valueCode = valueCode,
            uriParsedResult = prBarCode, formatBarCode = formatBarCode)
        is SMSParsedResult -> ContentInfoSms(valueCode = valueCode,
            smsParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is EmailAddressParsedResult -> ContentInfoEmail(valueCode = valueCode,
            emailAddressParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is GeoParsedResult -> ContentInfoGeo(valueCode = valueCode,
            geoParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is AddressBookParsedResult -> ContentInfoAddressBook(valueCode = valueCode,
            addressBookParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        is CalendarParsedResult -> ContentInfoCalendar(valueCode = valueCode,
            calendarParsedResult = prBarCode,
            formatBarCode = formatBarCode)
        else -> ContentInfoText(id = "1",valueCode = valueCode,
            displayResult = prBarCode.displayResult,
            formatBarCode = formatBarCode)
    }
}

@Composable
fun ContentInfoCalendar(
    valueCode: String,
    calendarParsedResult: CalendarParsedResult,
    formatBarCode: FormatBarCode,
) {
    calendarParsedResult.summary?.let {
        ContentInfoRow(headerText = "Summary", contentText = it)
    }
    calendarParsedResult.description?.let {
        ContentInfoRow(headerText = "Description", contentText = it)
    }
    calendarParsedResult.organizer?.let {
        ContentInfoRow(headerText = "Organizer",
            contentText = it)
    }
    calendarParsedResult.startTimestamp.let {
        if (it > 0) {
            val startDate = AppUtils.formatDate(Date(it))
            ContentInfoRow(headerText = "Start", contentText = startDate)
        }
    }
    calendarParsedResult.endTimestamp.let {
        if (it > 0) {
            val endDate = AppUtils.formatDate(Date(it))
            ContentInfoRow(headerText = "End", contentText = endDate)
        }
    }

    calendarParsedResult.location?.let { ContentInfoRow(headerText = "Location", contentText = it) }
    calendarParsedResult.attendees?.let {
        ContentInfoRow(headerText = "Attendees",
            contentText = it.joinToString(separator = ", "))

    }
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventEventAddCalendar(valueCode, formatBarCode),
        ScanResultContract.Event.EventSearchWeb(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)

}

@Composable
fun ContentInfoRow(headerText: String, contentText: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$headerText:",
            fontWeight = FontWeight.Bold,
            style = Typography.body2)
        Text(text = contentText,
            modifier = Modifier
                .padding(start = 8.dp),
            maxLines = 2,
            style = Typography.body2)
    }
}

@Composable
fun ContentInfoAddressBook(
    valueCode: String,
    addressBookParsedResult: AddressBookParsedResult,
    formatBarCode: FormatBarCode,
) {
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
    )
    if (addressBookParsedResult.names.isNullOrEmpty().not()) {
        ContentInfoRow("Name", addressBookParsedResult.names.first())
    }
    if (addressBookParsedResult.title.isNullOrEmpty().not()) {
        ContentInfoRow("Job title", addressBookParsedResult.title)
    }
    if (addressBookParsedResult.org.isNullOrEmpty().not()) {
        ContentInfoRow("Company", addressBookParsedResult.org)
    }
    if (addressBookParsedResult.addresses.isNullOrEmpty().not()) {
        ContentInfoRow("Addresses", addressBookParsedResult.addresses.first())
    }
    if (addressBookParsedResult.phoneNumbers.isNullOrEmpty().not()) {
        ContentInfoRow("Phone", addressBookParsedResult.phoneNumbers.first())
        events.add(ScanResultContract.Event.EventAddContact(valueCode, formatBarCode))
        events.add(ScanResultContract.Event.EventCallContact(valueCode, formatBarCode))
        events.add(ScanResultContract.Event.EventSendSMS(valueCode, formatBarCode))
    }
    if (addressBookParsedResult.emails.isNullOrEmpty().not()) {
        ContentInfoRow("Email", addressBookParsedResult.emails.first())
        events.add(ScanResultContract.Event.EventEmail(valueCode, formatBarCode))
    }
    if (addressBookParsedResult.note.isNullOrEmpty().not()) {
        ContentInfoRow("Note", addressBookParsedResult.note)
    }

    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))

    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoGeo(
    valueCode: String,
    geoParsedResult: GeoParsedResult,
    formatBarCode: FormatBarCode,
) {
    if (geoParsedResult.latitude > 0) {
        ContentInfoRow("Latitude", geoParsedResult.latitude.toString())
    }
    if (geoParsedResult.longitude > 0) {
        ContentInfoRow("Longitude", geoParsedResult.longitude.toString())
    }
    if (geoParsedResult.altitude > 0) {
        ContentInfoRow("Altitude", geoParsedResult.altitude.toString())
    }
    if (geoParsedResult.query.isNullOrEmpty().not()) {
        ContentInfoRow("query", geoParsedResult.query)
    }
    if (geoParsedResult.geoURI.isNullOrEmpty().not()) {
        ContentInfoRow("Geo", geoParsedResult.geoURI)
    }

    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventOpenGeo(valueCode, formatBarCode),
        ScanResultContract.Event.EventSearchWeb(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoEmail(
    valueCode: String,
    emailAddressParsedResult: EmailAddressParsedResult,
    formatBarCode: FormatBarCode,
) {
    if (emailAddressParsedResult.tos.isNullOrEmpty().not()) {
        ContentInfoRow("To", emailAddressParsedResult.tos.joinToString(","))
    }
    if (emailAddressParsedResult.cCs.isNullOrEmpty().not()) {
        ContentInfoRow("Cc", emailAddressParsedResult.cCs.joinToString(","))
    }
    if (emailAddressParsedResult.bcCs.isNullOrEmpty().not()) {
        ContentInfoRow("Bcc", emailAddressParsedResult.bcCs.joinToString(","))
    }
    if (emailAddressParsedResult.body.isNullOrEmpty().not()) {
        ContentInfoRow("Content", emailAddressParsedResult.body)
    }
    if (emailAddressParsedResult.subject.isNullOrEmpty().not()) {
        ContentInfoRow("Subject", emailAddressParsedResult.subject)
    }
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventEmail(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoSms(
    valueCode: String,
    smsParsedResult: SMSParsedResult,
    formatBarCode: FormatBarCode,
) {
    if (smsParsedResult.numbers.isNullOrEmpty().not()) {
        ContentInfoRow("Phone", smsParsedResult.numbers.first())
    }
    if (smsParsedResult.body.isNullOrEmpty().not()) {
        ContentInfoRow("Content", smsParsedResult.body)
    }
    if (smsParsedResult.subject.isNullOrEmpty().not()) {
        ContentInfoRow("Subject", smsParsedResult.subject)
    }
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventSendSMS(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoUrl(
    valueCode: String,
    uriParsedResult: URIParsedResult,
    formatBarCode: FormatBarCode,
) {
    if (uriParsedResult.title.isNullOrEmpty().not()) {
        ContentInfoRow("Title", uriParsedResult.title)
    }
    ContentInfoRow("Url", uriParsedResult.uri)
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventOpenWeb(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoWifi(
    valueCode: String,
    wifiParsedResult: WifiParsedResult,
    formatBarCode: FormatBarCode,
) {
    ContentInfoRow("SSID", wifiParsedResult.ssid)
    ContentInfoRow("Security", wifiParsedResult.networkEncryption)
    ContentInfoRow("Password", wifiParsedResult.password)
    ContentInfoRow("Hidden", wifiParsedResult.isHidden.toString())
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopyPassWifi(valueCode, formatBarCode),
        ScanResultContract.Event.EventConnectWifi(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun ContentInfoTel(
    valueCode: String,
    telParsedResult: TelParsedResult,
    formatBarCode: FormatBarCode,
) {
    Text(text = telParsedResult.number,
        modifier = Modifier.fillMaxWidth(),
        style = Typography.body1)
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventAddContact(valueCode, formatBarCode),
        ScanResultContract.Event.EventCallContact(valueCode, formatBarCode),
        ScanResultContract.Event.EventSendSMS(valueCode, formatBarCode),
        ScanResultContract.Event.EventSearchWeb(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}


/**
 * type text
 */
@Composable
fun ContentInfoText(
    id: String,
    textParsedResult: TextParsedResult,
    formatBarCode: FormatBarCode,
    valueCode: String,

) {
    ContentInfoText(id,valueCode, textParsedResult.text, formatBarCode)
}

@Composable
fun ContentInfoText(
    id: String,
    valueCode: String,
    displayResult: String,
    formatBarCode: FormatBarCode,
) {
    Text(text = displayResult,
        modifier = Modifier.fillMaxWidth(),
        style = Typography.body1)
    Text(text = id,
        modifier = Modifier.fillMaxWidth(),
        style = Typography.body1)
    Spacer(modifier = Modifier.size(24.dp))
    Divider(
        modifier = Modifier
            .fillMaxWidth(),
        color = ColorButton,
        thickness = 0.5.dp,
    )
    Spacer(modifier = Modifier.size(16.dp))
    val events = mutableListOf(
        ScanResultContract.Event.EventShare(valueCode, formatBarCode),
        ScanResultContract.Event.EventCopy(valueCode, formatBarCode),
        ScanResultContract.Event.EventSearchWeb(valueCode, formatBarCode),
    )
    RowActionEvent(listEvents = events)
}

@Composable
fun RowActionEvent(
    listEvents: List<ScanResultContract.Event>,
) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(items = listEvents, itemContent = { event ->
            ItemEventCardBox(event)
        })
    }
}

@Composable
fun ItemEventCardBox(
    event: ScanResultContract.Event,
) {

    val context = LocalContext.current
    when (event) {
        is ScanResultContract.Event.EventShare -> ItemEventCardBoxContent(R.drawable.ic_share,
            stringResource(R.string.btn_share)) {
            AppUtils.eventShare(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventCopy -> ItemEventCardBoxContent(R.drawable.ic_copy,
            stringResource(R.string.btn_copy)) {
            AppUtils.eventCopy(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventCopyPassWifi -> ItemEventCardBoxContent(R.drawable.ic_copy,
            stringResource(R.string.btn_copy_password)) {
            AppUtils.eventCopyPassword(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventConnectWifi -> ItemEventCardBoxContent(R.drawable.ic_wifi,
            stringResource(R.string.btn_connect_wifi)) {
            AppUtils.eventConnectWifi(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventEmail -> ItemEventCardBoxContent(R.drawable.ic_send_email,
            stringResource(R.string.btn_send_email)) {
            AppUtils.eventEmail(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventAddContact -> ItemEventCardBoxContent(R.drawable.ic_add_contact,
            stringResource(R.string.btn_add_contact)) {
            AppUtils.eventAddContact(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventCallContact -> ItemEventCardBoxContent(R.drawable.ic_call,
            stringResource(R.string.btn_call)) {
            AppUtils.eventCallContact(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventSendSMS -> ItemEventCardBoxContent(R.drawable.ic_send_sms,
            stringResource(R.string.btn_send_sms)) {
            AppUtils.eventSendSMS(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventOpenWeb -> ItemEventCardBoxContent(R.drawable.ic_web,
            stringResource(R.string.btn_open_web)) {
            AppUtils.eventOpenWeb(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventSearchWeb -> ItemEventCardBoxContent(R.drawable.ic_open_search,
            stringResource(R.string.btn_search)) {
            AppUtils.eventSearchWeb(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventOpenGeo -> ItemEventCardBoxContent(R.drawable.ic_geo,
            stringResource(R.string.btn_search)) {
            AppUtils.eventOpenGeo(context, event.valueQrCode, event.format)
        }
        is ScanResultContract.Event.EventEventAddCalendar -> ItemEventCardBoxContent(R.drawable.ic_calendar,
            stringResource(R.string.btn_add_event_calendar)) {
            AppUtils.eventEventAddCalendar(context, event.valueQrCode, event.format)
        }
        else -> ItemEventCardBoxContent(R.drawable.ic_share, stringResource(R.string.btn_share)) {

        }
    }

}

@Composable
fun ItemEventCardBoxContent(iconResource: Int, itemText: String, onClick: () -> Unit) {
    Card(modifier = Modifier
        .size(70.dp, 80.dp)
        .indication(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(radius = 70.dp, bounded = false)
        )
        .clickable {
            onClick.invoke()
        },
        shape = CircleShape,
        backgroundColor = Color.Transparent,
        contentColor = Color.Transparent,
        elevation = 0.dp) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier
                    .then(Modifier.size(32.dp))
                    .background(ColorButton.copy(alpha = 0.3f), shape = CircleShape),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = ImageVector.vectorResource(id = iconResource),
                    modifier = Modifier.size(18.dp),
                    contentDescription = "",
                    tint = ColorButton)
            }
            Text(text = itemText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                maxLines = 2,
                style = Typography.body2.copy(fontSize = 12.sp))
        }
    }
}
