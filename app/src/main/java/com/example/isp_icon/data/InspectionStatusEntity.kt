package com.example.isp_icon.data
import androidx.room.Entity

@Entity(tableName = "tabel_status_inspeksi", primaryKeys = ["noWo", "kategori"])
data class InspectionStatusEntity(
    val noWo: String,
    val kategori: String,
    val isCompleted: Boolean = true
)