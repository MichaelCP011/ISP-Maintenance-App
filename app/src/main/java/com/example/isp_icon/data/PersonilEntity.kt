package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_personil")
data class PersonilEntity(
    @PrimaryKey
    // HARUS SAMA dengan header spreadsheet "nama_lengkap"
    @SerializedName("nama_lengkap") val namaLengkap: String,

    // HARUS SAMA dengan header spreadsheet "jabatan"
    @SerializedName("jabatan") val jabatan: String?
)