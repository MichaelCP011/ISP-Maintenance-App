package com.example.isp_icon

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

// Ini adalah pembungkus respon utama (sesuai JSON kamu: status & data)
data class MaintenanceResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<MaintenanceItem>
)

// Ini adalah detail per baris data
@Parcelize
data class MaintenanceItem(
    @SerializedName("No WO") val noWo: String?, // Kita pakai No WO sebagai ID unik
    @SerializedName("Timestamp") val timestamp: String?,
    @SerializedName("Nama Site") val namaSite: String?,
    @SerializedName("Tgl Pelaksanaan") val tanggal: String?,
    @SerializedName("Pelaksana") val pelaksana: String?,
    @SerializedName("Asman") val asman: String?,
    @SerializedName("Area Jakban") val area: String?,
    @SerializedName("Model Bangunan") val modelBangunan: String?,
    @SerializedName("Tipe POP") val tipePop: String?,

    // Link PDF (Perhatikan nama kolom ini panjang & ada spasi, copy persis dari JSON)
    @SerializedName("Merged Doc URL - Realtime PM ISP SBU Jakban Checklist v.1.1")
    val linkPdf: String?,

    // Contoh data checklist (tambahkan sisanya jika perlu ditampilkan di Detail)
    @SerializedName("Apakah kondisi lingkungan POP terlihat bersih ?")
    val cekLingkunganBersih: String?,

    @SerializedName("Apakah terdapat Genset ?")
    val cekAdaGenset: String?
): Parcelable