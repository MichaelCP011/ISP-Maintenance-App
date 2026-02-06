package com.example.isp_icon

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InspectionSession(
    val noWo: String,
    val tanggal: String,
    val idSite: String,
    val namaSite: String,
    val tipePop: String,
    val pelaksana: String,
    val pemeriksa: String,
    val asman: String,
    val manajer: String,
    val area: String
) : Parcelable