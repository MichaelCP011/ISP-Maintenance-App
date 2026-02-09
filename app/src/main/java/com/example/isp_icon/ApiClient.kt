package com.example.isp_icon

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Pastikan URL diakhiri '/'
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbylyaeNUvTkx_8f1kZNA8X_ekLEQzYaSSzS7OYczuIlQ0MU-Cg_n3jq-KgrXTniQLnDgQ/"

    // 1. Buat Logger (CCTV)
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Ini akan menampilkan seluruh isi JSON di Logcat
    }

    // 2. Buat OkHttpClient dengan Logger
    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(30, TimeUnit.SECONDS) // Tambah durasi biar tidak RTO
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 3. Buat Gson Lenient
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Pasang Client yang ada Logger-nya
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(ApiService::class.java)
    }
}