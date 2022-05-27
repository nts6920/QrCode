package com.nhnextsoft.qrcode.utils

import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Environment
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.result.*
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.nhnextsoft.qrcode.Constants
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.copyToClipboard
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object AppUtils {

    fun formatDate(date: Date): String {
        val pattern =
            if (Locale.getDefault().equals(Locale.US)) {
                "MM.dd.yyyy HH:mm"
            } else {
                "dd.MM.yyyy HH:mm"
            }
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(date)
    }

    fun saveBitmapQrCodeToDisk(context: Context, bitmap: Bitmap): String {
        val file = File(context.cacheDir.absolutePath, Constants.barcodeDetectorImage)
        return saveBitmapToDisk(bitmap, file.absolutePath)
    }

    private fun saveBitmapToDisk(bitmap: Bitmap, path: String): String {
        val fileSave = File(path)
        if (fileSave.exists()) {
            fileSave.delete()
        }
        val fileOutputStream = FileOutputStream(fileSave)
        val bos = BufferedOutputStream(fileOutputStream)
        bitmap.compress(CompressFormat.JPEG, 50, bos)
        bos.flush()
        bos.close()
        return fileSave.absolutePath
    }

    fun showToast(
        context: Context,
        @StringRes stringId: Int,
    ) {
        showToast(context, context.getString(stringId))
    }

    fun showToast(context: Context, text: String) {
        val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    fun saveImageQrCodeCreate(
        context: Context,
        valueCode: String,
        barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE,
        colorBarcode: Color = Color.White,
    ) {
        val tsLong = System.currentTimeMillis() / 1000
        val filename = "qrcode-$tsLong.jpg"
        //Output stream
        val image: File
        var fos: OutputStream? = null
        val bitmapQrCode: Bitmap? = createBarCodeByBarcodeFormat(
            valueCode, 500, 500,
            barcodeFormat = barcodeFormat,
            colorWhite = colorBarcode
        )
        bitmapQrCode?.let { bitmap ->
            try {
                //For devices running android >= Q
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //getting the contentResolver
                    context.contentResolver?.also { resolver ->
                        //Content resolver will process the content values
                        val contentValues = ContentValues().apply {

                            //putting file information in content values
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                            put(MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOCUMENTS)
                        }

                        //Inserting the contentValues to contentResolver and getting the Uri
                        val imageUri: Uri? =
                            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues)
                        Timber.i("pictureFile: $imageUri")
                        //Opening an outputted with the Uri that we got
                        fos = imageUri?.let { resolver.openOutputStream(it) }
                    }

                    fos?.use {
                        //Finally writing the bitmap to the output stream that we opened
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                } else {
                    //These for devices running on android < Q
                    //So I don't think an explanation is needed here
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    image = File(imagesDir, filename)
                    fos = FileOutputStream(image)
                    fos?.use {
                        //Finally writing the bitmap to the output stream that we opened
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        try {
                            Timber.i("pictureFile: ${image.absolutePath}")
                            MediaStore.Images.Media.insertImage(context.contentResolver,
                                image.absolutePath, filename, null)
                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.data = Uri.fromFile(image)
                            context.sendBroadcast(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Timber.i("There was an issue scanning gallery.")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Timber.i("There was an issue saving the image.")
            }
        }
    }

    private fun connectToWifiAccessPoint(
        context: Context,
        SSID: String,
        password: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestion1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WifiNetworkSuggestion.Builder()
                    .setSsid(SSID)
                    .setWapiPassphrase(password)
                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                    .build()
            } else {
                WifiNetworkSuggestion.Builder()
                    .setSsid(SSID)
                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                    .build()
            }

            val suggestion2 = WifiNetworkSuggestion.Builder()
                .setSsid(SSID)
                .setWpa2Passphrase(password)
                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                .build()

            val suggestion3 = WifiNetworkSuggestion.Builder()
                .setSsid(SSID)
                .setWpa3Passphrase(password)
                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                .build()

            val suggestionsList = listOf(suggestion1, suggestion2, suggestion3)

            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val status = wifiManager.addNetworkSuggestions(suggestionsList)
            if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            }

// Optional (Wait for post connection broadcast to one of your suggestions)
            val intentFilter =
                IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)

            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                        return
                    }
                    // do post connect processing here
                }
            }
            context.registerReceiver(broadcastReceiver, intentFilter)
        } else {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                Timber.d("connection wifi pre Q")
                val wifiConfig = WifiConfiguration()
                wifiConfig.SSID = "\"" + SSID + "\""
                wifiConfig.preSharedKey = "\"" + password + "\""
                val netId: Int = wifiManager.addNetwork(wifiConfig)
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    fun createBarcode(
        str: String
    ): Bitmap? {
        val barHeight = 640
        return try {
            val encode =
                MultiFormatWriter().encode(Uri.encode(str), BarcodeFormat.CODE_128, 1080, 1)
            val width = encode.width
            val createBitmap = Bitmap.createBitmap(width, barHeight, Bitmap.Config.ARGB_8888)
            for (i in 0 until width) {
                val iArr = IntArray(barHeight)
                Arrays.fill(iArr, if (encode[i, 0]) -16777216 else -1)
                createBitmap.setPixels(iArr, 0, 1, i, 0, 1, barHeight)
            }
            createBitmap
        } catch (e: WriterException) {
            null
        }
    }

    fun createBarCodeByBarcodeFormat(
        codeData: String,
        codeHeight: Int = 1080,
        codeWidth: Int = 1080,
        colorBack: Color = Color.Black,
        colorWhite: Color = Color.White,
        barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE,
    ): Bitmap? {
        Timber.d("createBarCodeByBarcodeFormat $codeData")
        val hintsMap: MutableMap<EncodeHintType, Any?> = HashMap()
        hintsMap[EncodeHintType.CHARACTER_SET] = "utf-8"
        hintsMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q
        hintsMap[EncodeHintType.MARGIN] = 2
        try {
            val bitMatrix = MultiFormatWriter().encode(codeData,
                barcodeFormat,
                codeHeight,
                codeWidth,
                hintsMap)
            val width: Int = bitMatrix.width
            val height: Int = bitMatrix.height
            val pixels = IntArray(width * height)
            for (i in 0 until width) {
                for (j in 0 until height) {
                    if (bitMatrix[j, i]) { // True if is is Black
                        pixels[i * height + j] = colorBack.toArgb() //White
                    } else {
                        pixels[i * height + j] = colorWhite.toArgb() //Insert the color here.
                    }
                }
            }
            //SaveImage(bitmap1);
            return Bitmap.createBitmap(pixels, codeHeight, codeWidth, Bitmap.Config.ARGB_8888)
        } catch (e: WriterException) {
            e.printStackTrace()
            Timber.e(e)
        }
        return null
    }

    fun getUriBarcodeUri(context: Context, bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        val imagesFolder = File(context.cacheDir, "images")
        try {
            imagesFolder.mkdirs()
            val tsLong = System.currentTimeMillis() / 1000
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            Timber.d("IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    fun eventShare(context: Context, valueQrCode: String, format: FormatBarCode) {
        val bitmap = createBarCodeByBarcodeFormat(codeData = valueQrCode,
            barcodeFormat = FormatBarCode.ofToFormat(format.name))
        bitmap?.let {
            val uri = getUriBarcodeUri(context, it)
            showToast(context, context.getString(R.string.preparing))
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/png"
            val chooser = Intent.createChooser(intent, "Share")
            val resInfoList = context.packageManager.queryIntentActivities(chooser,
                PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        }
    }

    fun eventCopy(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        val copyToClipboard: String = when (parsedResult) {
            is TelParsedResult -> {
                parsedResult.displayResult
            }
            is TextParsedResult -> {
                parsedResult.displayResult
            }
            is WifiParsedResult -> {
                "SSID:${parsedResult.ssid}\nSecurity:${parsedResult.networkEncryption}" +
                        "\nPassword:${parsedResult.password}\nHidden:${parsedResult.isHidden}"
            }
            is URIParsedResult -> {
                parsedResult.displayResult
            }
            is SMSParsedResult -> {
                parsedResult.displayResult
            }
            is EmailAddressParsedResult -> {
                parsedResult.displayResult
            }
            is GeoParsedResult -> {
                parsedResult.displayResult
            }
            is AddressBookParsedResult -> {
                parsedResult.displayResult
            }
            is CalendarParsedResult -> {
                parsedResult.displayResult
            }
            else -> {
                parsedResult?.displayResult ?: ""
            }
        }
        context.copyToClipboard(copyToClipboard)
        showToast(context, context.getString(R.string.copied_to_clipboard))
    }

    fun eventCopyPassword(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        val copyToClipboard: String = when (parsedResult) {
            is WifiParsedResult -> {
                parsedResult.password
            }
            else -> {
                ""
            }
        }
        context.copyToClipboard(copyToClipboard)
        showToast(context, context.getString(R.string.copied_to_clipboard))
    }

    fun eventConnectWifi(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is WifiParsedResult -> {
                connectToWifiAccessPoint(context, parsedResult.ssid, parsedResult.password)
                showToast(context, context.getString(R.string.connecting))
            }
            else -> {
                showToast(context, context.getString(R.string.error))
            }
        }
    }

    private fun composeEmail(context: Context, addresses: Array<String>, subject: String) {
        ShareCompat.IntentBuilder(context)
            .setType("message/rfc822")
            .addEmailTo(addresses)
            .setSubject(subject)
            .setText("")
            //.setHtmlText(body) //If you are using HTML in your body text
            .setChooserTitle("Send mail")
            .startChooser()
    }

    fun eventEmail(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is EmailAddressParsedResult -> {
                composeEmail(context, parsedResult.tos, parsedResult.subject ?: "")
            }
            is AddressBookParsedResult -> {
                composeEmail(context, parsedResult.emails, "")
            }
        }
        showToast(context, context.getString(R.string.preparing))
    }

    fun insertContact(context: Context, name: String, email: String, phone: String) = try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.EMAIL, email)
            putExtra(ContactsContract.Intents.Insert.PHONE, phone)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }

    fun eventAddContact(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is TelParsedResult -> {
                insertContact(context,
                    parsedResult.title ?: "",
                    phone = parsedResult.number,
                    email = "")
            }
            is SMSParsedResult -> {
                insertContact(context,
                    "",
                    phone = parsedResult.numbers.first(),
                    email = "")
            }
            is EmailAddressParsedResult -> {
                insertContact(context, "", phone = "", email = parsedResult.tos.first())
            }
            is AddressBookParsedResult -> {
                insertContact(context,
                    parsedResult.names.first(),
                    phone = parsedResult.phoneNumbers.first(),
                    email = parsedResult.emails.first())
            }
        }
        showToast(context, context.getString(R.string.preparing))
    }

    private fun callFromDialer(context: Context, number: String) {
        try {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$number")
            if (callIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(callIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun eventCallContact(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is TelParsedResult -> {
                callFromDialer(context, parsedResult.number)
            }
            is SMSParsedResult -> {
                callFromDialer(context, parsedResult.numbers.first())
            }
            is AddressBookParsedResult -> {
                callFromDialer(context, parsedResult.phoneNumbers.first())
            }
        }
        showToast(context, context.getString(R.string.preparing))
    }

    private fun sendSMS(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
        intent.putExtra("sms_body", message)
        context.startActivity(intent)
    }

    fun eventSendSMS(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is TelParsedResult -> {
                sendSMS(context, parsedResult.number, "")
            }
            is SMSParsedResult -> {
                sendSMS(context, parsedResult.numbers.first(), parsedResult.body)
            }
            is AddressBookParsedResult -> {
                sendSMS(context, parsedResult.phoneNumbers.first(), "")
            }
        }
        showToast(context, context.getString(R.string.preparing))
    }

    fun openUri(context: Context, uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun eventOpenWeb(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is URIParsedResult -> {
                openUri(context, parsedResult.uri)
            }
            is AddressBookParsedResult -> {
                parsedResult.displayResult
                openUri(context, parsedResult.urLs.first())
            }
        }

        showToast(context, context.getString(R.string.preparing))
    }

    fun openWebSearch(context: Context, query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, query) // query contains search string
        context.startActivity(intent)
    }

    fun eventSearchWeb(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is TelParsedResult -> {
                openWebSearch(context, parsedResult.number)
            }
            is TextParsedResult -> {
                openWebSearch(context, parsedResult.text)
            }
            is URIParsedResult -> {
                openWebSearch(context, parsedResult.uri)
            }
            is GeoParsedResult -> {
                openWebSearch(context, parsedResult.displayResult)
            }
            is CalendarParsedResult -> {
                openWebSearch(context, parsedResult.summary)
            }
        }

        showToast(context, context.getString(R.string.preparing))
    }


    fun addEvent(context: Context, title: String, location: String, begin: Long, end: Long) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun eventEventAddCalendar(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is CalendarParsedResult -> {
                addEvent(
                    context,
                    parsedResult.summary,
                    parsedResult.location,
                    parsedResult.startTimestamp,
                    parsedResult.endTimestamp,
                )
            }
        }

        showToast(context, context.getString(R.string.preparing))
    }

    fun eventOpenGeo(context: Context, valueQrCode: String, format: FormatBarCode) {
        val parsedResult =
            FormatBarCode.ofParsedResult(valueQrCode, format.name)
        when (parsedResult) {
            is GeoParsedResult -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse(parsedResult.geoURI))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        showToast(context, context.getString(R.string.preparing))
    }
}