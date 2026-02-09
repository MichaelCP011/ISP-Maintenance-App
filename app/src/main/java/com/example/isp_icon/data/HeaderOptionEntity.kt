package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_opsi_header")
data class HeaderOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @SerializedName("kategori") val kategori: String, // ex: "nama_site", "pelaksana"
    @SerializedName("nilai") val nilai: String        // ex: "Site Bekasi", "Budi"
)