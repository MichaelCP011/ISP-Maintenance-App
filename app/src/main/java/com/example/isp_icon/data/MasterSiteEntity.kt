package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_master_site")
data class MasterSiteEntity(
    @PrimaryKey
    @SerializedName("id_site") val idSite: String, // Kita pakai ID sebagai Primary Key

    @SerializedName("nama_site") val namaSite: String?,
    @SerializedName("tipe_pop") val tipePop: String?,
    @SerializedName("model_bangunan") val modelBangunan: String?,
    @SerializedName("area") val area: String?
)