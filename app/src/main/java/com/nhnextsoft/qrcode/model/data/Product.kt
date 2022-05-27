package com.nhnextsoft.qrcode.model.data

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import java.util.*


data class Product(
    @SerializedName("id") var id: String,
                   @SerializedName("name") var valueCode: String,
                   @SerializedName("date") var date: String,
                   @SerializedName("format")  val format: String,
    @SerializedName("image") var Image: String)

