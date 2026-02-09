package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.isp_icon.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InspectionMenuActivity : AppCompatActivity() {

    private lateinit var session: InspectionSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_menu)

        // 1. Ambil Data Session
        @Suppress("DEPRECATION")
        session = intent.getParcelableExtra("SESSION_DATA") ?: return

        // 2. Tampilkan Info Header
        val tvSiteInfo = findViewById<TextView>(R.id.tvSiteName)
        tvSiteInfo.text = "Site: ${session.namaSite}\nWO: ${session.noWo}"

        // 3. Load Data Pertama Kali
        loadCategories()
    }

    // PENTING: Panggil loadCategories lagi saat kembali dari form (agar ceklis muncul)
    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // A. Ambil Daftar Kategori Unik (Genset, AC, dll)
            val categories = db.appDao().getAllKategori()

            // B. Ambil Daftar Kategori yang SUDAH SELESAI dikerjakan untuk No WO ini
            // Pastikan kamu sudah update AppDao di Tahap 2 sebelumnya!
            val completedList = db.appDao().getCompletedCategories(session.noWo)

            withContext(Dispatchers.Main) {
                setupRecyclerView(categories, completedList)
            }
        }
    }

    private fun setupRecyclerView(categories: List<String>, completedList: List<String>) {
        val rv = findViewById<RecyclerView>(R.id.rvCategories)
        rv.layoutManager = LinearLayoutManager(this)

        // Masukkan completedList ke Adapter
        rv.adapter = CategoryAdapter(categories, completedList) { selectedCategory ->
            // Aksi saat item diklik
            val intent = Intent(this, DynamicFormActivity::class.java)
            intent.putExtra("SESSION_DATA", session)
            intent.putExtra("CATEGORY_NAME", selectedCategory)
            startActivity(intent)
        }
    }
}