package com.nhnextsoft.qrcode.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.play.core.review.ReviewManagerFactory
import com.nhnextsoft.qrcode.BuildConfig
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.getActivity
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.ColorMain
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import kotlinx.coroutines.flow.Flow


@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    QRCodeTheme {
        SettingScreen(
            state = SettingContract.State(),
            effectFlow = null,
            onEventSent = {

            },
            onNavigationRequested = {

            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingScreen(
    state: SettingContract.State,
    effectFlow: Flow<SettingContract.Effect>?,
    onEventSent: (event: SettingContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: SettingContract.Effect.Navigation) -> Unit?,
) {
    val context = LocalContext.current
    Column(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(color = Color.White)
        .padding(start = 16.dp, end = 16.dp)
    )
    {

        var vibrateState by remember { mutableStateOf(state.settingVibrate) }
        var autoFocusCameraState by remember { mutableStateOf(state.settingAutoFocusCamera) }
        var openUrlAutomaticallyState by remember { mutableStateOf(state.settingOpenUrlAutomatically) }

        Text(text = stringResource(R.string.screen_settings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 24.dp, bottom = 24.dp),
            style = Typography.h5,
            fontWeight = FontWeight.Bold,
            color = Color.Black)

        SettingRowHeader(stringResource(R.string.general))


        SettingRowSwitch(
            settingName = stringResource(R.string.vibrate),
            iconIdResource = R.drawable.ic_vibrate,
            switchState = vibrateState
        ) {
            vibrateState = !vibrateState

            onEventSent.invoke(SettingContract.Event.SettingVibrateChange(vibrateState))
        }

        SettingRowSwitch(
            settingName = stringResource(R.string.autofocus),
            iconIdResource = R.drawable.ic_outline_camera,
            switchState = autoFocusCameraState
        ) {
            autoFocusCameraState = !autoFocusCameraState
            onEventSent.invoke(SettingContract.Event.SettingAutoFocusCameraChange(
                autoFocusCameraState))
        }

        SettingRowSwitch(
            settingName = stringResource(R.string.open_url_automatically),
            iconIdResource = R.drawable.ic_link_open,
            switchState = openUrlAutomaticallyState
        ) {
            openUrlAutomaticallyState = !openUrlAutomaticallyState
            onEventSent.invoke(SettingContract.Event.SettingOpenUrlAutomaticallyChange(
                openUrlAutomaticallyState))
        }
//        SettingRowNoSwitch(
//            modifier = Modifier
//                .fillMaxWidth()
//                .requiredHeight(50.dp)
//                .padding(8.dp),
//            settingName = stringResource(R.string.language),
//            iconIdResource = R.drawable.ic_web,
//        ) {
//
//        }
        SettingRowHeader(stringResource(R.string.Other))

        SettingRowNoSwitch(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(50.dp)
                .padding(8.dp),
            settingName = stringResource(R.string.share_app),
            iconIdResource = R.drawable.ic_share,
        ) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
            sendIntent.type = "text/plain"
            context.startActivity(sendIntent)
        }
        SettingRowNoSwitch(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(50.dp)
                .padding(8.dp),
            settingName = stringResource(R.string.rate_us),
            iconIdResource = R.drawable.ic_like,
        ) {
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = context.getActivity()
                        ?.let { manager.launchReviewFlow(it, reviewInfo) }
                    flow?.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        AppUtils.showToast(context, "Thank you")
                    }
                } else {
                    // There was some problem, continue regardless of the result.
                    // you can show your own rate dialog alert and redirect user to your app page
                    // on play store.
                    try {
                        val url =
                            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {

                    }
                }
            }
        }

        SettingRowNoSwitch(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(50.dp)
                .padding(8.dp),
            settingName = stringResource(R.string.policy),
            iconIdResource = R.drawable.ic_policy,
        ) {
            val url = "https://sites.google.com/view/taptapstudio/privacy-Policy"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
    }
}

@Composable
fun SettingRowHeader(textHeader: String) {
    Text(text = textHeader,
        modifier = Modifier.padding(start = 8.dp),
        style = Typography.h5.copy(textDecoration = TextDecoration.Underline),
        fontWeight = FontWeight.Bold,
        color = Color.Black)
}

@Composable
private fun SettingRowSwitch(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .requiredHeight(50.dp)
        .padding(8.dp),
    settingName: String,
    iconIdResource: Int,
    switchState: Boolean = true,
    onClick: (Boolean) -> Unit,
) {
    Row(modifier) {
        SettingRowNoSwitch(
            modifier = Modifier
                .weight(1f),
            settingName = settingName,
            iconIdResource = iconIdResource) {
            onClick.invoke(!switchState)
        }
        Switch(checked = switchState,
            onCheckedChange = {
                onClick.invoke(it)
            },
            Modifier
                .fillMaxHeight()
                .padding(end = 16.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorButton,
                checkedTrackColor = ColorButton,
                uncheckedThumbColor = ColorMain,
                uncheckedTrackColor = ColorMain
            )
        )
    }
}

@Composable
fun SettingRowNoSwitch(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .requiredHeight(50.dp)
        .padding(8.dp),
    settingName: String,
    iconIdResource: Int,
    onClick: () -> Unit,
) {
    Row(modifier) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconIdResource),
            "",
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(start = 4.dp)
        )
        Surface(modifier = Modifier
            .padding(start = 4.dp)
            .fillMaxSize()
            .wrapContentHeight()
            .clipToBounds()
            .clickable {
                onClick.invoke()
            }) {

            Text(
                text = settingName,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp)
                    .wrapContentHeight(),
                style = Typography.body2,
            )
        }
    }
}
