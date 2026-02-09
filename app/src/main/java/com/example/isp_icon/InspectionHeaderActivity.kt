package com.example.isp_icon

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InspectionHeaderActivity : AppCompatActivity() {

    // View
    private lateinit var etNoWo: EditText
    private lateinit var etTanggal: EditText
    private lateinit var acNamaSite: AutoCompleteTextView
    private lateinit var cgTipePop: ChipGroup
    private lateinit var cgModelBangunan: ChipGroup
    private lateinit var spPelaksana: Spinner
    private lateinit var spPemeriksa: Spinner
    private lateinit var spAsman: Spinner
    private lateinit var spManajer: Spinner
    private lateinit var cgArea: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_header)

        // Inisialisasi View
        etNoWo = findViewById(R.id.etNoWo)
        etTanggal = findViewById(R.id.etTanggal)
        acNamaSite = findViewById(R.id.acNamaSite)
        cgTipePop = findViewById(R.id.cgTipePop)
        cgModelBangunan = findViewById(R.id.cgModelBangunan)
        spPelaksana = findViewById(R.id.spPelaksana)
        spPemeriksa = findViewById(R.id.spPemeriksa)
        spAsman = findViewById(R.id.spAsman)
        spManajer = findViewById(R.id.spManajer)
        cgArea = findViewById(R.id.cgArea)

        // Load Data
        populateFormFromDB()

        findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            // Validasi & Pindah (Logicnya bisa copy dari yang lama, sesuaikan view baru)
            lanjutKeMenu()
        }
    }

    private fun populateFormFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.appDao()

            // 1. Ambil data per kategori dari tabel baru
            val sites = dao.getOptionsByCategory("nama_site")
            val tipePop = dao.getOptionsByCategory("tipe_pop")
            val model = dao.getOptionsByCategory("model_bangunan")
            val pelaksana = dao.getOptionsByCategory("pelaksana")
            val pemeriksa = dao.getOptionsByCategory("pemeriksa")
            val asman = dao.getOptionsByCategory("asman")
            val manajer = dao.getOptionsByCategory("manajer")
            val area = dao.getOptionsByCategory("area")

            withContext(Dispatchers.Main) {
                // Setup Searchable Site
                val siteAdapter = ArrayAdapter(this@InspectionHeaderActivity, android.R.layout.simple_dropdown_item_1line, sites)
                acNamaSite.setAdapter(siteAdapter)

                // Setup Chips (Box Selection)
                fillChipGroup(cgTipePop, tipePop)
                fillChipGroup(cgModelBangunan, model)
                fillChipGroup(cgArea, area)

                // Setup Spinners
                fillSpinner(spPelaksana, pelaksana)
                fillSpinner(spPemeriksa, pemeriksa)
                fillSpinner(spAsman, asman)
                fillSpinner(spManajer, manajer)
            }
        }
    }

    private fun fillChipGroup(group: ChipGroup, items: List<String>) {
        group.removeAllViews()
        for (item in items) {
            val chip = Chip(this)
            chip.text = item
            chip.isCheckable = true
            chip.isClickable = true
            // Style agar terlihat seperti kotak pilihan
            chip.setChipBackgroundColorResource(android.R.color.white)
            chip.chipStrokeWidth = 2f
            chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#2196F3")) // Ganti warna sesuai tema
            group.addView(chip)
        }
    }

    private fun fillSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
    }

    private fun lanjutKeMenu() {
        // Logic validasi dan Intent (sesuaikan dengan cara ambil data dari Chip)
        // Cara ambil data Chip:
        // val selectedChipId = cgTipePop.checkedChipId
        // val tipePop = if (selectedChipId != -1) findViewById<Chip>(selectedChipId).text.toString() else ""

        Toast.makeText(this, "Simulasi Lanjut...", Toast.LENGTH_SHORT).show()
    }
}