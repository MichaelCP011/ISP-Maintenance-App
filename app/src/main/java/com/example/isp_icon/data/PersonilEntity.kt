package com.example.isp_icon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tabel_personil")
data class PersonilEntity(
    @PrimaryKey
    @SerializedName("nama_lengkap") val namaLengkap: String,

    @SerializedName("jabatan") val jabatan: String?
)