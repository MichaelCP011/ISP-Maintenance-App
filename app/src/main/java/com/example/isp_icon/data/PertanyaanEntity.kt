package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_pertanyaan")
data class PertanyaanEntity(
    @PrimaryKey
    @SerializedName("id_pertanyaan") val idPertanyaan: String,

    @SerializedName("kategori") val kategori: String?,
    @SerializedName("pertanyaan") val pertanyaan: String?,
    @SerializedName("tipe_input") val tipeInput: String?,
    @SerializedName("opsi_pilihan") val opsiPilihan: String? // Disimpan sebagai string "A,B,C"
)