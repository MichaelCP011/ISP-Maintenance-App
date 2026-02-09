package com.example.isp_icon

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class HomeActivity : AppCompatActivity() {

    // View Indikator Status
    private lateinit var cvStatusIndicator: CardView
    private lateinit var tvStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi View Indikator
        cvStatusIndicator = findViewById(R.id.cvStatusIndicator)
        tvStatusText = findViewById(R.id.tvStatusText)

        // 1. Menu Navigasi: Checklist
        findViewById<CardView>(R.id.menuChecklist).setOnClickListener {
            startActivity(Intent(this, InspectionHeaderActivity::class.java))
        }

        // 2. Menu Navigasi: Monitoring
        findViewById<CardView>(R.id.menuMonitoring).setOnClickListener {
            Toast.makeText(this, "Fitur Monitoring akan segera hadir!", Toast.LENGTH_SHORT).show()
        }

        // 3. JALANKAN AUTO SYNC SAAT APLIKASI DIBUKA
        lakukanAutoSync()
    }

    private fun updateStatusIndicator(colorCode: String, message: String) {
        // Fungsi helper untuk ubah warna indikator
        cvStatusIndicator.setCardBackgroundColor(Color.parseColor(colorCode))
        tvStatusText.text = message
    }

    private fun lakukanAutoSync() {
        // Set Status: SEDANG SYNC (Biru)
        updateStatusIndicator("#2979FF", "Syncing...") // Biru Material Design

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("DEBUG_SYNC", "=== AUTO SYNC STARTED ===")
                val db = AppDatabase.getDatabase(applicationContext)
                val api = ApiClient.instance

                // 1. Download LOKASI
                val resLokasi = api.getMasterLokasi().awaitResponse()
                if (resLokasi.isSuccessful) {
                    val rawData = resLokasi.body()?.data ?: emptyList()
                    val cleanData = rawData.filter { !it.idSite.isNullOrEmpty() }
                    if (cleanData.isNotEmpty()) db.appDao().insertLokasi(cleanData)
                }

                // 2. Download PERSONIL
                val resPersonil = api.getMasterPersonil().awaitResponse()
                if (resPersonil.isSuccessful) {
                    val rawData = resPersonil.body()?.data ?: emptyList()
                    val cleanData = rawData.filter { !it.namaLengkap.isNullOrEmpty() }
                    if (cleanData.isNotEmpty()) db.appDao().insertPersonil(cleanData)
                }

                // 3. Download PERTANYAAN
                val resSoal = api.getMasterSoal().awaitResponse()
                if (resSoal.isSuccessful) {
                    val rawData = resSoal.body()?.data ?: emptyList()
                    val cleanData = rawData.filter { !it.idPertanyaan.isNullOrEmpty() }
                    if (cleanData.isNotEmpty()) db.appDao().insertPertanyaan(cleanData)
                }

                // 4. Download HEADER DATA (Site, Personil, dll)
                val resHeader = api.getHeaderData().awaitResponse()
                if (resHeader.isSuccessful) {
                    val data = resHeader.body()?.data ?: emptyList()
                    if (data.isNotEmpty()) {
                        db.appDao().clearHeaderOptions() // Bersihkan yg lama
                        db.appDao().insertHeaderOptions(data)
                        Log.d("DEBUG_SYNC", "Sukses simpan ${data.size} Opsi Header")
                    }
                }

                // Update UI: SUKSES (Hijau)
                withContext(Dispatchers.Main) {
                    updateStatusIndicator("#00E676", "Online & Updated") // Hijau Terang
                    Log.d("DEBUG_SYNC", "Sync Selesai & Sukses")
                }

            } catch (e: Exception) {
                Log.e("DEBUG_SYNC", "Sync Gagal: ${e.message}")

                // Update UI: GAGAL (Merah)
                withContext(Dispatchers.Main) {
                    updateStatusIndicator("#FF1744", "Offline / Error") // Merah Terang
                    // Kita tidak tampilkan Toast error agar tidak mengganggu user saat buka aplikasi,
                    // cukup indikator merah saja.
                }
            }
        }
    }
}