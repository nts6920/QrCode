package com.nhnextsoft.qrcode.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Parcelize
data class HistoryScan(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @ColumnInfo(name = "value_code")
    val valueCode: String,
    @ColumnInfo(name = "last_update")
    var date: Date,
    val format: String,
) : Parcelable {
    override fun toString(): String {
        return "HistoryScan(id=$id, valueCode='$valueCode', date=$date, format='$format')"
    }
}
