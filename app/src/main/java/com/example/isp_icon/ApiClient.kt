package com.example.isp_icon

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbxd9GJ8WZ4g4HJJawtQmaMDPXQ3cy0eKZYhZs9PGQxdq97Mb-e8huoqo8IjQD8WOfZI/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}