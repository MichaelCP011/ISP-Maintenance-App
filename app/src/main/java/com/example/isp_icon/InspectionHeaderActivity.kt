package com.example.isp_icon

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class InspectionHeaderActivity : AppCompatActivity() {

    // View References
    private lateinit var etNoWo: EditText
    private lateinit var etTanggal: EditText

    // Autocomplete untuk pencarian site
    private lateinit var acNamaSite: AutoCompleteTextView

    // Field hasil Autofill (Read-Only) - Dulu ChipGroup, sekarang EditText
    private lateinit var etTipePop: EditText
    private lateinit var etModelBangunan: EditText
    private lateinit var etArea: EditText

    // Personil
    private lateinit var acPelaksana: AutoCompleteTextView
    private lateinit var acPemeriksa: AutoCompleteTextView
    private lateinit var acAsman: AutoCompleteTextView
    private lateinit var acManajer: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_header)

        // 1. Inisialisasi View sesuai ID baru di XML
        etNoWo = findViewById(R.id.etNoWo)
        etTanggal = findViewById(R.id.etTanggal)
        acNamaSite = findViewById(R.id.acNamaSite)

        // Output Autofill
        etTipePop = findViewById(R.id.etTipePop)
        etModelBangunan = findViewById(R.id.etModelBangunan)
        etArea = findViewById(R.id.etArea)

        // Input Personil
        acPelaksana = findViewById(R.id.inputPelaksana)
        acPemeriksa = findViewById(R.id.inputPemeriksa)
        acAsman = findViewById(R.id.inputAssistantManager)
        acManajer = findViewById(R.id.inputManager)

        // 2. Setup Logic
        setupDatePicker()
        loadDataForDropdowns() // Isi dropdown personil
        setupSiteAutofill()    // Aktifkan pencarian site otomatis

        // 3. Tombol Lanjut
        findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            lanjutKeMenu()
        }
    }

    // --- LOGIC 1: KALENDER ---
    private fun setupDatePicker() {
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day)
                    etTanggal.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    // --- LOGIC 2: DROPDOWN PERSONIL ---
    private fun loadDataForDropdowns() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.appDao()

            val pelaksana = dao.getOptionsByCategory("pelaksana")
            val pemeriksa = dao.getOptionsByCategory("pemeriksa")
            val asman = dao.getOptionsByCategory("asman")
            val manajer = dao.getOptionsByCategory("manajer")

            withContext(Dispatchers.Main) {
                setupAutoComplete(acPelaksana, pelaksana)
                setupAutoComplete(acPemeriksa, pemeriksa)
                setupAutoComplete(acAsman, asman)
                setupAutoComplete(acManajer, manajer)
            }
        }
    }

    // --- LOGIC 3: AUTOFILL SITE ---
    private fun setupSiteAutofill() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val allSiteNames = db.appDao().getAllSiteNames()

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@InspectionHeaderActivity, android.R.layout.simple_dropdown_item_1line, allSiteNames)
                acNamaSite.setAdapter(adapter)
                acNamaSite.threshold = 1

                // Saat Site Diklik -> Isi Field Lainnya
                acNamaSite.setOnItemClickListener { parent, _, position, _ ->
                    val selectedSiteName = parent.getItemAtPosition(position) as String
                    isiOtomatisDetailSite(selectedSiteName)
                }
            }
        }
    }

    private fun isiOtomatisDetailSite(namaSite: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val detail = db.appDao().getSiteDetail(namaSite)

            withContext(Dispatchers.Main) {
                if (detail != null) {
                    // Isi EditText Read-Only dengan data dari DB
                    etTipePop.setText(detail.tipePop ?: "-")
                    etModelBangunan.setText(detail.modelBangunan ?: "-")
                    etArea.setText(detail.area ?: "-")

                    Toast.makeText(this@InspectionHeaderActivity, "Data Site Terisi Otomatis!", Toast.LENGTH_SHORT).show()
                } else {
                    // Reset jika tidak ketemu (opsional)
                    etTipePop.setText("")
                    etModelBangunan.setText("")
                    etArea.setText("")
                    Toast.makeText(this@InspectionHeaderActivity, "Detail site tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Helper Dropdown
    private fun setupAutoComplete(textView: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        textView.setAdapter(adapter)
        textView.setOnClickListener { textView.showDropDown() } // Klik langsung muncul
    }

    private fun lanjutKeMenu() {
        // Validasi
        if (etNoWo.text.isEmpty() || acNamaSite.text.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi No WO dan Nama Site", Toast.LENGTH_SHORT).show()
            return
        }

        val session = InspectionSession(
            noWo = etNoWo.text.toString(),
            tanggal = etTanggal.text.toString(),
            idSite = acNamaSite.text.toString(),
            namaSite = acNamaSite.text.toString(),
            tipePop = etTipePop.text.toString(), // Ambil dari EditText Autofill
            modelBangunan = etModelBangunan.text.toString(), // Ambil dari EditText Autofill
            area = etArea.text.toString(), // Ambil dari EditText Autofill
            pelaksana = acPelaksana.text.toString(),
            pemeriksa = acPemeriksa.text.toString(),
            asman = acAsman.text.toString(),
            manajer = acManajer.text.toString()
        )

        val intent = Intent(this, InspectionMenuActivity::class.java)
        intent.putExtra("SESSION_DATA", session)
        startActivity(intent)
    }
}