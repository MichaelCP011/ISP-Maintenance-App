package com.example.isp_icon

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyqihX7LWAUihuSya_n6s6KhZ4_YJWd_-3ApXW8DuZjTRM6lmqnf1I8Y13fvx-18e0w/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}