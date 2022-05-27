package com.nhnextsoft.qrcode.utils


import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.client.result.ParsedResult
import com.google.zxing.client.result.ResultParser

enum class FormatBarCode(val value: String? = null) {
    AZTEC("Aztec"),
    CODABAR("CODABAR"),
    CODE_39("Code 39"),
    CODE_93("Code 93"),
    CODE_128("Code 128"),
    DATA_MATRIX("Data Matrix"),
    EAN_8("EAN-8"),
    EAN_13("EAN-13"),
    ITF("ITF"),
    MAXICODE("MaxiCode"),
    PDF_417("PDF417"),
    QR_CODE("QR Code"),
    RSS_14("RSS 14"),
    RSS_EXPANDED("RSS EXPANDED"),
    UPC_A("UPC-A"),
    UPC_E("UPC-E"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {

        fun of(barcode: Barcode): FormatBarCode = when (barcode.format) {
            Barcode.FORMAT_CODE_128 -> CODE_128
            Barcode.FORMAT_CODE_39 -> CODE_39
            Barcode.FORMAT_CODE_93 -> CODE_93
            Barcode.FORMAT_CODABAR -> CODABAR
            Barcode.FORMAT_DATA_MATRIX -> DATA_MATRIX
            Barcode.FORMAT_EAN_13 -> EAN_13
            Barcode.FORMAT_EAN_8 -> EAN_8
            Barcode.FORMAT_ITF -> ITF
            Barcode.FORMAT_QR_CODE -> QR_CODE
            Barcode.FORMAT_UPC_A -> UPC_A
            Barcode.FORMAT_UPC_E -> UPC_E
            Barcode.FORMAT_PDF417 -> PDF_417
            Barcode.FORMAT_AZTEC -> AZTEC
            else -> UNKNOWN
        }

        fun ofToFormat(format: String): BarcodeFormat = when (format) {
            AZTEC.name -> BarcodeFormat.AZTEC
            CODABAR.name -> BarcodeFormat.CODABAR
            CODE_39.name -> BarcodeFormat.CODE_39
            CODE_93.name -> BarcodeFormat.CODE_93
            CODE_128.name -> BarcodeFormat.CODE_128
            DATA_MATRIX.name -> BarcodeFormat.DATA_MATRIX
            EAN_8.name -> BarcodeFormat.EAN_8
            EAN_13.name -> BarcodeFormat.EAN_13
            ITF.name -> BarcodeFormat.ITF
            MAXICODE.name -> BarcodeFormat.MAXICODE
            PDF_417.name -> BarcodeFormat.PDF_417
            QR_CODE.name -> BarcodeFormat.QR_CODE
            RSS_14.name -> BarcodeFormat.RSS_14
            RSS_EXPANDED.name -> BarcodeFormat.RSS_EXPANDED
            UPC_A.name -> BarcodeFormat.UPC_A
            UPC_E.name -> BarcodeFormat.UPC_E
            UNKNOWN.name -> BarcodeFormat.UPC_EAN_EXTENSION
            else -> BarcodeFormat.QR_CODE
        }

        fun ofParsedResult(content: String, format: String): ParsedResult? {
            val result = Result(content, null, null, ofToFormat(format))
            return ResultParser.parseResult(result)
        }

        fun ofToFormatBarCode(format: String): FormatBarCode = when (format) {
            AZTEC.name -> AZTEC
            CODABAR.name -> CODABAR
            CODE_39.name -> CODE_39
            CODE_93.name -> CODE_93
            CODE_128.name -> CODE_128
            DATA_MATRIX.name -> DATA_MATRIX
            EAN_8.name -> EAN_8
            EAN_13.name -> EAN_13
            ITF.name -> ITF
            MAXICODE.name -> MAXICODE
            PDF_417.name -> PDF_417
            QR_CODE.name -> QR_CODE
            RSS_14.name -> RSS_14
            RSS_EXPANDED.name -> RSS_EXPANDED
            UPC_A.name -> UPC_A
            UPC_E.name -> UPC_E
            UNKNOWN.name -> UNKNOWN
            else -> QR_CODE
        }

    }
}