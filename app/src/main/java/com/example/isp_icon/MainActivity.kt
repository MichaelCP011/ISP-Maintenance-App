package com.example.isp_icon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var rvMaintenance: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MaintenanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi View
        rvMaintenance = findViewById(R.id.rvMaintenance)
        progressBar = findViewById(R.id.progressBar)

        // 2. Setup RecyclerView & Adapter Kosong dulu
        rvMaintenance.layoutManager = LinearLayoutManager(this)
        adapter = MaintenanceAdapter(emptyList()) { item ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("EXTRA_DATA", item) // Kirim object full
            startActivity(intent)
        }
        rvMaintenance.adapter = adapter

        // 3. Ambil Data
        getDataFromSheet()

        val btnTambah = findViewById<Button>(R.id.btnTambahData)

        btnTambah.setOnClickListener {
            // Ganti URL ini dengan link Google Form aslimu
            val urlForm = "https://docs.google.com/forms/d/e/1FAIpQLSfIRgxgfwwWyEilMcBgIaaycv414yr1QfoZDUqD7gvFej1SCg/viewform"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(urlForm)
            startActivity(intent)
        }
    }

    private fun getDataFromSheet() {
        progressBar.visibility = View.VISIBLE // Tampilkan loading

        ApiClient.instance.getMaintenanceData("read").enqueue(object : Callback<MaintenanceResponse> {
            override fun onResponse(
                call: Call<MaintenanceResponse>,
                response: Response<MaintenanceResponse>
            ) {
                progressBar.visibility = View.GONE // Sembunyikan loading

                if (response.isSuccessful) {
                    val listData = response.body()?.data
                    if (listData != null) {
                        // Masukkan data ke Adapter!
                        adapter.updateData(listData)
                    } else {
                        Toast.makeText(applicationContext, "Data Kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MaintenanceResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(applicationContext, "Error Koneksi: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("API_ERROR", t.message.toString())
            }
        })
    }
}