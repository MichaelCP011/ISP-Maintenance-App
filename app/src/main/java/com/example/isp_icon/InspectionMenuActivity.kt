package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
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

        // 1. Ambil Data Session (Data dari halaman sebelumnya)
        @Suppress("DEPRECATION")
        session = intent.getParcelableExtra("SESSION_DATA") ?: return

        // 2. Tampilkan Info Header (Perbaikan Error di sini)
        // Kita gunakan ID 'tvSiteName' yang ada di XML baru
        val tvSiteInfo = findViewById<TextView>(R.id.tvSiteName)
        tvSiteInfo.text = "Site: ${session.namaSite} | WO: ${session.noWo}"

        // 3. Load Kategori dari Database
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // Query kategori unik dari tabel pertanyaan
            val categories = db.appDao().getAllKategori()

            withContext(Dispatchers.Main) {
                setupRecyclerView(categories)
            }
        }
    }

    private fun setupRecyclerView(categories: List<String>) {
        val rv = findViewById<RecyclerView>(R.id.rvCategories)

        // Gunakan LinearLayoutManager untuk list vertikal (sesuai desain baru)
        rv.layoutManager = LinearLayoutManager(this)

        rv.adapter = CategoryAdapter(categories) { selectedCategory ->
            // Aksi saat item diklik -> Buka Form Dinamis
            val intent = Intent(this, DynamicFormActivity::class.java)
            intent.putExtra("SESSION_DATA", session)
            intent.putExtra("CATEGORY_NAME", selectedCategory)
            startActivity(intent)
        }
    }
}