package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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

        // 1. Ambil Data Session dari Halaman Sebelumnya
        @Suppress("DEPRECATION")
        session = intent.getParcelableExtra("SESSION_DATA") ?: return

        // 2. Tampilkan Info Header
        findViewById<TextView>(R.id.tvSiteName).text = "Site: ${session.namaSite} (${session.noWo})"

        // 3. Load Kategori dari DB
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // Query DISTINCT kategori dari tabel pertanyaan
            val categories = db.appDao().getAllKategori()

            withContext(Dispatchers.Main) {
                setupRecyclerView(categories)
            }
        }
    }

    private fun setupRecyclerView(categories: List<String>) {
        val rv = findViewById<RecyclerView>(R.id.rvCategories)

        // Setup Grid 2 Kolom
        rv.layoutManager = GridLayoutManager(this, 2)

        rv.adapter = CategoryAdapter(categories) { selectedCategory ->
            // AKSI SAAT KATEGORI DIKLIK
            Toast.makeText(this, "Buka form: $selectedCategory", Toast.LENGTH_SHORT).show()

            // Nanti kita arahkan ke DynamicFormActivity di sini
            // val intent = Intent(this, DynamicFormActivity::class.java)
            // intent.putExtra("SESSION_DATA", session)
            // intent.putExtra("CATEGORY_NAME", selectedCategory)
            // startActivity(intent)
        }
    }
}