package com.nhnextsoft.qrcode.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nhnextsoft.qrcode.utils.FormatBarCode
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Parcelize
data class HistoryCreateCode(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @ColumnInfo(name = "value_code")
    val valueCode: String,
    @ColumnInfo(name = "last_update")
    val date: Date,
    val color:String = "#FFFFFFFF",
    val format: String = FormatBarCode.QR_CODE.name,
) : Parcelable {
    override fun toString(): String {
        return "HistoryCreateCode(id=$id, valueCode='$valueCode', date=$date, color='$color', format='$format')"
    }
}