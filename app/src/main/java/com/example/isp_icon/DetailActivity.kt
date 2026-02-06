package com.example.isp_icon

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private var currentItem: MaintenanceItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 1. Ambil Data dari Intent
        currentItem = intent.getParcelableExtra("EXTRA_DATA")

        // 2. Hubungkan View
        val etNamaSite = findViewById<EditText>(R.id.etNamaSite)
        val etTanggal = findViewById<EditText>(R.id.etTanggal)
        val etPelaksana = findViewById<EditText>(R.id.etPelaksana)
        val etAsman = findViewById<EditText>(R.id.etAsman)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)

        // 3. Tampilkan Data (Jika ada)
        currentItem?.let { item ->
            etNamaSite.setText(item.namaSite)
            etTanggal.setText(item.tanggal)
            etPelaksana.setText(item.pelaksana)
            etAsman.setText(item.asman)
        }

        // 4. Aksi Tombol Download
        btnDownload.setOnClickListener {
            val link = currentItem?.linkPdf
            if (link != null && link.startsWith("http")) {
                downloadPdf(link, currentItem?.namaSite ?: "Laporan")
            } else {
                Toast.makeText(this, "Link PDF tidak tersedia!", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdate.setOnClickListener {
            // 1. Validasi sederhana
            if (etNamaSite.text.isEmpty()) {
                etNamaSite.error = "Nama Site tidak boleh kosong!"
                return@setOnClickListener
            }

            lakukanUpdateData()
        }
    }

    private fun downloadPdf(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle("Download Laporan Maintenance")
            request.setDescription("Mengunduh laporan untuk $fileName")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Download dimulai...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal Download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun lakukanUpdateData() {
        // Ubah tombol jadi "Loading..." agar user tidak klik berkali-kali
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val originalText = btnUpdate.text
        btnUpdate.text = "Mengirim Data..."
        btnUpdate.isEnabled = false

        // 1. Ambil data terbaru dari EditText
        // Kita gunakan fitur .copy() dari Kotlin untuk menyalin data lama
        // tapi mengubah bagian yang diedit saja.
        // PENTING: "noWo" (ID) tidak boleh berubah agar Google Sheet tahu baris mana yang diedit.
        val dataUpdate = currentItem?.copy(
            namaSite = findViewById<EditText>(R.id.etNamaSite).text.toString(),
            tanggal = findViewById<EditText>(R.id.etTanggal).text.toString(),
            pelaksana = findViewById<EditText>(R.id.etPelaksana).text.toString(),
            asman = findViewById<EditText>(R.id.etAsman).text.toString()
            // Link PDF dan No WO tetap sama dari data asli
        )

        if (dataUpdate != null) {
            // 2. Panggil API
            ApiClient.instance.updateMaintenanceData(data = dataUpdate).enqueue(object : Callback<MaintenanceResponse> {
                override fun onResponse(
                    call: Call<MaintenanceResponse>,
                    response: Response<MaintenanceResponse>
                ) {
                    // Kembalikan tombol seperti semula
                    btnUpdate.text = originalText
                    btnUpdate.isEnabled = true

                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Berhasil Update Data!", Toast.LENGTH_LONG).show()
                        finish() // Tutup halaman detail, kembali ke List
                    } else {
                        Toast.makeText(applicationContext, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MaintenanceResponse>, t: Throwable) {
                    btnUpdate.text = originalText
                    btnUpdate.isEnabled = true
                    Toast.makeText(applicationContext, "Error Koneksi: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}