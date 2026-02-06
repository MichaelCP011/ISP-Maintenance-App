package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope // Pastikan ini ada
import com.example.isp_icon.data.AppDatabase
import com.example.isp_icon.data.LokasiEntity
import com.example.isp_icon.data.PersonilEntity
import com.example.isp_icon.data.PertanyaanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Menu Navigasi
        findViewById<CardView>(R.id.menuChecklist).setOnClickListener {
            startActivity(Intent(this, InspectionHeaderActivity::class.java))
        }

        findViewById<CardView>(R.id.menuMonitoring).setOnClickListener {
            // Arahkan ke monitoring
            Toast.makeText(this, "Menu Monitoring dipilih", Toast.LENGTH_SHORT).show()
        }

        // Logic Tombol Sync
        findViewById<Button>(R.id.btnSync).setOnClickListener {
            lakukanSyncData()
        }
    }

    private fun lakukanSyncData() {
        val btnSync = findViewById<Button>(R.id.btnSync)
        val originalText = btnSync.text.toString()
        btnSync.text = "Loading..."
        btnSync.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("DEBUG_SYNC", "=== MULAI SYNC DATA ===")
                val db = AppDatabase.getDatabase(applicationContext)
                val api = ApiClient.instance

                // ------------------------------------------------------
                // 1. SYNC LOKASI
                // ------------------------------------------------------
                Log.d("DEBUG_SYNC", "1. Download Master Lokasi...")
                val resLokasi = api.getMasterLokasi().awaitResponse()

                if (resLokasi.isSuccessful) {
                    val rawData = resLokasi.body()?.data ?: emptyList()
                    // Filter: Hapus yang id_site kosong
                    val cleanData = rawData.filter { !it.idSite.isNullOrEmpty() }

                    if (cleanData.isNotEmpty()) {
                        db.appDao().insertLokasi(cleanData)
                        Log.d("DEBUG_SYNC", "   -> Sukses simpan ${cleanData.size} Lokasi")
                    } else {
                        Log.e("DEBUG_SYNC", "   -> GAGAL: Data Lokasi kosong atau nama kolom 'id_site' di sheet salah!")
                    }
                } else {
                    Log.e("DEBUG_SYNC", "   -> Error API Lokasi: ${resLokasi.code()}")
                }

                // ------------------------------------------------------
                // 2. SYNC PERSONIL
                // ------------------------------------------------------
                Log.d("DEBUG_SYNC", "2. Download Master Personil...")
                val resPersonil = api.getMasterPersonil().awaitResponse()

                if (resPersonil.isSuccessful) {
                    val rawData = resPersonil.body()?.data ?: emptyList()
                    // Filter: Hapus yang nama_lengkap kosong
                    val cleanData = rawData.filter { !it.namaLengkap.isNullOrEmpty() }

                    if (cleanData.isNotEmpty()) {
                        db.appDao().insertPersonil(cleanData)
                        Log.d("DEBUG_SYNC", "   -> Sukses simpan ${cleanData.size} Personil")
                    } else {
                        Log.e("DEBUG_SYNC", "   -> GAGAL: Data Personil kosong. Cek header sheet 'Master_Personil' harus 'nama_lengkap'")
                    }
                } else {
                    Log.e("DEBUG_SYNC", "   -> Error API Personil: ${resPersonil.code()}")
                }

                // ------------------------------------------------------
                // 3. SYNC PERTANYAAN (SOAL)
                // ------------------------------------------------------
                Log.d("DEBUG_SYNC", "3. Download Config Pertanyaan...")
                val resSoal = api.getMasterSoal().awaitResponse()

                if (resSoal.isSuccessful) {
                    val rawData = resSoal.body()?.data ?: emptyList()
                    // Filter: Hapus yang id_pertanyaan kosong
                    val cleanData = rawData.filter { !it.idPertanyaan.isNullOrEmpty() }

                    if (cleanData.isNotEmpty()) {
                        db.appDao().insertPertanyaan(cleanData)
                        Log.d("DEBUG_SYNC", "   -> Sukses simpan ${cleanData.size} Pertanyaan")
                    } else {
                        Log.e("DEBUG_SYNC", "   -> GAGAL: Data Soal kosong. Cek header sheet 'Config_Pertanyaan' harus 'id_pertanyaan'")
                    }
                } else {
                    Log.e("DEBUG_SYNC", "   -> Error API Soal: ${resSoal.code()}")
                }

                // ------------------------------------------------------
                // FINAL CHECK
                // ------------------------------------------------------
                val countLokasi = db.appDao().getAllLokasi().size
                val countPersonil = db.appDao().getAllPersonil().size
                // (Optional) Buat fungsi countPertanyaan di DAO jika mau cek jumlahnya juga

                Log.d("DEBUG_SYNC", "=== SELESAI ===")
                Log.d("DEBUG_SYNC", "Total di Database HP -> Lokasi: $countLokasi, Personil: $countPersonil")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Sync Selesai! Lokasi: $countLokasi, Personil: $countPersonil", Toast.LENGTH_LONG).show()
                    btnSync.text = originalText
                    btnSync.isEnabled = true
                }

            } catch (e: Exception) {
                Log.e("DEBUG_SYNC", "CRITICAL ERROR: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSync.text = originalText
                    btnSync.isEnabled = true
                }
            }
        }
    }
}