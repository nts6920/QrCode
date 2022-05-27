package com.nhnextsoft.qrcode.ui.screen.scancode

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nhnextsoft.qrcode.rotate
import com.nhnextsoft.qrcode.utils.AppUtils
import com.nhnextsoft.qrcode.utils.toBitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt


class QRCodeAnalyzer(
    private val context: Context,
    private val onQRCodeScanned: (barcode: Barcode, pathImage: String?) -> Unit,
) :
    ImageAnalysis.Analyzer {
    var currentTimestamp: Long = 0

    var isFirstBarcode = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        currentTimestamp = System.currentTimeMillis()
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image =
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()
        val width = mediaImage.width.coerceAtMost(mediaImage.height)
        val height = mediaImage.width.coerceAtLeast(mediaImage.height)

        Timber.d(" mediaImage ${width}/${height} - ${mediaImage.width}/${mediaImage.height}")
        val left = (width - (width * 0.9f)) / 2
        val top = (height - (width * 0.9f)) / 2
        val right = (left + width * 0.9f)
        val bottom = (top + height * 0.9f)
        val rectWithinScanArea =
            Rect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
        val scanner = BarcodeScanning.getClient(options)
        scanner.process(image)
            .addOnSuccessListener { barcodes: List<Barcode?> ->
                if (barcodes.isNotEmpty() && !isFirstBarcode) {
                    for (barcode in barcodes) {
                        val boundingBox = barcode?.boundingBox
                        if (boundingBox != null && !isFirstBarcode && rectWithinScanArea.contains(
                                boundingBox)
                        ) {
                            Timber.d("rectWithinScanArea $rectWithinScanArea")
                            Timber.d("rectWithinScanArea ${
                                rectWithinScanArea.contains(boundingBox.left,
                                    boundingBox.top,
                                    boundingBox.right,
                                    boundingBox.bottom)
                            }")
                            Timber.d("boundingBox $boundingBox")
//                                    onQRCodeScanned.invoke(barcode, null)
                            isFirstBarcode = true
                            try {
                                CoroutineScope(Dispatchers.IO).launch {
                                    var bitmap = mediaImage.toBitMap(context)
                                    if (bitmap.width > bitmap.height) {
                                        bitmap = bitmap.rotate(90f)
                                    }
                                    Timber.d(" bitmap ${bitmap.width}/${bitmap.height}")
                                    val bitmapCrop = getBitmapQR(bitmap, boundingBox)
                                    val filePath =
                                        bitmapCrop?.let { it1 ->
                                            AppUtils.saveBitmapQrCodeToDisk(context,
                                                it1)
                                        }
                                    onQRCodeScanned.invoke(barcode, filePath)
                                    Timber.d("barcodes filePath $filePath")
                                    imageProxy.close()
                                }

                            } catch (e: Exception) {
                                imageProxy.close()
                                Timber.d("error ${e.message}")
                            }
                        }

                    }
//
                }else {
                imageProxy.close()
            }

            }
            .addOnFailureListener {
                Timber.d("OnRetrievedFailure: ")
                Timber.d("onError: " + it.message)
            }
            .addOnCompleteListener {
//                CoroutineScope(Dispatchers.IO).launch {
//                    delay(1000 - (System.currentTimeMillis() - currentTimestamp))
//
//                }
            }
    }


    fun analyzeImage(imageUri: Uri) {
        if (imageUri == null) {
            return
        }

        val image = InputImage.fromFilePath(context, imageUri)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)
        scanner.process(image)
            .addOnSuccessListener { barcodes: List<Barcode?> ->
                if (barcodes.isNotEmpty() && !isFirstBarcode) {
                    for (barcode in barcodes) {
                        val boundingBox = barcode?.boundingBox
                        if (boundingBox != null && !isFirstBarcode
                        ) {
                            Timber.d("boundingBox $boundingBox")
//                                    onQRCodeScanned.invoke(barcode, null)
                            isFirstBarcode = true
                            try {
                                CoroutineScope(Dispatchers.IO).launch {
                                    var bitmap = if (Build.VERSION.SDK_INT < 28) {
                                        MediaStore.Images
                                            .Media.getBitmap(context.contentResolver, imageUri)

                                    } else {
                                        val source = ImageDecoder
                                            .createSource(context.contentResolver, imageUri)
                                        ImageDecoder.decodeBitmap(source)
                                    }
                                    if (bitmap.width > bitmap.height) {
                                        bitmap = bitmap.rotate(90f)
                                    }
                                    Timber.d("bitmap width:${bitmap.width}/height:${bitmap.height}")
                                    boundingBox.set(
                                        boundingBox.left.coerceAtLeast(0),
                                        boundingBox.top.coerceAtLeast(0),
                                        boundingBox.right.coerceAtMost(bitmap.width),
                                        boundingBox.bottom.coerceAtMost(bitmap.height)
                                    )
                                    val bitmapCrop = getBitmapQR(bitmap, boundingBox)
                                    val filePath =
                                        bitmapCrop?.let { it1 ->
                                            AppUtils.saveBitmapQrCodeToDisk(context,
                                                it1)
                                        }
                                    onQRCodeScanned.invoke(barcode, filePath)
                                    Timber.d("barcodes filePath $filePath")
                                }

                            } catch (e: Exception) {
                                Timber.d("error ${e.message}")
                            }
                        }

                    }
//
                }
            }
            .addOnFailureListener {
                Timber.d("OnRetrievedFailure: ")
                Timber.d("onError: " + it.message)
            }
            .addOnCompleteListener {
            }
    }

    private fun getBitmapQR(bitmapOrigin: Bitmap, rect: Rect): Bitmap? {
        return Bitmap.createBitmap(bitmapOrigin,
            rect.left.coerceAtLeast(0),
            rect.top.coerceAtMost(bitmapOrigin.height),
            rect.width().coerceAtMost(bitmapOrigin.width),
            rect.height().coerceAtMost(bitmapOrigin.height),
            null,
            false)
    }


}
