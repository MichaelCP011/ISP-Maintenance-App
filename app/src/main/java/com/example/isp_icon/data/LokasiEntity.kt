package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_lokasi")
data class LokasiEntity(
    @PrimaryKey
    @SerializedName("id_site") val idSite: String, // "id_site" dari JSON -> idSite di Kotlin

    @SerializedName("nama_site") val namaSite: String?,
    @SerializedName("tipe") val tipe: String?,
    @SerializedName("alamat") val alamat: String?
)