package com.nhnextsoft.qrcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.nhnextsoft.qrcode.ui.feature.entry.QrEntryPointCodeActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("", text))
}

fun String.toColor(): Color {
    return Color(
        when {
            this.contains("0xFF") -> android.graphics.Color.parseColor("#" + this.removePrefix("0xFF"))
            this.contains("#") -> android.graphics.Color.parseColor(this)
            else -> android.graphics.Color.parseColor("#$this")
        }
    )
}

fun Int.toHexColor(): String {
    return String.format("#%08X", -0x1 and this)
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Context.isGooglePlayServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
    return resultCode == ConnectionResult.SUCCESS
}

fun Context.isFacebookAppInstalled(): Boolean {
    return try {
        this.packageManager.getApplicationInfo(packageNameFacebookApp, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.verifyInstallerIdSourceGooglePlay(): Boolean { // A list with valid installers package name
    // The package name of the app that has installed your app
    val installer =
        this.packageManager.getInstallerPackageName(this.packageName)
    // true if your app has been downloaded from Play Store
    return installer != null && validInstallersByGooglePlay.contains(installer)
}

private val validInstallersByGooglePlay: MutableList<String> =
    mutableListOf("com.android.vending", "com.google.android.feedback")
private const val packageNameFacebookApp: String = "com.facebook.katana"

@ExperimentalAnimationApi
fun Context.getActivity(): QrEntryPointCodeActivity? = when (this) {
    is QrEntryPointCodeActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
