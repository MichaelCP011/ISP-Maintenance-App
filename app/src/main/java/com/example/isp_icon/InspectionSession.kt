package com.example.isp_icon

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Wadah untuk membawa data antar halaman (Header -> Menu -> Form)
@Parcelize
data class InspectionSession(
    val noWo: String,
    val tanggal: String,
    val idSite: String,
    val namaSite: String,

    // --- TAMBAHAN BARU ---
    val tipePop: String? = "-",        // Menggunakan ? agar boleh kosong (null safety)
    val modelBangunan: String? = "-",
    val area: String? = "-",
    val pelaksana: String? = "-",
    val pemeriksa: String? = "-",
    val asman: String? = "-",
    val manajer: String? = "-"
) : Parcelable