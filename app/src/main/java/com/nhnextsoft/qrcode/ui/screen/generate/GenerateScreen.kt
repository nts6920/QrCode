package com.nhnextsoft.qrcode.ui.screen.generate

import android.graphics.Bitmap
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.nhnextsoft.qrcode.Constants
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.AppUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

data class GenerateTab(val type: Int, val name: String, val iconId: Int)

private val lightBlue = Color(0xFFD8FFFF)


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
            stringResource(id = R.string.personal),
            R.drawable.ic_wallet),
    )
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val titleState by remember { mutableStateOf(tabData[0].name) }

    LaunchedEffect(effectFlow) {
        effectFlow?.collect { effect ->
            Timber.d(effect.toString())
            when (effect) {
                is GenerateContract.Effect.EffectGenerateCode -> {
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
                    .padding(top = 2.dp, bottom = 8.dp)
            ) { page ->
                when (tabData[page].type) {
                    Constants.TYPE_TEXT -> {
                        GenerateTypeText(onEventSent = onEventSent)
                    }
                }
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
        .padding(bottom = 40.dp)
    ) {
        RowInputContent(
            value = textState,
            label = "Address:",
            placeholder = "Please input Six Wallet!"
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
