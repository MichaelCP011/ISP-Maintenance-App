package com.example.isp_icon

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class InspectionHeaderActivity : AppCompatActivity() {

    // View References
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

        // 1. Inisialisasi View
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

        // 2. Setup Date Picker (Kalender)
        setupDatePicker()

        // 3. Load Data dari Database
        populateFormFromDB()

        // 4. Tombol Lanjut
        findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            lanjutKeMenu()
        }
    }

    // --- LOGIC KALENDER (DATE PICKER) ---
    private fun setupDatePicker() {
        etTanggal.setOnClickListener {
            // Ambil tanggal hari ini sebagai default
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Tampilkan Dialog Kalender
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format: YYYY-MM-DD (Bulan dimulai dari 0, jadi +1)
                    val formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    etTanggal.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun populateFormFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.appDao()

            // Ambil data per kategori
            val sites = dao.getOptionsByCategory("nama_site")
            val tipePop = dao.getOptionsByCategory("tipe_pop")
            val model = dao.getOptionsByCategory("model_bangunan")
            val pelaksana = dao.getOptionsByCategory("pelaksana")
            val pemeriksa = dao.getOptionsByCategory("pemeriksa")
            val asman = dao.getOptionsByCategory("asman")
            val manajer = dao.getOptionsByCategory("manajer")
            val area = dao.getOptionsByCategory("area")

            withContext(Dispatchers.Main) {
                // Setup Searchable Dropdown Site
                val siteAdapter = ArrayAdapter(this@InspectionHeaderActivity, android.R.layout.simple_dropdown_item_1line, sites)
                acNamaSite.setAdapter(siteAdapter)

                // Setup Chips (Box Selection)
                fillChipGroup(cgTipePop, tipePop)
                fillChipGroup(cgModelBangunan, model)
                fillChipGroup(cgArea, area)

                // Setup Dropdown Personil
                fillSpinner(spPelaksana, pelaksana)
                fillSpinner(spPemeriksa, pemeriksa)
                fillSpinner(spAsman, asman)
                fillSpinner(spManajer, manajer)
            }
        }
    }

    // --- LOGIC CHIP WARNA-WARNI (REVISI PENTING DI SINI) ---
    private fun fillChipGroup(group: ChipGroup, items: List<String>) {
        group.removeAllViews()

        // Buat Aturan Warna (ColorStateList)
        // Jika Checked (Dipilih) -> Biru (#2196F3)
        // Jika Default (Tidak Dipilih) -> Putih (White)
        val backgroundColorState = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // Kondisi: Dipilih
                intArrayOf(-android.R.attr.state_checked) // Kondisi: Tidak Dipilih
            ),
            intArrayOf(
                Color.parseColor("#2196F3"), // Warna Biru saat dipilih
                Color.WHITE                  // Warna Putih saat tidak dipilih
            )
        )

        // Aturan Warna Teks (Agar kontras)
        // Dipilih -> Putih, Tidak Dipilih -> Hitam
        val textColorState = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.BLACK)
        )

        for (item in items) {
            val chip = Chip(this)
            chip.text = item
            chip.id = View.generateViewId() // PENTING: Harus punya ID unik agar bisa dipilih
            chip.isCheckable = true
            chip.isClickable = true

            // Terapkan warna dinamis tadi
            chip.chipBackgroundColor = backgroundColorState
            chip.setTextColor(textColorState)

            // Border tipis warna biru
            chip.chipStrokeWidth = 2f
            chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))

            group.addView(chip)
        }
    }

    private fun fillSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
    }

    private fun lanjutKeMenu() {
        // Contoh cara mengambil data dari Chip yang dipilih
        val tipePop = getSelectedChipText(cgTipePop)
        val modelBangunan = getSelectedChipText(cgModelBangunan)
        val area = getSelectedChipText(cgArea)

        if (etNoWo.text.isEmpty() || etTanggal.text.isEmpty() || acNamaSite.text.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi data WO, Tanggal, dan Site", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan data sementara (Session)
        val session = InspectionSession(
            noWo = etNoWo.text.toString(),
            tanggal = etTanggal.text.toString(),
            idSite = acNamaSite.text.toString(), // Sementara pakai nama site sbg ID
            namaSite = acNamaSite.text.toString(),
            tipePop = tipePop,
            pelaksana = spPelaksana.selectedItem?.toString() ?: "",
            pemeriksa = spPemeriksa.selectedItem?.toString() ?: "",
            asman = spAsman.selectedItem?.toString() ?: "",
            manajer = spManajer.selectedItem?.toString() ?: "",
            area = area
        )

        // Pindah ke Menu Inspeksi
        val intent = android.content.Intent(this, InspectionMenuActivity::class.java)
        intent.putExtra("SESSION_DATA", session)
        startActivity(intent)
    }

    // Helper untuk ambil teks dari ChipGroup
    private fun getSelectedChipText(chipGroup: ChipGroup): String {
        val selectedId = chipGroup.checkedChipId
        if (selectedId != -1) {
            val selectedChip = chipGroup.findViewById<Chip>(selectedId)
            return selectedChip.text.toString()
        }
        return ""
    }
}