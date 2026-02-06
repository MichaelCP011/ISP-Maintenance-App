package com.example.isp_icon

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
}