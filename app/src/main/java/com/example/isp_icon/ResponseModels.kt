package com.example.isp_icon

import com.google.gson.annotations.SerializedName

// Pembungkus generic untuk semua respon API baru
data class MasterResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<T>
)