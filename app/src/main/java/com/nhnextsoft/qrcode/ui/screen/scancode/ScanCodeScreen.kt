package com.nhnextsoft.qrcode.ui.screen.scancode

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.view.Surface.ROTATION_0
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.*
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.base.LAUNCH_LISTEN_FOR_EFFECTS
import com.nhnextsoft.qrcode.getCameraProvider
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.QRCodeTheme
import com.nhnextsoft.qrcode.ui.theme.Typography
import com.nhnextsoft.qrcode.utils.VibratorHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.camera.core.Preview as CameraPreview

@OptIn(ExperimentalPermissionsApi::class)
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ScanCodeScreenPreview() {
    QRCodeTheme {
        ScanCodeScreen(
            state = ScanCodeContract.State(),
            effectFlow = null,
            onEventSent = {

            },
            onNavigationRequested = {

            }
        )
    }
}

@ExperimentalPermissionsApi
@Composable
fun ScanCodeScreen(
    state: ScanCodeContract.State,
    effectFlow: Flow<ScanCodeContract.Effect>?,
    onEventSent: (event: ScanCodeContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: ScanCodeContract.Effect.Navigation) -> Unit?,
) {

    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            ScanCodeScreenView(state = state,
                effectFlow = effectFlow,
                onEventSent = onEventSent,
                onNavigationRequested = onNavigationRequested)
        }
        is PermissionStatus.Denied -> {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                    stringResource(R.string.permission_should_show_rationale)
                } else {
                    stringResource(R.string.permission_no_should_show_rationale)
                }
                Spacer(modifier = Modifier.size(50.dp))
                Card(modifier = Modifier.size(150.dp), shape = CircleShape, elevation = 8.dp) {
                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_photo_camera),
                        contentDescription = "",
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        tint = Color.LightGray
                    )
                }
                Text(
                    text = stringResource(R.string.text_camera),
                    modifier = Modifier.fillMaxWidth(0.6f),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                    style = Typography.h4
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = textToShow,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    style = Typography.body1
                )
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorButton,
                )) {
                    Text(text = stringResource(R.string.btn_allow),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = Typography.body1
                    )
                }

            }
        }
    }

}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanCodeScreenView(
    state: ScanCodeContract.State,
    effectFlow: Flow<ScanCodeContract.Effect>?,
    onEventSent: (event: ScanCodeContract.Event) -> Unit?,
    onNavigationRequested: (navigationEffect: ScanCodeContract.Effect.Navigation) -> Unit?,
) {
    val context = LocalContext.current
    val readStoragePermissionState =
        rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)
    var showDialogRequestReadPermission by remember { mutableStateOf(false) }
    var firstScan = false
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        Timber.d(imageUri.toString())

        val qrCodeAnalyzerImagePicker = QRCodeAnalyzer(context = context) { result, path ->
            if (state.settingVibrate && !firstScan) {
                firstScan = true
                VibratorHelper.shakeItBaby(context)
            }
            onEventSent.invoke(ScanCodeContract.Event.OnScanResult(result))
        }
        imageUri?.let { qrCodeAnalyzerImagePicker.analyzeImage(it) }

    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraOnOffTorch by remember { mutableStateOf(false) }


    val qrCodeAnalyzer = QRCodeAnalyzer(context) { result, path ->
        Timber.d("state.settingVibrate ${state.settingVibrate}")
        if (state.settingVibrate && !firstScan) {
            firstScan = true
            VibratorHelper.shakeItBaby(context)
        }
        onEventSent.invoke(ScanCodeContract.Event.OnScanResult(result))
    }

    LaunchedEffect(LAUNCH_LISTEN_FOR_EFFECTS) {
        effectFlow?.onEach { effect ->
            when (effect) {
                is ScanCodeContract.Effect.Navigation.NavigateToScanResult -> onNavigationRequested(
                    effect)
            }
        }?.collect()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        CameraContentView(
            lensFacing = lensFacing,
            hasTorch = cameraOnOffTorch,
            qrCodeAnalyzer = qrCodeAnalyzer
        )
        FloatingActionButton(
            onClick = {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                Timber.d("FloatingActionButton cameraSelectorLensFacingBACK $lensFacing")
                cameraOnOffTorch = false
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            backgroundColor = Color.Transparent,
            contentColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(1.dp)
        ) {
            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_camera_reverse),
                tint = Color.White,
                contentDescription = "")
        }
        if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            FloatingActionButton(
                onClick = {
                    cameraOnOffTorch = !cameraOnOffTorch
                    Timber.d("FloatingActionButton cameraOnOffTorch $cameraOnOffTorch")
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp, 10.dp, 70.dp, 10.dp),
                backgroundColor = Color.Transparent,
                contentColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_flash),
                    tint = Color.White,
                    contentDescription = "")
            }
        }
        ScanCodeTip(modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(top = 30.dp)) {
            if (readStoragePermissionState.status.isGranted) {
                showDialogRequestReadPermission = false
                launcher.launch("image/*")
            } else showDialogRequestReadPermission = true

        }

        ScannerFrameComposable(modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxSize(0.8F)
            .padding(top = 90.dp)
        )

        if (showDialogRequestReadPermission) {
            AlertDialog(
                onDismissRequest = {
                    showDialogRequestReadPermission = false
                },
                title = {
                    Text(text = stringResource(R.string.grant_access))
                },
                text = {
                    Text(stringResource(R.string.we_need_your_permission_to_select_photos))
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
                                if (!readStoragePermissionState.status.isGranted) {
                                    readStoragePermissionState.launchPermissionRequest()
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

@Composable
fun ScanCodeTip(modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier) {
        Text(text = "Place QR Code inside area",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            color = Color.White,
            style = Typography.body1,
            textAlign = TextAlign.Center
        )

        Text(text = "Scanning will start automatically",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            color = Color.Gray,
            style = Typography.body2,
            textAlign = TextAlign.Center)

        Button(onClick = { onClick.invoke() },
            modifier = Modifier
                .size(80.dp, 40.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Gray.copy(0.4f),
                backgroundColor = Color.Gray.copy(0.4f)
            )
        ) {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_image_gallery),
                contentDescription = null,
//                modifier = Modifier
//                    .width(40.dp),
                tint = Color.White)
        }
    }
}

@Composable
fun CameraContentView(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    hasTorch: Boolean = false,
    imageAnalysis: ImageAnalysis? = null,
    imageCapture: ImageCapture? = null,
    qrCodeAnalyzer: QRCodeAnalyzer? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // use @remember to persists lifcycle
//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val screenAspectRatio: Int = aspectRatio(context)
    val previewView = remember { PreviewView(context) }
    val preview = androidx.camera.core.Preview.Builder()
        .setTargetAspectRatio(screenAspectRatio)
        .setTargetRotation(ROTATION_0)
        .build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()
    val mImageAnalysis: ImageAnalysis = imageAnalysis
        ?: ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(ROTATION_0)
            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(1)
            .build()
    qrCodeAnalyzer?.let { analyzer ->
        mImageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
    }

    LaunchedEffect(lensFacing, hasTorch) {
        val cameraProvider = context.getCameraProvider()
        val camera = bindToCamera(
            cameraProvider = cameraProvider,
            lifecycleOwner = lifecycleOwner,
            preview = preview,
            cameraSelector = cameraSelector,
            imageAnalysis = mImageAnalysis,
            imageCapture = imageCapture
        )
        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(hasTorch)
        }
    }
    AndroidView({ previewView }, modifier = modifier.fillMaxSize())
}

private fun bindToCamera(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    preview: CameraPreview,
    cameraSelector: CameraSelector,
    imageAnalysis: ImageAnalysis? = null,
    imageCapture: ImageCapture? = null,
): Camera {

    cameraProvider.unbindAll()
    val useCases = ArrayList<UseCase>()
    imageAnalysis?.let { analysis ->
        useCases.add(analysis)
    }
    imageCapture?.let { capture ->
        useCases.add(capture)
    }
    return cameraProvider.bindToLifecycle(
        lifecycleOwner, cameraSelector, preview, *useCases.toTypedArray())
}

private fun aspectRatio(context: Context): Int {
    val ratioValue43 = 4.0 / 3.0
    val ratioValue169 = 16.0 / 9.0
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val width: Int
    val height: Int

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = wm.currentWindowMetrics
        val windowInsets: WindowInsets = windowMetrics.windowInsets
        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        val b = windowMetrics.bounds
        width = b.width() - insetsWidth
        height = b.height() - insetsHeight
    } else {
        val size = Point()
        val display = wm.defaultDisplay
        display?.getSize(size)
        width = size.x
        height = size.y
    }
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - ratioValue43) <= abs(previewRatio - ratioValue169)) {
        return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
}