package com.example.isp_icon

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.isp_icon.data.LokasiEntity
import com.example.isp_icon.data.PersonilEntity
import com.example.isp_icon.data.PertanyaanEntity

interface ApiService {
    @GET("exec")
    fun getMaintenanceData(
        @Query("action") action: String
    ): Call<MaintenanceResponse>

    @POST("exec")
    fun updateMaintenanceData(
        @Query("action") action: String = "update", // Parameter action=update
        @Body data: MaintenanceItem
    ): Call<MaintenanceResponse>

    @GET("exec")
    fun getMasterLokasi(
        @Query("action") action: String = "get_lokasi"
    ): Call<MasterResponse<LokasiEntity>>

    @GET("exec")
    fun getMasterPersonil(
        @Query("action") action: String = "get_personil"
    ): Call<MasterResponse<PersonilEntity>>

    @GET("exec")
    fun getMasterSoal(
        @Query("action") action: String = "get_soal"
    ): Call<MasterResponse<PertanyaanEntity>>

    @POST("exec")
    fun submitInspection(
        @Query("action") action: String = "create_inspeksi", // Sesuai logik di masa depan, tapi GAS kita handle di doPost langsung
        @Body request: InspectionRequest
    ): Call<MaintenanceResponse>
}