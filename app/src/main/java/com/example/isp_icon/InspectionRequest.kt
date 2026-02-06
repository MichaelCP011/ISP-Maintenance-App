package com.example.isp_icon

import com.google.gson.annotations.SerializedName

data class InspectionRequest(
    @SerializedName("no_wo") val noWo: String,
    @SerializedName("id_site") val idSite: String,
    @SerializedName("nama_pelaksana") val namaPelaksana: String,
    @SerializedName("kategori_inspeksi") val kategori: String,

    // Jawaban dikirim sebagai Map (Key: ID Soal, Value: Jawaban)
    // Nanti GSON otomatis mengubahnya jadi JSON di dalam body
    @SerializedName("jawaban") val jawaban: Map<String, String>
)